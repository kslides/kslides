package com.kslides

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class PageTest : StringSpec() {
  init {
    "mergePreAndCode pulls a <code> on the line after <pre> onto the same line" {
      val input = "<pre>\n   <code>\n   val x = 1\n   </code>\n</pre>"
      Page.mergePreAndCode(input) shouldContain "<pre><code>"
    }

    "mergePreAndCode leaves a <code> that does not immediately follow a <pre> untouched" {
      // Regression: a <pre> with no <code> on the next line must not 'arm' preFound so that a later,
      // unrelated <code> gets merged and loses its leading whitespace.
      val input = "<pre>\nplain line\n   <code>standalone</code>"
      val out = Page.mergePreAndCode(input)
      out shouldContain "   <code>standalone</code>"
      out shouldNotContain "<pre><code"
    }

    // The head is the same whatever the deck holds, so one slide serves both head assertions.
    val deck = kslidesTest { presentation { markdownSlide { content { "# Hi" } } } }.presentation("/")

    "the viewport lets a viewer zoom" {
      // Pinning the whole content attribute — a re-added maximum-scale or user-scalable fails here.
      Page.generatePage(deck) shouldContain
        """<meta content="width=device-width, initial-scale=1.0" name="viewport">"""
    }

    "the reveal.js 3-era Apple web-app metas are gone" {
      Page.generatePage(deck) shouldNotContain "apple-mobile-web-app"
    }

    "an ampersand an author would actually type does not abort the render" {
      // "Tom & Jerry" is a malformed reference to the XML parser the page passes through, and
      // "&nbsp;" is an undefined one. Either used to take down every deck in the render, not just
      // its own slide, since rendering is a single pass. Markdown and HTML slides both carry
      // author markup, so neither can be escaped wholesale; the ampersands are repaired instead.
      val md =
        Page.generatePage(
          kslidesTest {
            presentation { markdownSlide { content { "# Tom & Jerry\n\na&nbsp;b &mdash; c" } } }
          }.presentation("/"),
        )
      // reveal.js reads the template as raw text, so Markdown must see exactly what was typed --
      // entities included. The browser decodes them when reveal hands the rendered HTML to
      // innerHTML, so the author still gets the space and the dash they asked for.
      md shouldContain "# Tom & Jerry"
      md shouldContain "a&nbsp;b &mdash; c"

      val html =
        Page.generatePage(
          kslidesTest {
            presentation { htmlSlide { content { """<p class="x">Tom & Jerry</p>""" } } }
          }.presentation("/"),
        )
      // Here the content really is DOM, so the ampersand stays escaped -- and the tag stays a tag,
      // which escaping wholesale would have destroyed.
      html shouldContain """<p class="x">Tom &amp; Jerry</p>"""
    }

    "a bare < in Markdown is no longer fatal, which is what a Kotlin deck writes" {
      // The template is a text node now, so nothing parses it as XML. Both of these aborted the
      // whole render before -- and fencing never rescued the generics either.
      val md =
        Page.generatePage(
          kslidesTest {
            presentation {
              markdownSlide {
                content { "# T\n\nval x: List<String> = listOf()\n\n```kotlin\nmapOf<String, List<Int>>()\n```" }
              }
            }
          }.presentation("/"),
        )
      md shouldContain "val x: List<String> = listOf()"
      md shouldContain "mapOf<String, List<Int>>()"
    }

    "a slide can write about </script> without truncating its own template" {
      // The one sequence a raw-text element still forbids is its own end tag -- the browser's
      // tokenizer would close the template early and spill the rest of the slide into the page.
      // reveal.js swaps the placeholder back before parsing, so this costs the author nothing.
      val template =
        Page
          .generatePage(
          kslidesTest {
            presentation { markdownSlide { content { "# T\n\nClose it with `</script>` when done." } } }
          }.presentation("/"),
        ).substringAfter("text/template\">")
          .substringBefore("</script>")
      template shouldContain "__SCRIPT_END__"
      template shouldContain "when done."
    }

    "element content survives an ampersand wherever it reaches the page" {
      // rawHtml is where the XML parse happens, so repairing there covers every markup sink at
      // once: css{}, customTheme's passthrough, the corner SVGs, and rawHtml itself. Each of these
      // aborted the whole render before.
      val page =
        Page.generatePage(
          kslidesTest {
            presentation {
              css += """.x { content: "Tom & Jerry"; }"""
              presentationConfig {
                customTheme { customProperty("--r-x", "\"A & B\"") }
                topLeftHref = "https://example.com"
                topLeftSvg = "<svg><title>P & Q</title></svg>"
                enableMenu = true
                menuConfig { titleSelector = "h1 & h2" }
              }
              dslSlide { content { rawHtml("<p>Tom & Jerry</p><p>caf&eacute;</p>") } }
            }
          }.presentation("/"),
        )
      page shouldContain "P &amp; Q"
      // Set up above but previously unasserted, so a reader could not tell they were load-bearing.
      // Both used to abort the render; both come back bare from their raw-text elements.
      page shouldContain "A & B"
      page shouldContain "h1 & h2"
      // Inside <style> the serializer hands the ampersand back bare, so the CSS is what was written.
      page shouldContain """content: "Tom & Jerry""""
      // And the tag it was wrapped in is still a tag.
      page shouldContain "<p>Tom &amp; Jerry</p>"
      // rawHtml is element content, so a named entity is decoded rather than shown literally, and
      // the serializer re-emits it as an entity the browser will decode. Escaping it instead would
      // give "&amp;eacute;", which renders as the six visible characters -- the whole reason
      // rawHtml does not share the script/style treatment.
      page shouldContain "caf&eacute;"
      page shouldNotContain "&amp;eacute;"
    }

    "author styles are not scoped to screen media, so they also apply when printing" {
      // Regression for PDF export: media="screen" styles vanish in ?print-pdf / kslides-export
      // output, which un-positioned the corner links and produced a blank leading PDF page.
      val presentation =
        kslidesTest {
          css += ".reveal h4 { color: red; }"
          presentation {
            markdownSlide {
              slideConfig { codeFontSize = "0.5em" }
              content { "# Styled\n```kotlin\nval x = 1\n```" }
            }
          }
        }.presentation("/")

      val html = Page.generatePage(presentation, useHttp = true)
      html shouldNotContain "media=\"screen\""
    }
  }
}
