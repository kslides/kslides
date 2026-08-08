package com.kslides

import com.kslides.Page.generatePage
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import kotlinx.html.h2
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * A generated page addresses the output root for iframe/image `src` attributes (`playground/`,
 * `letsPlot/`, `kroki/` content) and for the `favicon.ico` link, so both have to reach it from
 * wherever the deck sits — the same depth problem [KSlides.outputTarget] solves for reveal.js
 * assets.
 */
class NestedDeckPathTest : StringSpec() {
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

    "every deck reaches the output root from its own depth" {
      // Keyed on the bare walk, since every root-relative URL on the page is built from it. Slide
      // ids run across all the decks in one render, so pin the walk rather than the generated
      // filename — and the resolve check below is the ground truth for the rest.
      val expected =
        mapOf(
          "index.html" to "",
          "greattalk1/index.html" to "../",
          "greattalk1/other.html" to "../",
          "a/b/c/index.html" to "../../../",
          "x/y/deep.html" to "../../",
        )
      val outDir = createTempDirectory("kslides-nested").toFile()
      try {
        val kslides =
          kslidesTest {
            output { outputDir = outDir.path }
            listOf("/", "greattalk1", "greattalk1/other.html", "a/b/c", "x/y/deep.html")
              .forEach { presentation(deckWithPlayground(it)) }
          }
        KSlides.writeSlidesToFileSystem(kslides.outputConfig)

        expected.forEach { (page, walk) ->
          val html = File(outDir, page).readText()
          val src = iframeSrc(html)
          withClue(page) {
            src!! shouldStartWith "${walk}playground/"
            html shouldContain """href="${walk}favicon.ico""""
            // The browser resolves the src against the page's own directory, so that resolution has
            // to land on a file that exists — this is the 404 the fix is about.
            File(outDir, page)
              .parentFile
              .resolve(src)
              .canonicalFile.isFile shouldBe true
          }
        }
      } finally {
        outDir.deleteRecursively()
      }
    }

    "HTTP mode addresses the output root absolutely, from any deck depth" {
      // The iframe routes and the classpath root that supplies the favicon are both registered at
      // the root of the routing tree, so a relative URL would resolve against the deck's own path
      // and miss.
      val kslides = kslidesTest { presentation(deckWithPlayground("greattalk1/other.html")) }
      val html = generatePage(kslides.presentation("/greattalk1/other.html"))
      iframeSrc(html) shouldBe "/playground/slide-1-1.html"
      html shouldContain """href="/favicon.ico""""
    }
  }
}
