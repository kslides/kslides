package com.kslides

import com.kslides.Page.generatePage
import com.kslides.config.SlideConfig
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class SlideFontSizeTest : StringSpec() {
  init {
    "font properties default to unset values" {
      val config = SlideConfig().apply { assignDefaults() }
      config.fontSize shouldBe ""
      config.codeFontSize shouldBe ""
      config.codeWrap shouldBe false
    }

    "font properties cascade global -> presentation -> slide" {
      // Mirrors the merge order in Slide.mergedSlideConfig
      val globalLevel =
        SlideConfig().apply {
          assignDefaults()
          codeFontSize = "0.60em"
          codeWrap = true
        }
      val presentationLevel = SlideConfig().apply { fontSize = "38px" }
      val slideLevel = SlideConfig().apply { codeFontSize = "0.40em" }

      val merged =
        SlideConfig().also { config ->
          config.merge(globalLevel)
          config.merge(presentationLevel)
          config.merge(slideLevel)
        }

      merged.fontSize shouldBe "38px"        // presentation level, untouched by slide
      merged.codeFontSize shouldBe "0.40em"  // slide overrides global
      merged.codeWrap shouldBe true          // inherited from global
    }

    "fontSize renders as inline font-size on the section" {
      val kslides =
        kslidesTest {
          presentation {
            dslSlide {
              slideConfig { fontSize = "34px" }
              content { }
            }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldContain """style="font-size: 34px;""""
    }

    "fontSize combines with user style, user style last" {
      val kslides =
        kslidesTest {
          presentation {
            dslSlide {
              style = "height: 600px"
              slideConfig { fontSize = "34px" }
              content { }
            }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldContain """style="font-size: 34px; height: 600px""""
    }

    "fontSize conflicts with user style are resolved in the user's favor" {
      val kslides =
        kslidesTest {
          presentation {
            dslSlide {
              style = "font-size: 20px"
              slideConfig { fontSize = "34px" }
              content { }
            }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldContain """style="font-size: 34px; font-size: 20px""""
    }

    "user style renders unchanged when fontSize is unset" {
      val kslides =
        kslidesTest {
          presentation {
            dslSlide {
              style = "height: 600px"
              content { }
            }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldContain """style="height: 600px""""
      html shouldNotContain "font-size"
    }

    "presentation-level codeFontSize and codeWrap emit one shared class and head rules" {
      val kslides =
        kslidesTest {
          presentation {
            presentationConfig {
              slideConfig {
                codeFontSize = "0.60em"
                codeWrap = true
              }
            }
            markdownSlide { content { "# One" } }
            markdownSlide { content { "# Two" } }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldContain ".reveal .kslides-code-1 pre { font-size: 0.60em; }"
      html shouldContain ".reveal .kslides-code-1 pre code { white-space: pre-wrap; word-break: break-word; }"
      html shouldNotContain "kslides-code-2"
      // both slides share the single generated class
      Regex("""class="kslides-code-1"""").findAll(html).count() shouldBe 2
    }

    "dslSlide codeFontSize renders the same generated class and head rule as markdownSlide" {
      val kslides =
        kslidesTest {
          presentation {
            dslSlide {
              slideConfig { codeFontSize = "0.45em" }
              content { }
            }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldContain """class="kslides-code-1""""
      html shouldContain ".reveal .kslides-code-1 pre { font-size: 0.45em; }"
    }

    "codeWrap emits a line-number gutter guard so wrapping never reaches the number cells" {
      val kslides =
        kslidesTest {
          presentation {
            presentationConfig {
              slideConfig { codeWrap = true }
            }
            markdownSlide { content { "# Wrapped" } }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldContain ".reveal pre code .hljs-ln-numbers { white-space: nowrap; word-break: normal; }"
    }

    "codeFontSize without codeWrap emits no gutter guard" {
      val kslides =
        kslidesTest {
          presentation {
            presentationConfig {
              slideConfig { codeFontSize = "0.60em" }
            }
            markdownSlide { content { "# Sized" } }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldNotContain "hljs-ln-numbers"
    }

    "slide-level codeFontSize override gets its own class and rule" {
      val kslides =
        kslidesTest {
          presentation {
            presentationConfig {
              slideConfig { codeFontSize = "0.60em" }
            }
            markdownSlide { content { "# Normal" } }
            markdownSlide {
              slideConfig { codeFontSize = "0.40em" }
              content { "# Small" }
            }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldContain ".reveal .kslides-code-1 pre { font-size: 0.60em; }"
      html shouldContain ".reveal .kslides-code-2 pre { font-size: 0.40em; }"
      Regex("""class="kslides-code-1"""").findAll(html).count() shouldBe 1
      Regex("""class="kslides-code-2"""").findAll(html).count() shouldBe 1
    }

    "slide-level codeWrap=false overrides presentation-level true" {
      val kslides =
        kslidesTest {
          presentation {
            presentationConfig {
              slideConfig {
                codeFontSize = "0.60em"
                codeWrap = true
              }
            }
            markdownSlide { content { "# Wrapped" } }
            markdownSlide {
              slideConfig { codeWrap = false }
              content { "# Unwrapped" }
            }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldContain ".reveal .kslides-code-1 pre code { white-space: pre-wrap; word-break: break-word; }"
      html shouldContain ".reveal .kslides-code-2 pre { font-size: 0.60em; }"
      html shouldNotContain ".kslides-code-2 pre code"
    }

    "generated class is appended after user classes" {
      val kslides =
        kslidesTest {
          presentation {
            presentationConfig {
              slideConfig { codeFontSize = "0.60em" }
            }
            markdownSlide {
              classes = "mystyle"
              content { "# Styled" }
            }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldContain """class="mystyle kslides-code-1""""
    }

    "no code styling emits no class or rules" {
      val kslides =
        kslidesTest {
          presentation {
            markdownSlide { content { "# Plain" } }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldNotContain "kslides-code"
    }

    "repeated renders are deterministic (registry cleared per render)" {
      val kslides =
        kslidesTest {
          presentation {
            presentationConfig {
              slideConfig { codeFontSize = "0.60em" }
            }
            markdownSlide { content { "# One" } }
          }
        }
      val p = kslides.presentation("/")
      generatePage(p) shouldBe generatePage(p)
    }

    "slideDefinition configBlock applies per-slide code font size" {
      val kslides =
        kslidesTest {
          presentation {
            slideDefinition(source = "does-not-exist.kt", token = "foo") {
              codeFontSize = "0.40em"
            }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldContain """class="kslides-code-1""""
      html shouldContain ".reveal .kslides-code-1 pre { font-size: 0.40em; }"
    }

    "vertical slideDefinition configBlock applies per-slide code font size" {
      val kslides =
        kslidesTest {
          presentation {
            verticalSlides {
              slideDefinition(source = "does-not-exist.kt", token = "foo") {
                codeFontSize = "0.40em"
              }
            }
          }
        }
      val html = generatePage(kslides.presentation("/"))
      html shouldContain """class="kslides-code-1""""
      html shouldContain ".reveal .kslides-code-1 pre { font-size: 0.40em; }"
    }
  }
}
