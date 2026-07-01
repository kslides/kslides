package com.kslides

import com.kslides.Page.generatePage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlinx.html.h2

class SlideRenderingTest : StringSpec() {
  private fun renderDeck(): String {
    val kslides =
      kslidesTest {
        presentation {
          markdownSlide {
            classes = "mdh"
            content { "# Markdown Horizontal" }
          }
          htmlSlide {
            classes = "htmlh"
            content { "<h2>HTML Horizontal</h2>" }
          }
          dslSlide {
            classes = "dslh"
            content { h2 { +"DSL Horizontal" } }
          }
          verticalSlides {
            markdownSlide {
              classes = "mdv"
              content { "# Markdown Vertical" }
            }
            htmlSlide {
              classes = "htmlv"
              content { "<h2>HTML Vertical</h2>" }
            }
            dslSlide {
              classes = "dslv"
              content { h2 { +"DSL Vertical" } }
            }
          }
        }
      }
    return generatePage(kslides.presentation("/"))
  }

  init {
    // The horizontal/vertical slide functions share renderMarkdownSlide/renderDslSlide/
    // renderHtmlSlide; this pins the section markup each emits (the refactor was verified
    // byte-identical against a pre-refactor render).
    "all six slide functions emit the expected <section> markup" {
      val html = renderDeck()

      // Markdown: horizontal applies the markdown separators from config; vertical zeroes the
      // horizontal separators out and forwards the notes separator.
      html shouldContain """<section class="mdh" data-markdown="">"""
      html shouldContain """<section class="mdv" data-markdown="" data-separator="" data-separator-notes="^Notes?:" data-separator-vertical="">"""
      // A horizontal markdown slide does not carry the vertical-only zeroed separators.
      html shouldNotContain """<section class="mdh" data-markdown="" data-separator"""

      html shouldContain """<section class="htmlh">"""
      html shouldContain """<section class="htmlv">"""
      html shouldContain """<section class="dslh">"""
      html shouldContain """<section class="dslv">"""
    }
  }
}
