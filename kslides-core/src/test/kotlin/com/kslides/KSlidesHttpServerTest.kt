package com.kslides

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking

class KSlidesHttpServerTest : StringSpec() {
  init {
    "startHttpServer serves presentations on an ephemeral port" {
      val kslides =
        buildKSlides {
          presentation {
            markdownSlide { content { "# Served" } }
          }
        }

      kslides.startHttpServer(port = 0).use { server ->
        server.port shouldBeGreaterThan 0
        runBlocking {
          HttpClient(CIO).use { client ->
            val response = client.get("http://localhost:${server.port}/")
            response.status shouldBe HttpStatusCode.OK
            response.bodyAsText() shouldContain "Served"
          }
        }
      }
      kslides.close()
    }

    "buildKSlides evaluates config blocks without emitting output" {
      val kslides =
        buildKSlides {
          output {
            enableFileSystem = true
            enableHttp = true
            outputDir = "should-not-be-written"
          }
          presentation {
            path = "demo.html"
            markdownSlide { content { "# Demo" } }
          }
        }

      // Config was applied, but no filesystem output was produced and no server started.
      kslides.outputConfig.outputDir shouldBe "should-not-be-written"
      java.io.File("should-not-be-written").exists() shouldBe false
      kslides.close()
    }

    "presentationPaths lists registered decks in declaration order" {
      val kslides =
        kslidesTest {
          presentation { markdownSlide { content { "# A" } } }
          presentation {
            path = "demo.html"
            markdownSlide { content { "# B" } }
          }
        }
      kslides.presentationPaths shouldBe listOf("/", "/demo.html")
    }
  }
}
