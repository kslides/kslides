package com.kslides

import com.kslides.Page.generatePage
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
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
    /** Write every deck to a temp dir and return it, so srcs can be resolved against real files. */
    fun writeDecks(block: KSlides.() -> Unit): File {
      val outDir = createTempDirectory("kslides-nested-iframe").toFile()
      val kslides =
        kslidesTest {
          output { outputDir = outDir.path }
          block()
        }
      KSlides.writeSlidesToFileSystem(kslides.outputConfig)
      return outDir
    }

    fun deckWithPlayground(
      deckPath: String,
      block: Presentation.() -> Unit = {},
    ): Presentation.() -> Unit =
      {
        path = deckPath
        block()
        dslSlide {
          content {
            h2 { +"Deck" }
            playground("does-not-exist.kt")
          }
        }
      }

    // Attributes are serialized alphabetically, so src is not necessarily first in the tag.
    val iframeSrcRegex = Regex("""<iframe[^>]*\ssrc="([^"]+)"""")

    /** The `src` of the first iframe on the written page. */
    fun iframeSrc(
      outDir: File,
      page: String,
    ) = iframeSrcRegex.find(File(outDir, page).readText())?.groupValues?.get(1)

    "a root deck keeps an unprefixed iframe src" {
      val outDir = writeDecks { presentation(deckWithPlayground("/")) }
      try {
        iframeSrc(outDir, "index.html") shouldBe "playground/slide-1-1.html"
      } finally {
        outDir.deleteRecursively()
      }
    }

    "a directory deck walks up to the playground directory" {
      val outDir = writeDecks { presentation(deckWithPlayground("greattalk1")) }
      try {
        iframeSrc(outDir, "greattalk1/index.html") shouldBe "../playground/slide-1-1.html"
      } finally {
        outDir.deleteRecursively()
      }
    }

    "a nested .html deck walks up out of its directory" {
      val outDir = writeDecks { presentation(deckWithPlayground("greattalk1/other.html")) }
      try {
        iframeSrc(outDir, "greattalk1/other.html") shouldBe "../playground/slide-1-1.html"
      } finally {
        outDir.deleteRecursively()
      }
    }

    "depth accumulates for deeply nested decks" {
      val outDir = writeDecks { presentation(deckWithPlayground("a/b/c")) }
      try {
        iframeSrc(outDir, "a/b/c/index.html") shouldBe "../../../playground/slide-1-1.html"
      } finally {
        outDir.deleteRecursively()
      }
    }

    // The assertion that actually encodes the bug: the browser resolves the src against the page's
    // own directory, so that resolution has to land on a file that exists.
    "every deck's iframe src resolves to the file that was written" {
      val outDir =
        writeDecks {
          presentation(deckWithPlayground("/"))
          presentation(deckWithPlayground("greattalk1"))
          presentation(deckWithPlayground("greattalk1/other.html"))
          presentation(deckWithPlayground("a/b/deep.html"))
        }
      try {
        listOf(
          "index.html",
          "greattalk1/index.html",
          "greattalk1/other.html",
          "a/b/deep.html",
        ).forEach { page ->
          val src = iframeSrc(outDir, page) ?: error("No iframe on $page")
          val resolved = File(outDir, page).parentFile.resolve(src).canonicalFile
          withClue("$page -> $src") { resolved.isFile shouldBe true }
        }
      } finally {
        outDir.deleteRecursively()
      }
    }

    "HTTP mode addresses the iframe routes absolutely, from any deck depth" {
      // The routes are registered at the root of the routing tree (KSlides.iframeRoutes), so a
      // relative src would resolve against the deck's own path and miss.
      val kslides =
        kslidesTest {
          presentation(deckWithPlayground("greattalk1/other.html"))
        }
      val src = iframeSrcRegex.find(generatePage(kslides.presentation("/greattalk1/other.html")))?.groupValues?.get(1)
      src shouldBe "/playground/slide-1-1.html"
    }
  }
}
