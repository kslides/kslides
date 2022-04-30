package com.kslides

class AppendableString(private var text: String = "") {

  operator fun plusAssign(other: String) {
    text += other
  }

  fun isNotBlank() = text.isNotBlank()

  fun prependIndent(indentToken: String) = text.indentFirstLine(indentToken)

  fun clear() {
    text = ""
  }

  override fun toString() = text
}