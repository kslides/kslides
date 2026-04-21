package com.kslides

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.ContentType

class DiagramOutputTypeTest : StringSpec() {
  init {
    "each DiagramOutputType carries the matching suffix and ContentType" {
      DiagramOutputType.PNG.suffix shouldBe "png"
      DiagramOutputType.PNG.contentType shouldBe ContentType.Image.PNG

      DiagramOutputType.SVG.suffix shouldBe "svg"
      DiagramOutputType.SVG.contentType shouldBe ContentType.Image.SVG

      DiagramOutputType.JPEG.suffix shouldBe "jpeg"
      DiagramOutputType.JPEG.contentType shouldBe ContentType.Image.JPEG
    }

    "outputTypeFromSuffix resolves a known suffix" {
      DiagramOutputType.outputTypeFromSuffix("png") shouldBe DiagramOutputType.PNG
      DiagramOutputType.outputTypeFromSuffix("svg") shouldBe DiagramOutputType.SVG
      DiagramOutputType.outputTypeFromSuffix("jpeg") shouldBe DiagramOutputType.JPEG
    }

    "outputTypeFromSuffix throws on an unknown suffix" {
      shouldThrowExactly<IllegalArgumentException> {
        DiagramOutputType.outputTypeFromSuffix("gif")
      }
    }
  }
}
