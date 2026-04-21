package com.kslides

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

class RecordIframeContentTest : StringSpec() {
  init {
    "HTTP + staticContent caches the rendered string and invokes the block once" {
      val kslides = KSlides()
      var invocations = 0
      recordIframeContent(
        useHttp = true,
        staticContent = true,
        kslides = kslides,
        path = "ignored",
        filename = "slide-1.html",
      ) {
        invocations += 1
        "hello"
      }

      kslides.staticIframeContent["slide-1.html"] shouldBe "hello"
      kslides.dynamicIframeContent.containsKey("slide-1.html") shouldBe false
      invocations shouldBe 1
    }

    "HTTP + !staticContent caches the lambda and defers rendering" {
      val kslides = KSlides()
      var invocations = 0
      recordIframeContent(
        useHttp = true,
        staticContent = false,
        kslides = kslides,
        path = "ignored",
        filename = "slide-2.html",
      ) {
        invocations += 1
        "lazy"
      }

      kslides.staticIframeContent.containsKey("slide-2.html") shouldBe false
      invocations shouldBe 0

      val lambda = kslides.dynamicIframeContent["slide-2.html"]
      (lambda != null) shouldBe true
      lambda!!.invoke() shouldBe "lazy"
      invocations shouldBe 1
    }

    "filesystem mode writes to <path><filename> and returns the expected content" {
      val tmpDir = Files.createTempDirectory("kslides-iframe-record").toFile()
      try {
        val kslides = KSlides()
        val path = tmpDir.absolutePath + "/"
        recordIframeContent(
          useHttp = false,
          staticContent = false,
          kslides = kslides,
          path = path,
          filename = "out.html",
        ) {
          "<html>content</html>"
        }

        val written = File(tmpDir, "out.html")
        written.exists() shouldBe true
        written.readText() shouldBe "<html>content</html>"
        kslides.staticIframeContent.isEmpty() shouldBe true
        kslides.dynamicIframeContent.isEmpty() shouldBe true
      } finally {
        tmpDir.deleteRecursively()
      }
    }

    "HTTP caches respect computeIfAbsent: second call with same filename is a no-op" {
      val kslides = KSlides()
      var invocations = 0
      repeat(3) {
        recordIframeContent(
          useHttp = true,
          staticContent = true,
          kslides = kslides,
          path = "ignored",
          filename = "same.html",
        ) {
          invocations += 1
          "round-$invocations"
        }
      }

      kslides.staticIframeContent["same.html"] shouldBe "round-1"
      invocations shouldBe 1
    }
  }
}
