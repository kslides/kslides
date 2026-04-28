package com.kslides

import kotlinx.css.CssBuilder
import kotlinx.html.HEAD
import kotlinx.html.style

/**
 * Accumulator for CSS content attached to a [com.kslides.KSlides] or [com.kslides.Presentation].
 * Supports both raw-string and Kotlin CSS-DSL input via `+=`, and enforces that CSS is only
 * declared at scopes that actually render a `<style>` tag.
 *
 * @property valid when `false`, any `+=` or `css{}` call throws. Used by [com.kslides.slide.Slide]
 *   and [com.kslides.VerticalSlidesContext] to expose a CSS receiver that deliberately fails,
 *   catching the common mistake of trying to scope CSS per slide.
 */
class CssValue(
  private var text: String = "",
  val valid: Boolean = true,
) {
  /** Copy constructor — produces a new, writable [CssValue] seeded with [other]'s text. */
  constructor(other: CssValue) : this(if (other.isNotBlank()) "$other\n" else "")

  /** Combine multiple [CssValue] instances (each trimmed) into one. */
  constructor(vararg elems: CssValue) : this(elems.toList().joinToString("\n") { it.text.trimIndent() })

  /**
   * Append a raw CSS string. Leading indentation is trimmed so multi-line string literals do
   * not produce unwanted whitespace.
   *
   * @throws IllegalArgumentException if this [CssValue] is marked [valid] = `false`.
   */
  operator fun plusAssign(other: String) {
    if (!valid) cssError()
    text += "${other.trimIndent()}\n"
  }

  /**
   * Append CSS built via the Kotlin CSS DSL.
   *
   * @throws IllegalArgumentException if this [CssValue] is marked [valid] = `false`.
   */
  operator fun plusAssign(block: CssBuilder.() -> Unit) {
    if (!valid) cssError()
    text += "\n${CssBuilder().apply(block)}"
  }

  /** `true` if any CSS has been accumulated. */
  fun isNotBlank() = text.isNotBlank()

  /** Return the accumulated CSS with [indentToken] prefixed to every line. Used for pretty output. */
  fun prependIndent(indentToken: String) = text.prependIndent(indentToken)

  /** Remove all accumulated CSS. */
  fun clear() {
    text = ""
  }

  /** The raw accumulated CSS text. */
  override fun toString() = text

  companion object {
    internal fun cssError(): Nothing = throw IllegalArgumentException("css calls must be made in a kslides{} or presentation{} block")

    internal fun HEAD.writeCssToHead(css: CssValue) {
      if (css.isNotBlank()) {
        rawHtml("\n")
        style("text/css") {
          media = "screen"
          rawHtml("\n")
          rawHtml(css.prependIndent("\t\t\t"))
          rawHtml("\n\t\t")
        }
        rawHtml("\n")
      }
    }
  }
}
