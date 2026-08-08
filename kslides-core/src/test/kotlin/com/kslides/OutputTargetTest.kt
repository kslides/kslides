package com.kslides

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File
import kotlin.io.path.createTempDirectory

class OutputTargetTest : StringSpec() {
  init {
    // Keys reach writeSlidesToFileSystem via Presentation.validatePath(), which prepends a "/".
    fun target(
      key: String,
      outputDir: String = "docs",
    ) = KSlides
      .outputTarget(outputDir, if (key.startsWith("/")) key else "/$key")
      .let { (file, rootPrefix) -> file.path to rootPrefix }

    "The root presentation writes an index.html and needs no walk back up" {
      target("/") shouldBe ("docs/index.html" to "")
    }

    "A top-level .html deck needs no dot-dot prefix" {
      target("greattalk2.html") shouldBe ("docs/greattalk2.html" to "")
    }

    "A top-level directory deck walks up one level" {
      target("greattalk1") shouldBe ("docs/greattalk1/index.html" to "../")
    }

    // Regression: the .html branch used to ignore the key's depth entirely, so a nested deck walked
    // back up zero levels and every root-relative link 404'd once published.
    "A nested .html deck walks up out of its directory" {
      target("greattalk1/other.html") shouldBe ("docs/greattalk1/other.html" to "../")
    }

    "Depth accumulates for deeply nested decks" {
      target("a/b/deep.html") shouldBe ("docs/a/b/deep.html" to "../../")
      target("a/b/c") shouldBe ("docs/a/b/c/index.html" to "../../../")
    }

    // outputDir sits above both the page and everything the page references, so extra segments in
    // it add no distance between the two.
    "A multi-segment outputDir does not add depth" {
      target("greattalk1", outputDir = "build/docs") shouldBe
        ("build/docs/greattalk1/index.html" to "../")
      target("greattalk1/other.html", outputDir = "build/docs") shouldBe
        ("build/docs/greattalk1/other.html" to "../")
      target("/", outputDir = "build/docs") shouldBe ("build/docs/index.html" to "")
    }

    // End-to-end over writeSlidesToFileSystem, mirroring the kslides-template deck layout that
    // surfaced the bug: every generated page must reach docs/revealjs/ from where it sits.
    "Generated pages link to reveal.js assets from every nesting level" {
      val outDir = createTempDirectory("kslides-output-target").toFile()
      try {
        val kslides =
          kslidesTest {
            output { outputDir = outDir.path }
            presentation { markdownSlide { content { "# Root" } } }
            presentation {
              path = "greattalk1"
              markdownSlide { content { "# Nested index" } }
            }
            presentation {
              path = "greattalk1/other.html"
              markdownSlide { content { "# Nested html" } }
            }
            presentation {
              path = "greattalk2.html"
              markdownSlide { content { "# Top-level html" } }
            }
          }
        KSlides.writeSlidesToFileSystem(kslides.outputConfig)

        // readText() throws FileNotFoundException naming the path, so a missing file self-reports.
        fun page(path: String) = File(outDir, path).readText()

        page("index.html") shouldContain """href="revealjs/dist/reveal.css""""
        page("greattalk2.html") shouldContain """href="revealjs/dist/reveal.css""""
        page("greattalk1/index.html") shouldContain """href="../revealjs/dist/reveal.css""""
        page("greattalk1/other.html") shouldContain """href="../revealjs/dist/reveal.css""""
      } finally {
        outDir.deleteRecursively()
      }
    }

    // The nested ".html" deck is declared with no sibling directory deck to create greattalk1/
    // for it, which used to make writeText fail on the missing parent.
    "A nested .html deck creates its own parent directory" {
      val outDir = createTempDirectory("kslides-output-parent").toFile()
      try {
        val kslides =
          kslidesTest {
            output { outputDir = outDir.path }
            presentation {
              path = "solo/deep/only.html"
              markdownSlide { content { "# Only deck" } }
            }
          }
        KSlides.writeSlidesToFileSystem(kslides.outputConfig)

        File(outDir, "solo/deep/only.html").readText() shouldContain
          """href="../../revealjs/dist/reveal.css""""
      } finally {
        outDir.deleteRecursively()
      }
    }
  }
}
