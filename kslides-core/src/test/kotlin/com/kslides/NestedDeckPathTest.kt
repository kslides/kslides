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
        presentationConfig {
          // Two corner-image srcs (one relative, one external) plus a relative logo src. The hrefs
          // are required scaffolding — a corner <img> is only emitted inside a non-blank href.
          topLeftHref = "https://example.com"
          topLeftSvgSrc = "images/gh.svg"
          topRightHref = "./"
          topRightSvgSrc = "https://example.com/home.svg"
          customTheme { logo("images/logo.png") }
        }
        dslSlide {
          slideConfig {
            backgroundImage = "images/bg.png"
            // data-background takes a color too, and a color must survive untouched.
            background = "#ffffff"
          }
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
            // Author-supplied asset paths resolve the same way, including slide backgrounds...
            html shouldContain """src="${walk}images/gh.svg""""
            html shouldContain """src="${walk}images/logo.png""""
            html shouldContain """data-background-image="${walk}images/bg.png""""
            // ...while an already-anchored src, a color, and corner links of any kind, are not.
            html shouldContain """src="https://example.com/home.svg""""
            html shouldContain """data-background="#ffffff""""
            html shouldContain """href="./""""
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

    "background values follow reveal.js's own color-vs-image test, and data: URIs survive whole" {
      val kslides =
        kslidesTest {
          presentation {
            path = "talks/deck.html"
            dslSlide {
              slideConfig {
                // reveal.js treats a cache-busted path as an image, so it has to resolve too.
                background = "images/bg.png?v=2"
                // A data: URI carries its own comma; splitting the video list must not cut it up.
                backgroundVideo = "data:video/mp4;base64,AAAA"
              }
              content { h2 { +"Deck" } }
            }
          }
        }
      val html = generatePage(kslides.presentation("/talks/deck.html"), useHttp = false, rootPrefix = "../")
      html shouldContain """data-background="../images/bg.png?v=2""""
      html shouldContain """data-background-video="data:video/mp4;base64,AAAA""""
    }

    "an anchored value is emitted as written, wherever it is used" {
      val kslides =
        kslidesTest {
          presentation {
            path = "talks/deck.html"
            presentationConfig {
              // Uppercase scheme: anchored, and must not collect a "../" walk.
              topLeftHref = "https://example.com"
              topLeftSvgSrc = "HTTPS://cdn.example.com/gh.svg"
            }
            // Site-absolute, through the asset-directory resolver rather than the output root.
            cssFiles += CssFile("/css/mine.css")
            jsFiles += JsFile("/js/mine.js")
            dslSlide { content { h2 { +"Deck" } } }
          }
        }
      val html = generatePage(kslides.presentation("/talks/deck.html"), useHttp = false, rootPrefix = "../")
      html shouldContain """src="HTTPS://cdn.example.com/gh.svg""""
      html shouldContain """href="/css/mine.css""""
      html shouldContain """src="/js/mine.js""""
    }

    "a relative path that merely starts with http is still relative" {
      // "http" is a prefix of this filename, not a scheme — the old guard called it anchored and
      // left it bare, so it 404'd from a nested deck.
      val kslides =
        kslidesTest {
          presentation {
            path = "talks/deck.html"
            presentationConfig {
              topLeftHref = "./"
              topLeftSvgSrc = "http-icons/gh.svg"
            }
            dslSlide { content { h2 { +"Deck" } } }
          }
        }
      val html = generatePage(kslides.presentation("/talks/deck.html"), useHttp = false, rootPrefix = "../")
      html shouldContain """src="../http-icons/gh.svg""""
    }

    "HTTP mode addresses the output root absolutely, from any deck depth" {
      // The iframe routes and the classpath root that supplies the favicon are both registered at
      // the root of the routing tree, so a relative URL would resolve against the deck's own path
      // and miss.
      val kslides = kslidesTest { presentation(deckWithPlayground("greattalk1/other.html")) }
      val html = generatePage(kslides.presentation("/greattalk1/other.html"))
      iframeSrc(html) shouldBe "/playground/slide-1-1.html"
      html shouldContain """href="/favicon.ico""""
      // Author-supplied paths too: the classpath root that backs them is served at "/".
      html shouldContain """src="/images/gh.svg""""
      html shouldContain """src="/images/logo.png""""
      html shouldContain """data-background-image="/images/bg.png""""
      html shouldContain """src="https://example.com/home.svg""""
    }
  }
}
