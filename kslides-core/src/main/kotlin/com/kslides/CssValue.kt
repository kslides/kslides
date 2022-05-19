package com.kslides

import kotlinx.css.*
import kotlinx.html.*

class CssValue(private var text: String = "", val valid: Boolean = true) {

  constructor(other: CssValue) : this(if (other.isNotBlank()) "$other\n" else "")

  constructor(vararg elems: CssValue) : this(elems.toList().joinToString("\n") { it.text.trimIndent() })

  operator fun plusAssign(other: String) {
    if (!valid) cssError()
    text += "${other.trimIndent()}\n"
  }

  operator fun plusAssign(block: CssBuilder.() -> Unit) {
    if (!valid) cssError()
    text += "\n${CssBuilder().apply(block)}"
  }

  fun isNotBlank() = text.isNotBlank()

  fun prependIndent(indentToken: String) = text.prependIndent(indentToken)

  fun clear() {
    text = ""
  }

  override fun toString() = text

  companion object {
    internal fun cssError(): Nothing =
      throw IllegalArgumentException("css calls must be made in a kslides{} or presentation{} block")

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