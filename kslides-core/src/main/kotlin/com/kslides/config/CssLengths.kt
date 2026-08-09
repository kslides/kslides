package com.kslides.config

// Validators for the length-shaped config properties on SlideConfig and PlaygroundConfig, whose
// values are interpolated verbatim into generated CSS. Without a check, a value like "0.6em;}"
// closes the generated rule early and silently corrupts everything after it. These run from
// ConfigProperty.setValue, so a bad value fails at the assignment site, named.
//
// ThemeConfig's font properties and customProperty() values land raw inside a <style> body too,
// but they are font stacks and arbitrary theme values that no shape regex fits, so they get a
// containment check instead — see requireCssValue.
//
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

// What a value cannot contain without escaping the declaration it sits in: ";" starts another,
// "{"/"}" open or close a rule, "<" can spell "</style>" and leave the element altogether, and a
// comment delimiter can swallow every rule that follows.
private val cssValueBreakouts = listOf(";", "{", "}", "<", "/*", "*/")

/**
 * Require [value] to stay inside the CSS declaration kslides builds around it. Where
 * [requireCssLength] can describe the whole shape of what it accepts, this cannot — a font stack or
 * an arbitrary `--r-*` value has no fixed form — so it checks only that the value cannot break out.
 *
 * A value like `"red; } .reveal h1 { color: lime"` would otherwise close the generated rule and
 * inject its own, silently, changing a page the author never edited. Blank is allowed: it is how
 * the config cascade spells "unset".
 *
 * The cost is that a value legitimately containing one of these characters — a quoted `content`
 * string with a semicolon, say — is rejected too. Reveal's `--r-*` variables are colors, fonts and
 * lengths, so that trade is one-sided in practice.
 *
 * @throws IllegalArgumentException if [value] contains a character that would end the declaration.
 */
internal fun requireCssValue(
  value: String,
  propertyName: String,
) {
  val found = cssValueBreakouts.firstOrNull { it in value }
  require(found == null) {
    "$propertyName may not contain \"$found\", which would end the CSS declaration it is written " +
      "into: \"$value\""
  }
}
