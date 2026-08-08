package com.kslides

import com.kslides.Page.generatePage
import com.kslides.config.LogoPosition
import com.kslides.config.ThemeConfig
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlinx.css.Color
import kotlinx.css.TextTransform
import kotlinx.css.px

class ThemeConfigTest : StringSpec() {
  init {
    "an unconfigured ThemeConfig emits nothing" {
      ThemeConfig().cssText() shouldBe ""
    }

    "typed properties map to their reveal.js theme variables" {
      val css =
        ThemeConfig()
          .apply {
            backgroundColor = Color("#f5f5f5")
            mainColor = Color("#1a1a2e")
            linkColorHover = Color("#4a8be2")
            headingTextTransform = TextTransform.none
            headingFontWeight = 600
            heading1Size = 2.px
            codeFont = "JetBrains Mono, monospace"
          }.cssText()

      css shouldContain "--r-background-color: #f5f5f5;"
      css shouldContain "--r-main-color: #1a1a2e;"
      css shouldContain "--r-link-color-hover: #4a8be2;"
      css shouldContain "--r-heading-text-transform: none;"
      css shouldContain "--r-heading-font-weight: 600;"
      css shouldContain "--r-heading1-size: 2px;"
      css shouldContain "--r-code-font: JetBrains Mono, monospace;"
      css shouldContain ":root {"
    }

    "customProperty passes unmodeled variables through and validates the name" {
      val theme = ThemeConfig().apply { customProperty("--r-heading-letter-spacing", "0.05em") }
      theme.cssText() shouldContain "--r-heading-letter-spacing: 0.05em;"

      shouldThrowExactly<IllegalArgumentException> {
        ThemeConfig().customProperty("r-heading-margin", "0")
      }
    }

    "logo() emits corner-pinned CSS and validates its arguments" {
      val css =
        ThemeConfig()
          .apply {
            logo("assets/logo.svg", position = LogoPosition.BOTTOM_LEFT, size = 60.px, opacity = 0.5)
          }.cssText()

      css shouldContain ".kslides-logo {"
      css shouldContain "position: fixed;"
      css shouldContain "bottom: 16px;"
      css shouldContain "left: 16px;"
      css shouldContain "width: 60px;"
      css shouldContain "opacity: 0.5;"
      css shouldContain "pointer-events: none;" // no href -> click-through

      val linked = ThemeConfig().apply { logo("l.png", href = "https://example.com") }.cssText()
      linked shouldNotContain "pointer-events"
      linked shouldNotContain "opacity" // defaults to 1.0 -> omitted

      shouldThrowExactly<IllegalArgumentException> { ThemeConfig().logo(" ") }
      shouldThrowExactly<IllegalArgumentException> { ThemeConfig().logo("l.png", opacity = 1.5) }
    }

    "customTheme cascades global -> presentation with per-property override" {
      val kslides =
        kslidesTest {
          presentationConfig {
            customTheme {
              mainColor = Color("#111111")
              linkColor = Color("#222222")
              customProperty("--r-block-margin", "10px")
              logo("global.png")
            }
          }
          presentation {
            presentationConfig {
              customTheme {
                mainColor = Color("#333333") // overrides global
                customProperty("--r-heading-margin", "0") // merges with global custom property
              }
            }
            markdownSlide { content { "# Themed" } }
          }
        }

      val merged = kslides.presentation("/").finalConfig.customThemeConfig
      val css = merged.cssText()
      css shouldContain "--r-main-color: #333333;"
      css shouldContain "--r-link-color: #222222;"
      css shouldContain "--r-block-margin: 10px;"
      css shouldContain "--r-heading-margin: 0;"
      css shouldContain ".kslides-logo" // logo inherited from the global block
    }

    "baseTheme overrides the presentation theme link and the generated page carries the overrides" {
      val presentation =
        kslidesTest {
          presentation {
            presentationConfig {
              theme = PresentationTheme.BLACK
              customTheme {
                baseTheme = PresentationTheme.WHITE
                mainColor = Color("#1a1a2e")
                logo("assets/logo.svg")
              }
            }
            markdownSlide { content { "# Themed" } }
          }
        }.presentation("/")

      val html = generatePage(presentation, useHttp = true, rootPrefix = "/")

      html shouldContain PresentationTheme.WHITE.cssSrc
      html shouldNotContain PresentationTheme.BLACK.cssSrc
      html shouldContain "id=\"custom-theme\""
      html shouldContain "--r-main-color: #1a1a2e;"
      html shouldContain "class=\"kslides-logo\""

      // The override block must come after the theme stylesheet link to win the cascade.
      html.indexOf("id=\"custom-theme\"") shouldBeGreaterThan html.indexOf(PresentationTheme.WHITE.cssSrc)
    }

    "pages without customTheme carry no custom-theme block or logo" {
      val presentation =
        kslidesTest {
          presentation {
            markdownSlide { content { "# Plain" } }
          }
        }.presentation("/")

      val html = generatePage(presentation, useHttp = true, rootPrefix = "/")
      html shouldNotContain "custom-theme"
      html shouldNotContain "kslides-logo"
    }
  }
}
