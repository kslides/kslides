package com.kslides.config

import com.kslides.Crosslink
import com.kslides.CssValue
import com.kslides.KSlidesDslMarker
import com.kslides.PlaygroundMode
import com.kslides.PlaygroundTheme
import com.kslides.TargetPlatform
import kotlinx.css.CssBuilder
import kotlin.reflect.full.isSubclassOf

/**
 * Kotlin Playground embedding options. The top section mirrors the attributes defined by the
 * [Kotlin Playground](https://github.com/JetBrains/kotlin-playground) library itself; the lower
 * section controls the iframe wrapper kslides generates around it.
 *
 * Three separate CSS surfaces, differing by *scope*:
 * - [style] — inline CSS on the `<iframe>` element itself, in the **surrounding slide's** document.
 *   Sizes and frames the box; it cannot reach the code inside.
 * - [fontSize] / [lineHeight] — typed properties rendered by [cssText] into the **iframe's** head.
 * - [css] — raw rules, also in the iframe's head, emitted after [cssText].
 *
 * A Playground styling option earns a typed property when it needs a Playground-internal selector
 * or a specificity workaround a deck author cannot reasonably discover (see [cssText]); anything
 * reachable by an obvious selector stays in [css].
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
   * Font size of the code in the Playground editor and its run-output pane — any CSS length.
   * Blank keeps Playground's own default.
   *
   * Prefer absolute units (`"20px"`): the Playground renders in its own iframe document, so `em`
   * resolves against that document's root font size rather than the surrounding slide's.
   *
   * Line spacing follows automatically — CodeMirror's own `line-height` is a unitless ratio, so it
   * scales with whatever font size is set here. Use [lineHeight] to change the ratio itself.
   *
   * @throws IllegalArgumentException on assignment of a value that is not a CSS length.
   */
  var fontSize by ConfigProperty<String>(kslidesManagedValues, ::requireCssLength)

  /**
   * `line-height` for the Playground editor and run-output pane — a unitless ratio (`"1.4"`) or a
   * CSS length. Blank keeps CodeMirror's own ratio, which already scales with [fontSize]; set this
   * only to tighten or loosen the spacing itself.
   *
   * @throws IllegalArgumentException on assignment of a value that is not a valid `line-height`.
   */
  var lineHeight by ConfigProperty<String>(kslidesManagedValues, ::requireCssLineHeight)

  /**
   * When `true`, the generated Playground HTML is cached for the lifetime of the
   * [com.kslides.KSlides] instance. Set to `false` only if the Playground content depends on
   * runtime state that changes between requests.
   */
  var staticContent by ConfigProperty<Boolean>(kslidesManagedValues)

  /**
   * CSS injected into the generated Playground iframe's `<head>`, after the rules generated for
   * [fontSize] — so a rule declared here wins over them at equal specificity. Reach for [fontSize]
   * first; use this for Playground internals it does not cover.
   */
  val css = CssValue()

  internal fun assignDefaults() {
    width = "100%"
    height = "250px"
    style = ""
    title = ""
    staticContent = false
    fontSize = ""
    lineHeight = ""
  }

  /**
   * The CSS this config generates — the rules implementing [fontSize] / [lineHeight], or an empty
   * string when neither is set. Named to match [ThemeConfig.cssText]; a new typed styling property
   * is wired in here.
   *
   * Both properties are declared on `.CodeMirror` itself rather than on the `<pre>` lines: the
   * editor's own `.CodeMirror pre.CodeMirror-line` rule sets `line-height: inherit` and outranks
   * any `.CodeMirror pre` selector, so the lines take their spacing from the container.
   */
  internal fun cssText(): String {
    val size = fontSize
    val height = lineHeight
    if (size.isBlank() && height.isBlank())
      return ""

    val declarations =
      listOfNotNull(
        if (size.isNotBlank()) "font-size: $size;" else null,
        if (height.isNotBlank()) "line-height: $height;" else null,
      ).joinToString(" ")

    return ".CodeMirror { $declarations }\n.code-output { $declarations }"
  }

  /** [cssText] followed by [userCss] — the Playground iframe's complete stylesheet. */
  internal fun stylesheet(userCss: CssValue): CssValue {
    val generated = cssText()
    return if (generated.isBlank()) CssValue(userCss) else CssValue(CssValue(generated), userCss)
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
