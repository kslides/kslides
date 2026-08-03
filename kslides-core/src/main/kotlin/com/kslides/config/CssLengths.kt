package com.kslides.config

// Validators for config properties whose values are interpolated verbatim into generated CSS —
// inline style attributes (SlideConfig.fontSize), head rules (SlideConfig.codeFontSize) and the
// Playground iframe stylesheet (PlaygroundConfig.fontSize/lineHeight). Without a check, a value
// like "0.6em;}" closes the generated rule early and silently corrupts everything after it. These
// run from ConfigProperty.setValue, so a bad value fails at the assignment site, named.

private val cssUnits =
  listOf("px", "em", "rem", "ex", "ch", "vw", "vh", "vmin", "vmax", "cm", "mm", "in", "pt", "pc", "q", "%")

private const val CSS_NUMBER = "[+-]?(\\d+(\\.\\d*)?|\\.\\d+)"

private val cssLengthRegex = Regex("^$CSS_NUMBER(${cssUnits.joinToString("|")})$", RegexOption.IGNORE_CASE)

private val cssNumberRegex = Regex("^$CSS_NUMBER$")

// calc()/var()/clamp() values pass through as long as they cannot break out of the declaration.
private val cssFunctionRegex = Regex("^(calc|var|clamp|min|max)\\([^;{}<>]*\\)$", RegexOption.IGNORE_CASE)

private fun String.isCssLength() = cssLengthRegex.matches(this) || this == "0" || cssFunctionRegex.matches(this)

/**
 * Require [value] to be a CSS length (`"34px"`, `"0.6em"`, `"80%"`, a bare `0`, or a
 * `calc()`/`var()`/`clamp()`/`min()`/`max()` expression). Blank is always allowed — it is how the
 * config cascade spells "unset".
 *
 * @throws IllegalArgumentException if [value] is neither blank nor a valid CSS length.
 */
internal fun requireCssLength(
  value: String,
  propertyName: String,
) {
  require(value.isBlank() || value.trim().isCssLength()) {
    "$propertyName is not a valid CSS length: \"$value\""
  }
}

/**
 * Require [value] to be a CSS `line-height` — a unitless ratio (`"1.25"`) in addition to everything
 * [requireCssLength] accepts. Blank is always allowed.
 *
 * @throws IllegalArgumentException if [value] is neither blank nor a valid `line-height`.
 */
internal fun requireCssLineHeight(
  value: String,
  propertyName: String,
) {
  val trimmed = value.trim()
  require(value.isBlank() || cssNumberRegex.matches(trimmed) || trimmed.isCssLength()) {
    "$propertyName is not a valid CSS line-height: \"$value\""
  }
}
