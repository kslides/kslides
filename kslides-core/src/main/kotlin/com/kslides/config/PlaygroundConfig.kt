package com.kslides.config

import com.github.pambrose.common.util.*
import com.kslides.*
import kotlin.reflect.full.*

class PlaygroundConfig : AbstractConfig() {

  var width by ConfigProperty<String>(managedValues)
  var height by ConfigProperty<String>(managedValues)
  var style by ConfigProperty<String>(managedValues)
  var title by ConfigProperty<String>(managedValues) // For screen readers

  // These have to be kept in sync with the playgroundAttributes value below
  var args by ConfigProperty<String>(unmanagedValues)
  var dataTargetPlatform by ConfigProperty<TargetPlatform>(unmanagedValues)
  var dataHighlightOnly by ConfigProperty<Boolean>(unmanagedValues)
  var foldedButton by ConfigProperty<Boolean>(unmanagedValues)
  var dataJsLibs by ConfigProperty<String>(unmanagedValues)
  var autoIndent by ConfigProperty<Boolean>(unmanagedValues)
  var theme by ConfigProperty<PlaygroundTheme>(unmanagedValues)
  var mode by ConfigProperty<PlaygroundMode>(unmanagedValues)
  var dataMinCompilerVersion by ConfigProperty<String>(unmanagedValues)
  var dataAutocomplete by ConfigProperty<Boolean>(unmanagedValues)
  var highlightOnFly by ConfigProperty<Boolean>(unmanagedValues)
  var indent by ConfigProperty<Int>(unmanagedValues)
  var lines by ConfigProperty<Boolean>(unmanagedValues)
  var from by ConfigProperty<Int>(unmanagedValues)
  var to by ConfigProperty<Int>(unmanagedValues)
  var dataOutputHeight by ConfigProperty<Int>(unmanagedValues)
  var matchBrackets by ConfigProperty<Boolean>(unmanagedValues)
  var dataCrosslink by ConfigProperty<Crosslink>(unmanagedValues)
  var dataShorterHeight by ConfigProperty<Int>(unmanagedValues)
  var dataScrollbarStyle by ConfigProperty<String>(unmanagedValues)

  fun init() {
    width = ""
    height = ""
    style = ""
    title = ""
  }

  private fun String.toPropertyName() =
    toList()
      .map { if (it.isUpperCase()) "-${it.lowercaseChar()}" else it }
      .joinToString("")

  internal fun toQueryString() =
    if (unmanagedValues.isNotEmpty())
      unmanagedValues
        .map { (k, v) ->
          k to (
              when {
                v is TargetPlatform -> v.queryVal
                v is PlaygroundMode -> v.queryVal
                v::class.isSubclassOf(Enum::class) -> (v as Enum<*>).name.lowercase()
                else -> v
              }
              )
        }
        .joinToString("&") { (k, v) -> "${k.toPropertyName()}=${v.toString().encode()}" }
        .let { "&$it" }
    else
      ""

  companion object {
    val playgroundAttributes =
      listOf(
        "args",
        "data-target-platform",
        "data-highlight-only",
        "folded-button",
        "data-js-libs",
        "auto-indent",
        "theme",
        "mode",
        "data-min-compiler-version",
        "data-autocomplete",
        "highlight-on-fly",
        "indent",
        "lines",
        "from",
        "to",
        "data-output-height",
        "match-brackets",
        "data-crosslink",
        "data-shorter-height",
        "data-scrollbar-style",
      )
  }
}