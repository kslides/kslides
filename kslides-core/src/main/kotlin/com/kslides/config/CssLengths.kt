package com.kslides.config

// Validators for the length-shaped config properties on SlideConfig and PlaygroundConfig, whose
// values are interpolated verbatim into generated CSS. Without a check, a value like "0.6em;}"
// closes the generated rule early and silently corrupts everything after it. These run from
// ConfigProperty.setValue, so a bad value fails at the assignment site, named.
//
// Not covered: ThemeConfig's font properties and customProperty() values also land raw inside a
// <style> body, but they are colors/font stacks/arbitrary values that no shape regex fits — they
// need a containment check (reject ; { } <) rather than these. Still an open gap.
// Properties that render into HTML *attributes* (style=, data-background-*) are a different case
// and deliberately unvalidated: kotlinx.html escapes them, and the blast radius is one element.

private const val CSS_NUMBER = "[+-]?(\\d+(\\.\\d*)?|\\.\\d+)"

private const val CSS_UNITS = "px|em|rem|ex|ch|vw|vh|vmin|vmax|cm|mm|in|pt|pc|q|%"

// calc()/var()/clamp() values pass through as long as they cannot break out of the declaration.
private const val CSS_FUNCTION = "(calc|var|clamp|min|max)\\([^;{}<>]*\\)"

// A length needs a unit, except for a bare 0; a line-height may also be a unitless ratio.
private val cssLengthRegex = Regex("^(0|$CSS_NUMBER($CSS_UNITS)|$CSS_FUNCTION)$", RegexOption.IGNORE_CASE)

private val cssLineHeightRegex = Regex("^($CSS_NUMBER($CSS_UNITS)?|$CSS_FUNCTION)$", RegexOption.IGNORE_CASE)

/**
 * Require [value] to be a CSS length (`"34px"`, `"0.6em"`, `"80%"`, a bare `0`, or a
 * `calc()`/`var()`/`clamp()`/`min()`/`max()` expression). Blank is allowed — it is how the config
 * cascade spells "unset".
 *
 * @throws IllegalArgumentException if [value] is neither blank nor a valid CSS length.
 */
internal fun requireCssLength(
  value: String,
  propertyName: String,
) = require(value.isBlank() || cssLengthRegex.matches(value.trim())) {
  "$propertyName is not a valid CSS length: \"$value\""
}

/**
 * Require [value] to be a CSS `line-height` — a unitless ratio (`"1.25"`) in addition to everything
 * [requireCssLength] accepts. Blank is allowed.
 *
 * @throws IllegalArgumentException if [value] is neither blank nor a valid `line-height`.
 */
internal fun requireCssLineHeight(
  value: String,
  propertyName: String,
) = require(value.isBlank() || cssLineHeightRegex.matches(value.trim())) {
  "$propertyName is not a valid CSS line-height: \"$value\""
}
