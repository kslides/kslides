package com.kslides

import com.kslides.Page.generatePage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class MermaidTest : StringSpec() {
  // Single-slide mermaid deck; most tests only vary the diagram source.
  private fun mermaidDeck(source: String = "graph TD; A-->B") = kslidesTest { presentation { dslSlide { content { mermaid(source) } } } }

  private fun renderMermaid(source: String = "graph TD; A-->B") = generatePage(mermaidDeck(source).presentation("/"))

  init {
    "mermaid() emits a pre.mermaid element containing the trimIndent-ed diagram source" {
      val html =
        renderMermaid(
          """
          graph TD
            A[Author] --> B(kslides)
          """,
        )
      html shouldContain """<pre class="mermaid">"""
      val preContent = html.substringAfter("""<pre class="mermaid">""").substringBefore("</pre>")
      preContent shouldBe "graph TD\n  A[Author] --&gt; B(kslides)"
    }

    "a deck using mermaid gets the bundled runtime, init snippet, and head CSS" {
      val html = renderMermaid()
      html shouldContain Mermaid.MERMAID_JS_PATH
      html shouldContain "mermaid.initialize({ startOnLoad: false,"
      html shouldContain "Reveal.on('ready', kslidesMermaidSync);"
      html shouldContain "Reveal.on('slidechanged', kslidesMermaidSync);"
      html shouldContain ".reveal pre.mermaid:not([data-processed])"
    }

    "a deck without mermaid does not include the runtime or init snippet" {
      val kslides = kslidesTest { presentation { markdownSlide { content { "# No diagrams here" } } } }

      val html = generatePage(kslides.presentation("/"))
      html shouldNotContain Mermaid.MERMAID_JS_PATH
      html shouldNotContain "mermaid.initialize"
      html shouldNotContain ".reveal pre.mermaid"
    }

    "the runtime is scoped per presentation: only the deck with a mermaid block includes it" {
      val kslides =
        kslidesTest {
          presentation {
            dslSlide {
              content { mermaid("graph TD; A-->B") }
            }
          }
          presentation {
            path = "/plain.html"
            markdownSlide { content { "# Plain" } }
          }
        }

      generatePage(kslides.presentation("/")) shouldContain Mermaid.MERMAID_JS_PATH
      generatePage(kslides.presentation("/plain.html")) shouldNotContain Mermaid.MERMAID_JS_PATH
    }

    "HTML-sensitive diagram source survives the kotlinx.html rendering round-trip" {
      val source =
        """
        graph TD
          A["x < y & 'z'"] --> B["a > b"]
          B --> C["say ${'"'}hi${'"'}"]
        """
      val html = renderMermaid(source)
      val preContent = html.substringAfter("""<pre class="mermaid">""").substringBefore("</pre>")

      // The serializer must have escaped the markup-sensitive characters (a raw "<" would
      // otherwise open a tag and corrupt the diagram text).
      preContent shouldContain "&lt;"
      preContent shouldContain "&amp;"
      preContent shouldNotContain "<y"

      // A browser parsing the escaped text yields exactly the original source (Mermaid decodes
      // the innerHTML entities before parsing). &amp; must be decoded last, as a browser would.
      val roundTripped =
        preContent
          .replace("&lt;", "<")
          .replace("&gt;", ">")
          .replace("&quot;", "\"")
          .replace("&amp;", "&")
      roundTripped shouldBe source.trimIndent()
    }

    "mermaid() with a blank source is skipped — no pre.mermaid, no runtime script" {
      val html = renderMermaid("   ")
      html shouldNotContain """<pre class="mermaid">"""
      html shouldNotContain Mermaid.MERMAID_JS_PATH
    }

    "the init snippet picks Mermaid's theme from the reveal.js theme" {
      val kslides =
        kslidesTest {
          presentation {
            // The kslides default theme is BLACK (dark)
            dslSlide {
              content { mermaid("graph TD; A-->B") }
            }
          }
          presentation {
            path = "/light.html"
            presentationConfig { theme = PresentationTheme.WHITE }
            dslSlide {
              content { mermaid("graph TD; A-->B") }
            }
          }
        }

      generatePage(kslides.presentation("/")) shouldContain "theme: 'dark'"
      generatePage(kslides.presentation("/light.html")) shouldContain "theme: 'default'"
    }

    "the mermaid flag resets between renders — a re-render emits the runtime exactly once" {
      val p = mermaidDeck().presentation("/")
      generatePage(p)
      val secondRender = generatePage(p)
      Regex
        .escape(Mermaid.MERMAID_JS_PATH)
        .toRegex()
        .findAll(secondRender)
        .count() shouldBe 1
    }
  }
}
