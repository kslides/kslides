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
  }
}
