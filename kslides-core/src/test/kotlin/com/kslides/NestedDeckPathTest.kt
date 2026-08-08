package com.kslides

import com.kslides.Page.generatePage
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith
import kotlinx.html.h2
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * A generated page addresses the output root for iframe/image `src` attributes (`playground/`,
 * `letsPlot/`, `kroki/` content) and for the favicon link, so both have to reach it from
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

    // A deck one level below the output root, rendered to the filesystem — the depth every walk
    // asserted below is keyed on.
    fun nestedDeckHtml(block: Presentation.() -> Unit): String {
      val kslides =
        kslidesTest {
          presentation {
            path = "talks/deck.html"
            block()
            dslSlide { content { h2 { +"Deck" } } }
          }
        }
      return generatePage(kslides.presentation("/talks/deck.html"), useHttp = false, rootPrefix = "../")
    }

    "background values follow reveal.js's own color-vs-image test, and data: URIs survive whole" {
      val html =
        nestedDeckHtml {
          presentationConfig {
            slideConfig {
              // reveal.js treats a cache-busted path as an image, so it has to resolve too.
              background = "images/bg.png?v=2"
              // A data: URI carries its own comma; splitting the video list must not cut it up.
              backgroundVideo = "data:video/mp4;base64,AAAA"
            }
          }
        }
      html shouldContain """data-background="../images/bg.png?v=2""""
      html shouldContain """data-background-video="data:video/mp4;base64,AAAA""""
    }

    "a background iframe resolves like the other background assets" {
      // The only background asset the suite did not pin. reveal.js resolves data-background-iframe
      // against the page exactly as it does the image and video forms, so it walks up too.
      val html =
        nestedDeckHtml {
          presentationConfig {
            slideConfig {
              backgroundIframe = "embeds/demo.html"
              backgroundInteractive = true
            }
          }
        }
      html shouldContain """data-background-iframe="../embeds/demo.html""""
      html shouldContain "data-background-interactive"

      // An external embed is somebody else's URL — emitted as written.
      nestedDeckHtml {
        presentationConfig { slideConfig { backgroundIframe = "https://example.com/embed" } }
      } shouldContain """data-background-iframe="https://example.com/embed""""
    }

    "a Kroki diagram reaches the kroki directory from a nested deck" {
      // The third of the three iframe/image sources — playground and letsPlot are covered above and
      // in LetsPlotDslTest, and all three route through DslSlide.iframeSrc. Seeding the content
      // cache keyed by the generated filename keeps computeIfAbsent from ever calling Kroki, so
      // this stays a path test with no network and no server.
      val outDir = createTempDirectory("kslides-kroki").toFile()
      try {
        val kslides =
          kslidesTest {
            // Filesystem mode writes the fetched image next to the decks, so keep it out of docs/.
            output { outputDir = outDir.path }
            presentation {
              path = "talks/deck.html"
              dslSlide { content { diagram("graphviz") { source = "digraph { a -> b }" } } }
            }
          }
        // content{} runs at render time, so seeding here still beats the diagram to the cache.
        kslides.staticKrokiContent["slide-1-1.svg"] = "<svg/>".toByteArray()

        val html = generatePage(kslides.presentation("/talks/deck.html"), useHttp = false, rootPrefix = "../")
        html shouldContain """src="../kroki/slide-1-1.svg""""
        // And the walk has to land on the file the render just wrote.
        File(outDir, "talks")
          .resolve("../kroki/slide-1-1.svg")
          .canonicalFile.isFile shouldBe true
      } finally {
        outDir.deleteRecursively()
      }
    }

    "an anchored value is emitted as written; one that merely starts with http is not" {
      val html =
        nestedDeckHtml {
          presentationConfig {
            // Uppercase scheme: anchored, and must not collect a "../" walk.
            topLeftHref = "https://example.com"
            topLeftSvgSrc = "HTTPS://cdn.example.com/gh.svg"
            // "http" is a prefix of this filename, not a scheme — the old guard called it
            // anchored and left it bare, so it 404'd from a nested deck.
            topRightHref = "./"
            topRightSvgSrc = "http-icons/gh.svg"
          }
          // Site-absolute: anchored, so neither origin's prefix is applied.
          cssFiles += CssFile("/css/mine.css")
          jsFiles += JsFile("/js/mine.js")
        }
      html shouldContain """src="HTTPS://cdn.example.com/gh.svg""""
      html shouldContain """src="../http-icons/gh.svg""""
      html shouldContain """href="/css/mine.css""""
      html shouldContain """src="/js/mine.js""""
    }

    "a css or js file resolves against the origin it names" {
      val html =
        nestedDeckHtml {
          // The default names a file inside the reveal.js assets...
          cssFiles += CssFile("plugin/mine/mine.css")
          jsFiles += JsFile("plugin/mine/mine.js")
          // ...and OUTPUT_ROOT one published alongside the decks, reached from this deck's depth.
          cssFiles += CssFile("css/site.css", origin = AssetOrigin.OUTPUT_ROOT)
          jsFiles += JsFile("js/site.js", origin = AssetOrigin.OUTPUT_ROOT)
        }
      html shouldContain """href="../revealjs/plugin/mine/mine.css""""
      html shouldContain """src="../revealjs/plugin/mine/mine.js""""
      html shouldContain """href="../css/site.css""""
      html shouldContain """src="../js/site.js""""
    }

    "the menu plugin's themesPath is handed over verbatim, unlike everything kslides emits itself" {
      // The deliberate exception to this whole file. themesPath goes inside the opaque reveal.js
      // options object, so kslides never sees it as a URL and the menu plugin resolves it against
      // the page itself. Adding a walk here would double up on whatever the author already wrote.
      // Pinned so a later sweep through the path resolvers does not "fix" it into a bug.
      val html =
        nestedDeckHtml {
          presentationConfig {
            enableMenu = true
            menuConfig { themesPath = "css/theme/" }
          }
        }
      html shouldContain "css/theme/"
      html shouldNotContain "../css/theme/"
    }

    "the favicon link is configurable, and blank emits nothing" {
      // Default: one link, and the path resolves like any other author asset.
      val default = nestedDeckHtml { }
      default shouldContain """<link href="../favicon.ico" rel="icon" type="image/x-icon">"""
      // Not two — "shortcut icon" was an IE alias every other browser reads as plain "icon".
      default shouldNotContain "shortcut icon"

      // The type hint is derived from the filename, not special-cased to .ico.
      nestedDeckHtml { presentationConfig { favicon = "images/icon.png" } } shouldContain
        """<link href="../images/icon.png" rel="icon" type="image/png">"""

      // Nothing to derive it from, so the hint is omitted and the browser infers from the response.
      nestedDeckHtml { presentationConfig { favicon = "icon" } } shouldContain
        """<link href="../icon" rel="icon">"""

      // Blank opts out entirely — kslides ships no icon, so this is how you stop asking for one.
      nestedDeckHtml { presentationConfig { favicon = "" } } shouldNotContain """rel="icon""""
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
