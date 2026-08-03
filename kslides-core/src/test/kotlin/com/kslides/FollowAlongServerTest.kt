package com.kslides

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

class FollowAlongServerTest : StringSpec() {
  private val token = "test-token"

  private fun buildDeck() =
    buildKSlides {
      output {
        followAlong = true
        presenterToken = token
      }
      presentation { markdownSlide { content { "# A" } } }
    }

  private fun wsUrl(
    port: Int,
    query: String,
  ) = "ws://localhost:$port${FollowAlong.FOLLOW_PATH}$query"

  /** Test harness holding the running server's port, a ws-capable client, and open connections. */
  private class Harness(
    val scope: CoroutineScope,
    val client: HttpClient,
    val port: Int,
  ) {
    val connections = mutableListOf<Job>()
  }

  /**
   * Connects a viewer and pipes every text frame it receives into the returned channel. Returns
   * once the initial status frame has arrived, which also guarantees the server has registered
   * the viewer for broadcasts. The connection stays open until the harness tears down.
   */
  private suspend fun Harness.connectViewer(path: String = "/"): Channel<String> {
    val frames = Channel<String>(Channel.UNLIMITED)
    val firstFrame = CompletableDeferred<Unit>()
    connections +=
      scope.launch {
        client.webSocket(wsUrl(port, "?path=$path&role=viewer")) {
          for (frame in incoming) {
            if (frame is Frame.Text) {
              frames.send(frame.readText())
              firstFrame.complete(Unit)
            }
          }
          frames.close()
        }
      }
    firstFrame.await()
    return frames
  }

  /** Drains [frames] until one contains [needle]; times out via the surrounding withTimeout. */
  private suspend fun awaitFrameContaining(
    frames: Channel<String>,
    needle: String,
  ): String {
    while (true) {
      val frame = frames.receive()
      if (needle in frame) return frame
    }
  }

  private fun withServer(block: suspend Harness.() -> Unit) {
    val kslides = buildDeck()
    kslides.startHttpServer(port = 0).use { server ->
      HttpClient(CIO) { install(WebSockets) }.use { client ->
        runBlocking {
          withTimeout(30_000) {
            val harness = Harness(this, client, server.port)
            try {
              harness.block()
            } finally {
              // Open viewer connections would otherwise keep this scope alive forever.
              harness.connections.forEach { it.cancel() }
            }
          }
        }
      }
    }
    kslides.close()
  }

  init {
    "presenter state reaches connected viewers, and late joiners get the last state" {
      withServer {
        val viewer = connectViewer()
        viewer.receive() shouldContain "\"live\":false" // initial status, no presenter yet

        client.webSocket(wsUrl(port, "?path=/&role=presenter&token=$token")) {
          awaitFrameContaining(viewer, "\"live\":true") // presenter-connected broadcast

          send("""{"type":"state","state":{"indexh":3,"indexv":0}}""")
          awaitFrameContaining(viewer, "\"indexh\":3")

          // A late joiner immediately receives live status and the last-known state.
          val lateJoiner = connectViewer()
          awaitFrameContaining(lateJoiner, "\"live\":true")
          awaitFrameContaining(lateJoiner, "\"indexh\":3")
        }

        // Presenter disconnect broadcasts live=false.
        awaitFrameContaining(viewer, "\"live\":false")
      }
    }

    "an invalid presenter token is rejected with a policy violation" {
      withServer {
        client.webSocket(wsUrl(port, "?path=/&role=presenter&token=wrong")) {
          val reason = closeReason.await()
          reason?.knownReason shouldBe CloseReason.Codes.VIOLATED_POLICY
          reason?.message shouldBe "Invalid presenter token"
        }
      }
    }

    "an unknown presentation path is rejected" {
      withServer {
        client.webSocket(wsUrl(port, "?path=/nope&role=viewer")) {
          val reason = closeReason.await()
          reason?.knownReason shouldBe CloseReason.Codes.VIOLATED_POLICY
          reason?.message shouldBe "Unknown presentation path"
        }
      }
    }

    "viewer frames are ignored — the audience is read-only by construction" {
      withServer {
        val watcher = connectViewer()
        watcher.receive() // initial status

        // Another viewer tries to publish state; nothing may reach the watcher.
        client.webSocket(wsUrl(port, "?path=/&role=viewer")) {
          incoming.receive() // its own initial status
          send("""{"type":"state","state":{"indexh":9}}""")
          delay(500)
        }

        watcher.tryReceive().isSuccess shouldBe false

        // And the bogus state was not stored: a fresh viewer gets status only, no state.
        val fresh = connectViewer()
        fresh.receive() shouldContain "\"type\":\"status\""
        delay(300)
        fresh.tryReceive().isSuccess shouldBe false
      }
    }

    "a new presenter supersedes the old one, which is told it was replaced" {
      withServer {
        val viewer = connectViewer()

        val firstReplaced = CompletableDeferred<CloseReason?>()
        val firstConnected = CompletableDeferred<Unit>()
        connections +=
          scope.launch {
            client.webSocket(wsUrl(port, "?path=/&role=presenter&token=$token")) {
              firstConnected.complete(Unit)
              firstReplaced.complete(closeReason.await())
            }
          }
        firstConnected.await()
        awaitFrameContaining(viewer, "\"live\":true")

        client.webSocket(wsUrl(port, "?path=/&role=presenter&token=$token")) {
          val reason = firstReplaced.await()
          reason?.message shouldBe FollowAlong.REPLACED_REASON

          // The new presenter drives the deck.
          send("""{"type":"state","state":{"indexh":7,"indexv":1}}""")
          awaitFrameContaining(viewer, "\"indexh\":7")
        }
      }
    }

    "an abrupt presenter disconnect still notifies viewers" {
      withServer {
        val viewer = connectViewer()
        viewer.receive() // initial status

        val presenterUp = CompletableDeferred<Unit>()
        val presenterJob =
          scope.launch {
            client.webSocket(wsUrl(port, "?path=/&role=presenter&token=$token")) {
              presenterUp.complete(Unit)
              closeReason.await() // stay connected until cancelled
            }
          }
        presenterUp.await()
        awaitFrameContaining(viewer, "\"live\":true")

        // Cancellation drops the socket without a clean close handshake — like a browser
        // navigating away — and viewers must still learn the presenter went offline.
        presenterJob.cancel()
        awaitFrameContaining(viewer, "\"live\":false")
      }
    }

    "a broadcast reaches many concurrent viewers" {
      withServer {
        val viewers = (1..40).map { connectViewer() }
        viewers.forEach { it.receive() } // drain each initial status

        client.webSocket(wsUrl(port, "?path=/&role=presenter&token=$token")) {
          send("""{"type":"state","state":{"indexh":5,"indexv":2,"indexf":1}}""")
          // Each viewer buffers into its own unlimited channel, so a sequential drain is
          // sufficient; the surrounding withTimeout bounds the whole check.
          viewers.forEach { awaitFrameContaining(it, "\"indexh\":5") }
        }
      }
    }
  }
}
