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

    "the viewport lets a viewer zoom" {
      // Blocking pinch-zoom fails WCAG 2.1 SC 1.4.4. reveal.js's own template still ships
      // maximum-scale/user-scalable=no; kslides deliberately does not.
      val html = Page.generatePage(kslidesTest { presentation { markdownSlide { content { "# Hi" } } } }.presentation("/"))
      html shouldContain """<meta content="width=device-width, initial-scale=1.0" name="viewport">"""
      html shouldNotContain "user-scalable"
      html shouldNotContain "maximum-scale"
      // reveal.js 3-era carry-over: upstream dropped these, and Apple deprecated the first.
      html shouldNotContain "apple-mobile-web-app"
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
