package com.kslides.config

class CodeSnippetConfig {
  var code = ""
  var language: String = "kotlin"
  var highlightPattern: String = "" // "" turns on all lines, "none" turns off line numbers
  var lineOffSet: Int = -1
  var dataId: String = ""           // For animation
  var trim: Boolean = true
  var escapeHtml: Boolean = false
  var copyButton: Boolean = true    // Adds COPY button
  var copyButtonText: String = ""
  var copyButtonMsg: String = ""

  operator fun String.unaryPlus() {
    code += this
  }
}