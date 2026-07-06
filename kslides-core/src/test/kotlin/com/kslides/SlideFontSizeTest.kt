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
  }
}
