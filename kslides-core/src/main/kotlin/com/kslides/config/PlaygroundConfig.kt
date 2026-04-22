package com.kslides.config

import com.kslides.*
import kotlinx.css.CssBuilder
import kotlin.reflect.full.isSubclassOf

/**
 * Kotlin Playground embedding options. The top section mirrors the attributes defined by the
 * [Kotlin Playground](https://github.com/JetBrains/kotlin-playground) library itself; the lower
 * section controls the iframe wrapper kslides generates around it.
 *
 * CSS declared via [css] is injected into the generated iframe's `<head>` — use it to tweak
 * font size or editor height for the embedded Playground, not to style the surrounding slide.
 */
@KSlidesDslMarker
class PlaygroundConfig : AbstractConfig() {
  /** Playground `args` attribute — command-line arguments passed when the code runs. */
  var args by ConfigProperty<String>(revealjsManagedValues)

  /** Execution target for the embedded code (e.g. JVM, JS, JUnit). */
  var dataTargetPlatform by ConfigProperty<TargetPlatform>(revealjsManagedValues)

  /** When `true`, disables the Run button and just syntax-highlights the code. */
  var dataHighlightOnly by ConfigProperty<Boolean>(revealjsManagedValues)

  /** When `true`, collapses the editor behind a "Show code" button. */
  var foldedButton by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Space-separated list of JS library URLs loaded before JS-target code runs. */
  var dataJsLibs by ConfigProperty<String>(revealjsManagedValues)

  /** Automatically re-indent the code as the user types. */
  var autoIndent by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Editor color theme. */
  var theme by ConfigProperty<PlaygroundTheme>(revealjsManagedValues)

  /** Language/syntax mode the editor operates in. */
  var mode by ConfigProperty<PlaygroundMode>(revealjsManagedValues)

  /** Minimum Kotlin compiler version that must be available on the Playground backend. */
  var dataMinCompilerVersion by ConfigProperty<String>(revealjsManagedValues)

  /** Enable autocomplete in the Playground editor. */
  var dataAutocomplete by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Re-highlight code while it is being edited. */
  var highlightOnFly by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Editor indentation width in spaces. */
  var indent by ConfigProperty<Int>(revealjsManagedValues)

  /** Show line numbers. */
  var lines by ConfigProperty<Boolean>(revealjsManagedValues)

  /** First line to display from the source file (1-based). */
  var from by ConfigProperty<Int>(revealjsManagedValues)

  /** Last line to display from the source file (inclusive). */
  var to by ConfigProperty<Int>(revealjsManagedValues)

  /** Maximum output-pane height in pixels. */
  var dataOutputHeight by ConfigProperty<Int>(revealjsManagedValues)

  /** Highlight matching brackets in the editor. */
  var matchBrackets by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Toggle for Playground's "open in IDE" crosslink button. */
  var dataCrosslink by ConfigProperty<Crosslink>(revealjsManagedValues)

  /** Shorter editor height variant (pixels). */
  var dataShorterHeight by ConfigProperty<Int>(revealjsManagedValues)

  /** CodeMirror `scrollbarStyle` (e.g. `"simple"`, `"null"`). */
  var dataScrollbarStyle by ConfigProperty<String>(revealjsManagedValues)

  /** iframe `width` attribute. Default `"100%"`. */
  var width by ConfigProperty<String>(kslidesManagedValues)

  /** iframe `height` attribute. Default `"250px"`. */
  var height by ConfigProperty<String>(kslidesManagedValues)

  /** Inline CSS applied to the iframe. Useful for adding borders while tuning layout. */
  var style by ConfigProperty<String>(kslidesManagedValues)

  /** Accessible title text for screen readers. */
  var title by ConfigProperty<String>(kslidesManagedValues)

  /**
   * When `true`, the generated Playground HTML is cached for the lifetime of the
   * [com.kslides.KSlides] instance. Set to `false` only if the Playground content depends on
   * runtime state that changes between requests.
   */
  var staticContent by ConfigProperty<Boolean>(kslidesManagedValues)

  /**
   * CSS injected into the generated Playground iframe's `<head>`. Commonly used to override
   * `.CodeMirror { font-size: ...; }` to match the surrounding slide's typography.
   */
  val css = CssValue()

  internal fun assignDefaults() {
    width = "100%"
    height = "250px"
    style = ""
    title = ""
    staticContent = false
  }

  /** Append CSS (via the Kotlin CSS DSL) to the Playground iframe's stylesheet. */
  fun css(block: CssBuilder.() -> Unit) {
    css += block
  }

  internal fun toAttributes() =
    revealjsManagedValues
      .map { (k, v) ->
        k to (
          when {
            v is TargetPlatform -> v.queryVal
            v is PlaygroundMode -> v.queryVal
            v::class.isSubclassOf(Enum::class) -> (v as Enum<*>).name.lowercase()
            else -> v.toString()
          }
          )
      }

  companion object {
    internal fun String.toPropertyName() =
      toList()
        .map { if (it.isUpperCase()) "-${it.lowercaseChar()}" else it }
        .joinToString("")
  }
}
