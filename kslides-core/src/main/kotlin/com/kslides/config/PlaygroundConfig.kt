package com.kslides.config

import com.github.pambrose.common.util.*
import com.kslides.*
import kotlin.reflect.full.*

class PlaygroundConfig : AbstractConfig() {

  // These have to be kept in sync with the playgroundAttributes value below
  var args by ConfigProperty<String>(revealjsManagedValues)
  var dataTargetPlatform by ConfigProperty<TargetPlatform>(revealjsManagedValues)
  var dataHighlightOnly by ConfigProperty<Boolean>(revealjsManagedValues)
  var foldedButton by ConfigProperty<Boolean>(revealjsManagedValues)
  var dataJsLibs by ConfigProperty<String>(revealjsManagedValues)
  var autoIndent by ConfigProperty<Boolean>(revealjsManagedValues)
  var theme by ConfigProperty<PlaygroundTheme>(revealjsManagedValues)
  var mode by ConfigProperty<PlaygroundMode>(revealjsManagedValues)
  var dataMinCompilerVersion by ConfigProperty<String>(revealjsManagedValues)
  var dataAutocomplete by ConfigProperty<Boolean>(revealjsManagedValues)
  var highlightOnFly by ConfigProperty<Boolean>(revealjsManagedValues)
  var indent by ConfigProperty<Int>(revealjsManagedValues)
  var lines by ConfigProperty<Boolean>(revealjsManagedValues)
  var from by ConfigProperty<Int>(revealjsManagedValues)
  var to by ConfigProperty<Int>(revealjsManagedValues)
  var dataOutputHeight by ConfigProperty<Int>(revealjsManagedValues)
  var matchBrackets by ConfigProperty<Boolean>(revealjsManagedValues)
  var dataCrosslink by ConfigProperty<Crosslink>(revealjsManagedValues)
  var dataShorterHeight by ConfigProperty<Int>(revealjsManagedValues)
  var dataScrollbarStyle by ConfigProperty<String>(revealjsManagedValues)

  var width by ConfigProperty<String>(kslidesManagedValues)
  var height by ConfigProperty<String>(kslidesManagedValues)
  var style by ConfigProperty<String>(kslidesManagedValues)
  var title by ConfigProperty<String>(kslidesManagedValues) // For screen readers

  fun assignDefaults() {
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
    if (revealjsManagedValues.isNotEmpty())
      revealjsManagedValues
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