package com.kslides.config

import com.kslides.KSlidesDslMarker
import com.kslides.PresentationTheme
import kotlinx.css.Color
import kotlinx.css.LinearDimension
import kotlinx.css.TextTransform
import kotlinx.css.hyphenize
import kotlinx.css.px

/** Corner of the viewport a [ThemeConfig.logo] is pinned to. */
enum class LogoPosition {
  TOP_LEFT,
  TOP_RIGHT,
  BOTTOM_LEFT,
  BOTTOM_RIGHT,
}

/** A brand logo pinned to one corner of every slide. Created via [ThemeConfig.logo]. */
internal data class Logo(
  val src: String,
  val position: LogoPosition,
  val size: LinearDimension,
  val margin: LinearDimension,
  val opacity: Double,
  val href: String,
) {
  /** The rules pinning this logo to its corner, emitted as part of [ThemeConfig.cssText]. */
  fun css() =
    buildString {
      val (vertical, horizontal) =
        when (position) {
          LogoPosition.TOP_LEFT -> "top" to "left"
          LogoPosition.TOP_RIGHT -> "top" to "right"
          LogoPosition.BOTTOM_LEFT -> "bottom" to "left"
          LogoPosition.BOTTOM_RIGHT -> "bottom" to "right"
        }
      appendLine(".kslides-logo {")
      // Fixed positioning keeps the logo pinned in scroll view and repeats it on every page
      // when printing (reveal.js print view lays all slides out in one document flow). The
      // z-index must clear the print view's .pdf-page stacking contexts (z-index 1), whose
      // opaque backgrounds would otherwise paint over the logo, while staying below reveal's
      // progress bar (10) and navigation controls (11).
      appendLine("  position: fixed;")
      appendLine("  $vertical: $margin;")
      appendLine("  $horizontal: $margin;")
      appendLine("  width: $size;")
      appendLine("  z-index: 5;")
      if (opacity < 1.0)
        appendLine("  opacity: $opacity;")
      if (href.isBlank())
        appendLine("  pointer-events: none;")
      appendLine("}")
      appendLine(".kslides-logo img {")
      appendLine("  width: 100%;")
      appendLine("  display: block;")
      append("}")
    }
}

/**
 * Type-safe theming on top of a stock reveal.js theme, configured via the `customTheme {}` block
 * inside [PresentationConfig] (globally or per presentation, cascading like every other config).
 *
 * Each typed property maps to one of the CSS custom properties (`--r-*`) that reveal.js themes
 * expose; only the properties actually assigned are emitted, as a `<style id="custom-theme">`
 * override block layered after the base theme's stylesheet. No SCSS compilation is involved.
 *
 * ```kotlin
 * presentationConfig {
 *   customTheme {
 *     baseTheme = PresentationTheme.WHITE
 *     mainColor = Color("#1a1a2e")
 *     headingTextTransform = TextTransform.none
 *     logo("assets/logo.svg", position = LogoPosition.TOP_RIGHT, size = 80.px)
 *   }
 * }
 * ```
 *
 * For the reveal.js theme variables the DSL does not model (`--r-heading-margin`,
 * `--r-heading1-text-shadow`, …), use the [customProperty] passthrough.
 */
@KSlidesDslMarker
class ThemeConfig : AbstractConfig() {
  // Every ConfigProperty below maps 1:1 to a reveal.js theme variable: the map key (the Kotlin
  // property name) is converted to kebab-case and prefixed with "--r-" at emission time, so the
  // property names must track the reveal.js variable names exactly.

  /** Slide background color (`--r-background-color`). */
  var backgroundColor by ConfigProperty<Color>(kslidesManagedValues)

  /** Body text color (`--r-main-color`). */
  var mainColor by ConfigProperty<Color>(kslidesManagedValues)

  /** Body font stack (`--r-main-font`), e.g. `"Inter, sans-serif"`. */
  var mainFont by ConfigProperty<String>(kslidesManagedValues)

  /** Base font size (`--r-main-font-size`); reveal.js stock themes use `42.px`. */
  var mainFontSize by ConfigProperty<LinearDimension>(kslidesManagedValues)

  /** Heading text color (`--r-heading-color`). */
  var headingColor by ConfigProperty<Color>(kslidesManagedValues)

  /** Heading font stack (`--r-heading-font`). */
  var headingFont by ConfigProperty<String>(kslidesManagedValues)

  /**
   * Heading text transform (`--r-heading-text-transform`). Most stock themes default to
   * `uppercase`; set [TextTransform.none] to keep headings as written.
   */
  var headingTextTransform by ConfigProperty<TextTransform>(kslidesManagedValues)

  /** Heading font weight (`--r-heading-font-weight`), e.g. `600`. */
  var headingFontWeight by ConfigProperty<Int>(kslidesManagedValues)

  /** `<h1>` size (`--r-heading1-size`); stock themes use em values, e.g. `2.5.em`. */
  var heading1Size by ConfigProperty<LinearDimension>(kslidesManagedValues)

  /** `<h2>` size (`--r-heading2-size`). */
  var heading2Size by ConfigProperty<LinearDimension>(kslidesManagedValues)

  /** `<h3>` size (`--r-heading3-size`). */
  var heading3Size by ConfigProperty<LinearDimension>(kslidesManagedValues)

  /** `<h4>` size (`--r-heading4-size`). */
  var heading4Size by ConfigProperty<LinearDimension>(kslidesManagedValues)

  /** Code font stack (`--r-code-font`), e.g. `"JetBrains Mono, monospace"`. */
  var codeFont by ConfigProperty<String>(kslidesManagedValues)

  /** Link color (`--r-link-color`). */
  var linkColor by ConfigProperty<Color>(kslidesManagedValues)

  /** Link hover color (`--r-link-color-hover`). */
  var linkColorHover by ConfigProperty<Color>(kslidesManagedValues)

  /** Text-selection foreground color (`--r-selection-color`). */
  var selectionColor by ConfigProperty<Color>(kslidesManagedValues)

  /** Text-selection background color (`--r-selection-background-color`). */
  var selectionBackgroundColor by ConfigProperty<Color>(kslidesManagedValues)

  /** Vertical margin between block elements (`--r-block-margin`); stock themes use `20.px`. */
  var blockMargin by ConfigProperty<LinearDimension>(kslidesManagedValues)

  /**
   * The stock reveal.js theme these overrides layer on top of. When set, it takes precedence
   * over [PresentationConfig.theme] for this presentation (and drives dark/light-derived
   * behavior such as the Mermaid theme).
   */
  var baseTheme: PresentationTheme? = null

  internal var logoValue: Logo? = null

  internal val customProperties = LinkedHashMap<String, String>()

  /**
   * Pin a brand logo to a corner of every slide.
   *
   * @param src image URL. A relative value resolves against the output root, so the same path
   *   works from a deck at any depth; absolute (`/img/logo.png`), external (`https://...`), and
   *   `data:` values pass through untouched.
   * @param position which corner ([LogoPosition.TOP_RIGHT] by default).
   * @param size rendered width; height follows the image's aspect ratio.
   * @param margin distance from the two adjacent viewport edges.
   * @param opacity 0.0–1.0; useful for watermark-style logos.
   * @param href optional link target; when blank the logo ignores pointer events entirely. Unlike
   *   [src], it is emitted as written — a navigation target is left alone.
   */
  fun logo(
    src: String,
    position: LogoPosition = LogoPosition.TOP_RIGHT,
    size: LinearDimension = 80.px,
    margin: LinearDimension = 16.px,
    opacity: Double = 1.0,
    href: String = "",
  ) {
    require(src.isNotBlank()) { "logo() requires a non-blank src" }
    require(opacity in 0.0..1.0) { "logo() opacity must be between 0.0 and 1.0: $opacity" }
    logoValue = Logo(src, position, size, margin, opacity, href)
  }

  /**
   * Set a raw reveal.js theme variable the DSL does not model, e.g.
   * `customProperty("--r-heading-letter-spacing", "0.05em")`. Values are emitted verbatim into
   * the override block and cascade per property name like the typed ones.
   */
  fun customProperty(
    name: String,
    value: String,
  ) {
    require(name.startsWith("--")) { "customProperty() name must start with \"--\": $name" }
    customProperties[name] = value
  }

  override fun merge(other: AbstractConfig) {
    super.merge(other)
    if (other is ThemeConfig) {
      customProperties.putAll(other.customProperties)
      other.baseTheme?.let { baseTheme = it }
      other.logoValue?.let { logoValue = it }
    }
  }

  /**
   * The override stylesheet for everything assigned on this config; empty when nothing is set.
   * Rendered once per presentation — see [com.kslides.Presentation.indentedCustomThemeCss].
   */
  internal fun cssText() =
    buildString {
      val variables =
        kslidesManagedValues.map { (name, value) -> "--r-${name.hyphenize()}" to value.toString() } +
          customProperties.toList()

      if (variables.isNotEmpty()) {
        appendLine(":root {")
        variables.forEach { (name, value) -> appendLine("  $name: $value;") }
        appendLine("}")
      }

      logoValue?.let { append(it.css()) }
    }.trimEnd()
}
