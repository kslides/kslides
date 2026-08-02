package com.kslides.export

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import java.nio.file.Files

// Downloads a headless browser on first run, so it is opt-in:
// KSLIDES_EXPORT_TEST=true ./gradlew :kslides-export:test
class PdfExportIntegrationTest : StringSpec() {
  private val enabled = System.getenv("KSLIDES_EXPORT_TEST") == "true"

  init {
    "exportPdf prints a deck to PDF end to end".config(enabled = enabled) {
      val tmpDir = Files.createTempDirectory("kslides-export-test").toFile()

      val files =
        exportPdf {
          output {
            pdf {
              outputDir = tmpDir.absolutePath
              previewPng = true
            }
          }
          presentation {
            markdownSlide { content { "# PDF Export" } }
            markdownSlide { content { "# Second Slide" } }
          }
        }

      files shouldHaveSize 2
      files.map { it.name }.sorted() shouldBe listOf("index.pdf", "index.png")

      val pdf = files.single { it.name == "index.pdf" }
      pdf.length() shouldBeGreaterThan 1_000L
      pdf.readBytes().copyOfRange(0, 4).decodeToString() shouldBe "%PDF"
    }
  }
}
