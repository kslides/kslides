package com.kslides

import kotlinx.css.*

class CssValue(private var text: String = "", val valid: Boolean = true) {

  constructor(other: CssValue) : this(if (other.isNotBlank()) "$other\n" else "")

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

  override fun toString() = text

  companion object {
    fun cssError(): Nothing =
      throw IllegalArgumentException("css calls must be made in a kslides{} or presentation{} section")
  }
}