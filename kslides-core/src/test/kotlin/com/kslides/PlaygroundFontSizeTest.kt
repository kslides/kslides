package com.kslides

import com.kslides.Page.generatePage
import com.kslides.config.PlaygroundConfig
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldNotStartWith

class PlaygroundFontSizeTest : StringSpec() {
  init {
    fun playgroundConfig(block: PlaygroundConfig.() -> Unit) =
      PlaygroundConfig().apply {
        assignDefaults()
        block()
      }

    "font properties default to unset values" {
      val config = playgroundConfig { }
      config.fontSize shouldBe ""
      config.lineHeight shouldBe ""
      config.cssText() shouldBe ""
    }

    "font properties cascade global -> presentation -> playground" {
      // Mirrors the merge order in the playground() DSL function
      val globalLevel =
        playgroundConfig {
          fontSize = "20px"
          lineHeight = "1.5"
        }
      val presentationLevel = PlaygroundConfig().apply { fontSize = "18px" }
      val playgroundLevel = PlaygroundConfig().apply { fontSize = "15px" }

      val merged =
        PlaygroundConfig().also { config ->
          config.merge(globalLevel)
          config.merge(presentationLevel)
          config.merge(playgroundLevel)
        }

      merged.fontSize shouldBe "15px"    // innermost level wins
      merged.lineHeight shouldBe "1.5"   // inherited from global
    }

    "cssText() sizes the editor and the output pane, and emits no line-height of its own" {
      // CodeMirror's own line-height is a unitless ratio, so spacing already follows the font size.
      val css = playgroundConfig { fontSize = "20px" }.cssText()
      css shouldContain ".CodeMirror { font-size: 20px; }"
      css shouldContain ".code-output { font-size: 20px; }"
      css shouldNotContain "line-height"
    }

    "cssText() targets .CodeMirror rather than its pre lines" {
      // .CodeMirror pre.CodeMirror-line { line-height: inherit } outranks any .CodeMirror pre rule,
      // so declaring line-height on the pre elements would be a silent no-op.
      val css = playgroundConfig { lineHeight = "1.8" }.cssText()
      css shouldContain ".CodeMirror { line-height: 1.8; }"
      css shouldContain ".code-output { line-height: 1.8; }"
      css shouldNotContain "pre"
      css shouldNotContain "font-size"
    }

    "cssText() emits both declarations when both properties are set" {
      val css =
        playgroundConfig {
          fontSize = "15px"
          lineHeight = "20px"
        }.cssText()
      css shouldContain ".CodeMirror { font-size: 15px; line-height: 20px; }"
      css shouldContain ".code-output { font-size: 15px; line-height: 20px; }"
    }

    "stylesheet() adds nothing when neither property is set" {
      // Not even a leading blank line, so decks that do not use fontSize render byte-identically.
      val css = playgroundConfig { }.stylesheet(CssValue(".foo { color: red; }")).toString()
      css.trim() shouldBe ".foo { color: red; }"
      css shouldNotStartWith "\n"
    }

    "playground fontSize renders into the iframe head ahead of user css" {
      val kslides =
        kslidesTest {
          presentationConfig {
            playgroundConfig { fontSize = "20px" }
          }
          presentation {
            dslSlide {
              content {
                playground("does-not-exist.kt") {
                  staticContent = true
                  fontSize = "18px"
                  css += ".CodeMirror { font-size: 99px; }"
                }
              }
            }
          }
        }
      generatePage(kslides.presentation("/"))

      val html = kslides.staticIframeContent.values.single()
      // The playground-level fontSize overrides the presentation-level one...
      html shouldContain ".CodeMirror { font-size: 18px; }"
      html shouldNotContain "font-size: 20px"
      // ...and the generated rules precede the user's css, so the user's rule wins.
      (html.indexOf(".CodeMirror { font-size: 18px; }") < html.indexOf(".CodeMirror { font-size: 99px; }")) shouldBe true
    }

    "malformed CSS lengths are rejected at the assignment site" {
      shouldThrowExactly<IllegalArgumentException> { PlaygroundConfig().fontSize = "20px;}" }
      shouldThrowExactly<IllegalArgumentException> { PlaygroundConfig().lineHeight = "1.25;}" }
      shouldThrowExactly<IllegalArgumentException> { PlaygroundConfig().fontSize = "1.25" }  // needs a unit
    }

    "valid CSS lengths are accepted" {
      shouldNotThrowAny {
        PlaygroundConfig().apply {
          fontSize = "20px"
          fontSize = "0.6em"
          fontSize = "80%"
          fontSize = "0"
          fontSize = ""
          fontSize = "calc(100% - 2px)"
          lineHeight = "1.25"
          lineHeight = "20px"
        }
      }
    }
  }
}
