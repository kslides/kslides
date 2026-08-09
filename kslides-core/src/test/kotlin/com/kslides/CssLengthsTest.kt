package com.kslides

import com.kslides.config.PlaygroundConfig
import com.kslides.config.SlideConfig
import com.kslides.config.ThemeConfig
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

/**
 * The length validators behind `slideConfig`'s `fontSize`/`codeFontSize` and `playgroundConfig`'s
 * `fontSize`/`lineHeight`. Their values are interpolated verbatim into generated CSS, so the
 * grammar is the whole defence — `SlideFontSizeTest` and `PlaygroundFontSizeTest` cover what the
 * properties *do*, this covers what they accept.
 *
 * Exercised through the properties rather than the internal functions directly, since being wired
 * to the assignment site is half of what makes them work.
 */
class CssLengthsTest : StringSpec() {
  init {
    "every unit in the grammar is accepted" {
      // Whatever the regex lists, a caller can use — otherwise a legal value fails at runtime for
      // someone whose deck is sized in units this suite never tried.
      listOf(
        "10px",
        "1.5em",
        "2rem",
        "3ex",
        "4ch",
        "5vw",
        "6vh",
        "7vmin",
        "8vmax",
        "1cm",
        "10mm",
        "2in",
        "12pt",
        "1pc",
        "3q",
        "80%",
      ).forEach { value ->
        withClue(value) { shouldNotThrowAny { SlideConfig().fontSize = value } }
      }
    }

    "a bare 0 needs no unit, but any other bare number does" {
      shouldNotThrowAny { SlideConfig().fontSize = "0" }
      // A unitless length is the classic CSS mistake — it is not "16 of something", it is invalid.
      shouldThrowExactly<IllegalArgumentException> { SlideConfig().fontSize = "16" }
    }

    "signs and leading-dot decimals parse" {
      listOf("+2px", "-2px", ".5em", "0.5em").forEach { value ->
        withClue(value) { shouldNotThrowAny { SlideConfig().fontSize = value } }
      }
    }

    "the CSS functions pass through, including nested ones" {
      listOf(
        "calc(100% - 2px)",
        "var(--r-main-font-size)",
        "var(--gap, 1rem)",
        "clamp(1rem, 2vw, 3rem)",
        "min(50%, 400px)",
        "max(1em, 12px)",
        "calc(var(--gap) * 2)",
      ).forEach { value ->
        withClue(value) { shouldNotThrowAny { SlideConfig().fontSize = value } }
      }
    }

    "a function cannot smuggle a declaration break past the guard" {
      // The point of the whole validator: these are what turn one property into a broken stylesheet.
      listOf(
        "calc(100% - 2px);}",
        "calc(1px); color: red",
        "var(--x){}",
        "calc(1px)</style>",
      ).forEach { value ->
        withClue(value) { shouldThrowExactly<IllegalArgumentException> { SlideConfig().fontSize = value } }
      }
    }

    "a value that merely contains a length is still rejected" {
      // Anchored matching, not a search — "20px" appearing anywhere must not save the value.
      listOf("20px; color: red", "20px }", "expression(20px)", "20 px").forEach { value ->
        withClue(value) { shouldThrowExactly<IllegalArgumentException> { SlideConfig().codeFontSize = value } }
      }
    }

    "blank is how the cascade spells unset, at every level" {
      shouldNotThrowAny {
        SlideConfig().fontSize = ""
        SlideConfig().codeFontSize = ""
        PlaygroundConfig().fontSize = ""
        PlaygroundConfig().lineHeight = ""
      }
    }

    "surrounding whitespace is tolerated" {
      shouldNotThrowAny { SlideConfig().fontSize = "  20px  " }
    }

    "line-height also takes a unitless ratio, which a plain length does not" {
      shouldNotThrowAny { PlaygroundConfig().lineHeight = "1.25" }
      shouldNotThrowAny { PlaygroundConfig().lineHeight = "20px" }
      // Same value, different property — the two validators are genuinely different grammars.
      shouldThrowExactly<IllegalArgumentException> { PlaygroundConfig().fontSize = "1.25" }
      shouldThrowExactly<IllegalArgumentException> { PlaygroundConfig().lineHeight = "1.25;}" }
    }

    "the failure names the property and shows the value" {
      // The value is somewhere in a deck definition; a message that says neither is a scavenger hunt.
      val e = shouldThrowExactly<IllegalArgumentException> { SlideConfig().codeFontSize = "0.6em;}" }
      e.message!! shouldContain "codeFontSize"
      e.message!! shouldContain "0.6em;}"

      val lh = shouldThrowExactly<IllegalArgumentException> { PlaygroundConfig().lineHeight = "junk" }
      lh.message!! shouldContain "lineHeight"
    }

    "a theme value cannot break out of the declaration it is written into" {
      // The generated block is ":root { --r-x: VALUE; }", so a value carrying its own ";}" used to
      // close the rule and inject another -- silently changing a page the author never edited.
      shouldThrowExactly<IllegalArgumentException> {
        ThemeConfig().customProperty("--r-x", "red; } .reveal h1 { color: lime")
      }
      // The name is emitted verbatim too, so it needs the same check.
      shouldThrowExactly<IllegalArgumentException> { ThemeConfig().customProperty("--x; } h1 { color: red", "1") }
      // "</style>" would leave the element altogether; a comment delimiter would swallow the rest.
      shouldThrowExactly<IllegalArgumentException> { ThemeConfig().mainFont = "Menlo</style>" }
      shouldThrowExactly<IllegalArgumentException> { ThemeConfig().codeFont = "Menlo /* x" }
    }

    "ordinary theme values are untouched" {
      shouldNotThrowAny {
        ThemeConfig().apply {
          mainFont = "Menlo, Consolas, 'Courier New', monospace"
          headingFont = ""
          customProperty("--r-heading-letter-spacing", "0.05em")
          customProperty("--r-block-margin", "calc(1rem + 2px)")
          customProperty("--r-link-color", "rgba(0, 0, 0, 0.5)")
        }
      }
    }

    // The message has to name which property and show the value: the author is looking at a
    // customTheme block, not a stack trace.
    "the failure names the property and the character that would have escaped" {
      val e = shouldThrowExactly<IllegalArgumentException> { ThemeConfig().mainFont = "Menlo;}" }
      e.message!! shouldContain "mainFont"
      e.message!! shouldContain "\";\""
    }

    "a rejected value never reaches the config, so the cascade still sees it as unset" {
      // The validator runs before the write, so a caught IllegalArgumentException leaves no
      // half-applied state behind — the property still reads as the default the cascade merges on.
      val config = SlideConfig().apply { assignDefaults() }
      shouldThrowExactly<IllegalArgumentException> { config.fontSize = "20px;}" }
      config.fontSize shouldBe ""

      // And a good value assigned afterwards still lands, so the failure is not sticky.
      config.fontSize = "34px"
      config.fontSize shouldBe "34px"
    }
  }
}
