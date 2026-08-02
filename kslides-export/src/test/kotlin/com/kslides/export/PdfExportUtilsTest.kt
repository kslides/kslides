package com.kslides.export

import com.kslides.config.PdfConfig
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.io.File

class PdfExportUtilsTest : StringSpec() {
  init {
    "deckName maps presentation paths to file/filter names" {
      deckName("/") shouldBe "index"
      deckName("/index.html") shouldBe "index"
      deckName("/demo.html") shouldBe "demo"
      deckName("demo.html") shouldBe "demo"
      deckName("/demo") shouldBe "demo"
      deckName("/sub/deck") shouldBe "sub-deck"
    }

    "selectDecks returns all paths by default" {
      selectDecks(listOf("/", "/demo.html"), emptyList(), null)
        .shouldContainExactly("/", "/demo.html")
    }

    "selectDecks honors excludes in any accepted form" {
      val paths = listOf("/", "/demo.html", "/extra.html")
      selectDecks(paths, listOf("demo"), null).shouldContainExactly("/", "/extra.html")
      selectDecks(paths, listOf("/demo.html"), null).shouldContainExactly("/", "/extra.html")
      selectDecks(paths, listOf("index"), null).shouldContainExactly("/demo.html", "/extra.html")
    }

    "an explicit deck request matches loosely and bypasses excludes" {
      val paths = listOf("/", "/demo.html")
      selectDecks(paths, listOf("demo"), "demo").shouldContainExactly("/demo.html")
      selectDecks(paths, emptyList(), "demo.html").shouldContainExactly("/demo.html")
      selectDecks(paths, emptyList(), "/demo.html").shouldContainExactly("/demo.html")
      selectDecks(paths, emptyList(), "index").shouldContainExactly("/")
      selectDecks(paths, emptyList(), "missing").shouldContainExactly()
    }

    "pdfOptions defaults to the deck's own print CSS page size" {
      val options = pdfOptions(PdfConfig(), File("out.pdf"))
      options.preferCSSPageSize shouldBe true
      options.printBackground shouldBe true
      options.width shouldBe null
      options.height shouldBe null
      options.path shouldBe File("out.pdf").toPath()
    }

    "pdfOptions uses an explicit page size when configured" {
      val config = PdfConfig().apply {
        pageWidth = "11in"
        pageHeight = "8.5in"
      }
      val options = pdfOptions(config, File("out.pdf"))
      options.preferCSSPageSize shouldBe null
      options.width shouldBe "11in"
      options.height shouldBe "8.5in"
    }
  }
}
