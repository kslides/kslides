package com.kslides

import com.github.pambrose.common.util.*

class PlaygroundConfig : AbstractConfig() {

  var width by ConfigProperty<String>(managedValues)
  var height by ConfigProperty<String>(managedValues)
  var style by ConfigProperty<String>(managedValues)

  var args by ConfigProperty<String>(unmanagedValues)
  var dataTargetPlatform by ConfigProperty<String>(unmanagedValues)
  var dataHighlightOnly by ConfigProperty<Boolean>(unmanagedValues)
  var foldedButton by ConfigProperty<Boolean>(unmanagedValues)
  var dataJsLibs by ConfigProperty<String>(unmanagedValues)
  var autoIndent by ConfigProperty<Boolean>(unmanagedValues)
  var theme by ConfigProperty<String>(unmanagedValues)
  var mode by ConfigProperty<String>(unmanagedValues)
  var dataMinCompilerVersion by ConfigProperty<String>(unmanagedValues)
  var dataAutocomplete by ConfigProperty<Boolean>(unmanagedValues)
  var highlightOnFly by ConfigProperty<Boolean>(unmanagedValues)
  var indent by ConfigProperty<Int>(unmanagedValues)
  var lines by ConfigProperty<Boolean>(unmanagedValues)
  var from by ConfigProperty<Int>(unmanagedValues)
  var to by ConfigProperty<Int>(unmanagedValues)
  var dataOutputHeight by ConfigProperty<Int>(unmanagedValues)
  var matchBrackets by ConfigProperty<Boolean>(unmanagedValues)
  var dataCrosslink by ConfigProperty<String>(unmanagedValues)
  var dataShorterHeight by ConfigProperty<Int>(unmanagedValues)
  var dataScrollbarStyle by ConfigProperty<String>(unmanagedValues)

  internal fun init() {
    width = ""
    height = ""
    style = ""
  }

  private fun String.toPropertyName() =
    toList()
      .map { if (it.isUpperCase()) "-${it.lowercaseChar()}" else it }
      .joinToString("")

  fun toQueryString() =
    if (unmanagedValues.isNotEmpty())
      unmanagedValues
        .map { (k, v) -> "${k.toPropertyName()}=${v.toString().encode()}" }
        .joinToString("&")
        .let { "&$it" }
    else
      ""
}