package com.kslides

import io.ktor.server.engine.EmbeddedServer
import kotlinx.coroutines.runBlocking

/**
 * Handle for a running kslides HTTP server started via [KSlides.startHttpServer].
 *
 * Wraps the underlying Ktor server so that callers (e.g. the kslides-export module) do not need
 * Ktor on their compile classpath. Closing the handle stops the server.
 */
class KSlidesHttpServer internal constructor(
  private val server: EmbeddedServer<*, *>,
) : AutoCloseable {
  /** The port the server is bound to — the actual port when the server was started on port `0`. */
  val port: Int by lazy {
    runBlocking {
      server.engine
        .resolvedConnectors()
        .first()
        .port
    }
  }

  override fun close() {
    server.stop(STOP_GRACE_MILLIS, STOP_TIMEOUT_MILLIS)
  }

  companion object {
    private const val STOP_GRACE_MILLIS = 250L
    private const val STOP_TIMEOUT_MILLIS = 1_000L
  }
}
