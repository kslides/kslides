package com.kslides

import com.kslides.Page.generatePage
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import kotlinx.html.h2
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * Iframe and image `src` attributes have to reach `<outputDir>/playground/` (and `letsPlot/`,
 * `kroki/`) from wherever the deck that references them sits. The content is always written at the
 * output root, so a nested deck has to walk back up — the same depth problem [KSlides.outputTarget]
 * solves for reveal.js assets.
 */
class NestedDeckIframePathTest : StringSpec() {
  init {
    fun deckWithPlayground(deckPath: String): Presentation.() -> Unit =
      {
        path = deckPath
        dslSlide {
          content {
            h2 { +"Deck" }
            playground("does-not-exist.kt")
          }
        }
      }

    // Attributes are serialized alphabetically, so src is not necessarily first in the tag.
    val iframeSrcRegex = Regex("""<iframe[^>]*\ssrc="([^"]+)"""")

    fun iframeSrc(html: String) = iframeSrcRegex.find(html)?.groupValues?.get(1)

    "every deck's iframe src reaches the playground directory from its own depth" {
      // Slide ids run across all the decks in one render, so pin the depth prefix rather than the
      // generated filename — the prefix is what this is about, and the resolve check below is the
      // ground truth for the rest.
      val expected =
        mapOf(
          "index.html" to "playground/",
          "greattalk1/index.html" to "../playground/",
          "greattalk1/other.html" to "../playground/",
          "a/b/c/index.html" to "../../../playground/",
          "x/y/deep.html" to "../../playground/",
        )
      val outDir = createTempDirectory("kslides-nested-iframe").toFile()
      try {
        val kslides =
          kslidesTest {
            output { outputDir = outDir.path }
            listOf("/", "greattalk1", "greattalk1/other.html", "a/b/c", "x/y/deep.html")
              .forEach { presentation(deckWithPlayground(it)) }
          }
        KSlides.writeSlidesToFileSystem(kslides.outputConfig)

        expected.forEach { (page, prefix) ->
          val actual = iframeSrc(File(outDir, page).readText())
          withClue(page) {
            actual!! shouldStartWith prefix
            // The browser resolves the src against the page's own directory, so that resolution has
            // to land on a file that exists — this is the 404 the fix is about.
            File(outDir, page)
              .parentFile
              .resolve(actual)
              .canonicalFile.isFile shouldBe true
          }
        }
      } finally {
        outDir.deleteRecursively()
      }
    }

    "HTTP mode addresses the iframe routes absolutely, from any deck depth" {
      // The routes are registered at the root of the routing tree (KSlides.iframeRoutes), so a
      // relative src would resolve against the deck's own path and miss.
      val kslides = kslidesTest { presentation(deckWithPlayground("greattalk1/other.html")) }
      iframeSrc(generatePage(kslides.presentation("/greattalk1/other.html"))) shouldBe
        "/playground/slide-1-1.html"
    }
  }
}
