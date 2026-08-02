package com.kslides

import com.kslides.config.PdfConfig
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class PdfConfigTest : StringSpec() {
  init {
    "PdfConfig has sensible defaults" {
      val pdf = KSlides().outputConfig.pdfConfig

      pdf.outputDir shouldBe "build/pdf"
      pdf.previewPng shouldBe false
      pdf.browserChannel shouldBe ""
      pdf.readyTimeoutMillis shouldBe 30_000L
      pdf.settleMillis shouldBe 1_000L
      pdf.pageWidth shouldBe ""
      pdf.pageHeight shouldBe ""
      pdf.excludes.isEmpty() shouldBe true
    }

    "pdf{} block inside output{} configures the pdfConfig" {
      val kslides =
        kslidesTest {
          output {
            pdf {
              outputDir = "out/pdfs"
              previewPng = true
              browserChannel = "chrome"
              pageWidth = "11in"
              pageHeight = "8.5in"
              exclude("scratch", "/wip.html")
            }
          }
          presentation {
            markdownSlide { content { "# Test" } }
          }
        }

      val pdf = kslides.outputConfig.pdfConfig
      pdf.outputDir shouldBe "out/pdfs"
      pdf.previewPng shouldBe true
      pdf.browserChannel shouldBe "chrome"
      pdf.pageWidth shouldBe "11in"
      pdf.pageHeight shouldBe "8.5in"
      pdf.excludes.shouldContainExactly("scratch", "/wip.html")
    }

    "exclude() accumulates across calls" {
      val pdf = PdfConfig().apply {
        exclude("a")
        exclude("b", "c")
      }
      pdf.excludes.shouldContainExactly("a", "b", "c")
    }
  }
}
