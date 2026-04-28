package com.kslides

import com.kslides.CssValue.Companion.cssError
import com.kslides.config.SlideConfig
import com.kslides.slide.VerticalSlide
import kotlinx.css.CssBuilder

/**
 * Receiver type for `verticalSlides{}` blocks. Exposes per-stack id/classes/style attributes plus
 * a [slideConfig] that applies to every child slide in the stack.
 *
 * Intentionally *not* annotated with [KSlidesDslMarker] so that slide-defining calls
 * (`markdownSlide`, `htmlSlide`, `dslSlide`, `slideDefinition`) resolve without needing a
 * `this@Presentation` qualifier.
 *
 * The [slideConfig] declared here is not merged with the global/presentation `slideConfig{}`,
 * so it takes effect as a stack-local override.
 */
class VerticalSlidesContext {
  internal val slideConfig = SlideConfig().apply { assignDefaults() }
  internal val verticalSlides = mutableListOf<VerticalSlide>()

  /**
   * Placeholder that intentionally throws when written to — CSS must be declared at the
   * `kslides{}` or `presentation{}` level, not inside a `verticalSlides{}` block.
   */
  val css = CssValue(valid = false)

  /** CSS class list applied to the outer `<section>` wrapping the vertical stack. */
  var classes = ""

  /** HTML `id` applied to the outer `<section>`; blank omits the attribute. */
  var id = ""

  /** Inline CSS applied to the outer `<section>`; blank omits the attribute. */
  var style = ""

  /**
   * Guard against declaring `css {}` inside a `verticalSlides{}` block. Always throws
   * [IllegalArgumentException].
   */
  fun css(
    @Suppress("UNUSED_PARAMETER") block: CssBuilder.() -> Unit,
  ): Unit = cssError()

  /**
   * Configure a [SlideConfig] that applies to every child slide in this vertical stack. Useful
   * for shared backgrounds, transitions, or Markdown separators across the whole stack.
   */
  fun slideConfig(block: SlideConfig.() -> Unit) = slideConfig.block()

  internal fun resetContext() {
    verticalSlides.clear()
  }
}
