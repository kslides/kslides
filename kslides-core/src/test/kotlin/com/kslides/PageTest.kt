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
      // reveal.js reads the template as raw text, so markdown must see exactly what was typed.
      md shouldContain "# Tom & Jerry"
      md shouldContain "\u00A0"    // &nbsp; arrives as the character it names
      md shouldContain "—"          // &mdash; likewise

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
