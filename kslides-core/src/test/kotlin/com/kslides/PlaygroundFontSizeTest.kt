package com.kslides

import com.kslides.config.PlaygroundConfig
import com.kslides.config.SlideConfig
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlinx.html.h2
import java.io.File
import java.nio.file.Files

class PlaygroundFontSizeTest : StringSpec() {
  init {
    "font properties default to unset values" {
      val config = PlaygroundConfig().apply { assignDefaults() }
      config.fontSize shouldBe ""
      config.lineHeight shouldBe ""
      config.fontSizeCss() shouldBe ""
    }

    "font properties cascade global -> presentation -> playground" {
      // Mirrors the merge order in the playground() DSL function
      val globalLevel =
        PlaygroundConfig().apply {
          assignDefaults()
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

    "fontSizeCss() sizes the editor and the output pane, and emits no line-height of its own" {
      // CodeMirror's own line-height is a unitless ratio, so spacing already follows the font size.
      val css = PlaygroundConfig().apply { assignDefaults() }.also { it.fontSize = "20px" }.fontSizeCss()
      css shouldContain ".CodeMirror { font-size: 20px; }"
      css shouldContain ".code-output { font-size: 20px; }"
      css shouldNotContain "line-height"
    }

    "fontSizeCss() targets .CodeMirror rather than its pre lines" {
      // .CodeMirror pre.CodeMirror-line { line-height: inherit } outranks any .CodeMirror pre rule,
      // so declaring line-height on the pre elements would be a silent no-op.
      val css = PlaygroundConfig().apply { assignDefaults() }.also { it.lineHeight = "1.8" }.fontSizeCss()
      css shouldContain ".CodeMirror { line-height: 1.8; }"
      css shouldNotContain "pre"
    }

    "fontSizeCss() emits both declarations when both properties are set" {
      val css =
        PlaygroundConfig()
          .apply {
            assignDefaults()
            fontSize = "15px"
            lineHeight = "20px"
          }.fontSizeCss()
      css shouldContain ".CodeMirror { font-size: 15px; line-height: 20px; }"
      css shouldContain ".code-output { font-size: 15px; line-height: 20px; }"
    }

    "fontSizeCss() emits only line-height when fontSize is blank" {
      val css = PlaygroundConfig().apply { assignDefaults() }.also { it.lineHeight = "1.8" }.fontSizeCss()
      css shouldContain ".code-output { line-height: 1.8; }"
      css shouldNotContain "font-size"
    }

    "playground fontSize renders into the iframe head ahead of user css" {
      val workDir = File(System.getProperty("user.dir"))
      val srcFile = File.createTempFile("kslides-playground", ".kt", workDir)
      val tmpDir = Files.createTempDirectory("kslides-playground-test").toFile()
      try {
        srcFile.writeText("fun main() { println(\"hi\") }\n")

        kslides {
          output {
            enableFileSystem = true
            enableHttp = false
            outputDir = tmpDir.absolutePath
          }
          presentationConfig {
            playgroundConfig { fontSize = "20px" }
          }
          presentation {
            dslSlide {
              content {
                h2 { +"Playground" }
                playground(srcFile.name) {
                  fontSize = "18px"
                  css += ".CodeMirror { font-size: 99px; }"
                }
              }
            }
          }
        }

        val iframes = File(tmpDir, "playground").listFiles { f -> f.extension == "html" }.orEmpty().toList()
        iframes.size shouldBe 1

        val html = iframes.single().readText()
        // The playground-level fontSize overrides the presentation-level one...
        html shouldContain ".CodeMirror { font-size: 18px; }"
        html shouldNotContain "font-size: 20px"
        // ...and the generated rules precede the user's css, so the user's rule wins.
        (html.indexOf(".CodeMirror { font-size: 18px; }") < html.indexOf(".CodeMirror { font-size: 99px; }")) shouldBe true
      } finally {
        srcFile.delete()
        tmpDir.deleteRecursively()
      }
    }

    "malformed CSS lengths are rejected at the assignment site" {
      shouldThrowExactly<IllegalArgumentException> { PlaygroundConfig().fontSize = "20px;}" }
      shouldThrowExactly<IllegalArgumentException> { PlaygroundConfig().lineHeight = "1.25;}" }
      shouldThrowExactly<IllegalArgumentException> { PlaygroundConfig().fontSize = "1.25" }  // needs a unit
      shouldThrowExactly<IllegalArgumentException> { SlideConfig().fontSize = "34px; color: red" }
      shouldThrowExactly<IllegalArgumentException> { SlideConfig().codeFontSize = "0.6em;}" }
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
        SlideConfig().apply {
          fontSize = "34px"
          codeFontSize = "0.60em"
        }
      }
    }
  }
}
