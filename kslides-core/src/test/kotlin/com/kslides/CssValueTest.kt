package com.kslides

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class CssValueTest : StringSpec() {
  init {
    "fresh CssValue is blank" {
      val css = CssValue()
      css.isNotBlank() shouldBe false
      css.toString() shouldBe ""
    }

    "plusAssign a raw string appends trimmed text with a newline" {
      val css = CssValue()
      css += """
        body { color: red; }
      """
      css.isNotBlank() shouldBe true
      css.toString() shouldContain "body { color: red; }"
    }

    "plusAssign with a CssBuilder block appends generated css" {
      val css = CssValue()
      css += { rule("h1") { } }
      css.isNotBlank() shouldBe true
    }

    "clear() resets the buffer to blank" {
      val css = CssValue()
      css += "x { }"
      css.isNotBlank() shouldBe true
      css.clear()
      css.isNotBlank() shouldBe false
    }

    "an invalid CssValue throws on write" {
      val css = CssValue(valid = false)
      shouldThrowExactly<IllegalArgumentException> { css += "a { }" }
      shouldThrowExactly<IllegalArgumentException> { css += { rule("a") { } } }
    }

    "the vararg constructor concatenates its inputs" {
      val a = CssValue().apply { this += "a { }" }
      val b = CssValue().apply { this += "b { }" }
      val combined = CssValue(a, b)
      combined.toString() shouldContain "a { }"
      combined.toString() shouldContain "b { }"
    }

    "prependIndent prefixes every line with the token" {
      val css = CssValue()
      css += """
        x { }
        y { }
      """
      css
        .prependIndent(">>")
        .lines()
        .filter { it.isNotBlank() }
        .all { it.startsWith(">>") } shouldBe true
    }
  }
}
