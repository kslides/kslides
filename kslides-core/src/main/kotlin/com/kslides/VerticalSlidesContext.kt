package com.kslides

import com.kslides.CssValue.Companion.cssError
import com.kslides.config.SlideConfig
import com.kslides.slide.VerticalSlide
import kotlinx.css.CssBuilder

/**
 * Receiver type for `verticalSlides{}` blocks. Exposes per-stack id/classes/style attributes plus
 * a [slideConfig] that applies reveal.js `data-*` options (transitions, backgrounds, Markdown
 * separators) to every child slide in the stack via the wrapper `<section>`.
 *
 * Intentionally *not* annotated with [KSlidesDslMarker] so that slide-defining calls
 * (`markdownSlide`, `htmlSlide`, `dslSlide`, `slideDefinition`) resolve without needing a
 * `this@Presentation` qualifier.
 *
 * The [slideConfig] declared here is not merged with the global/presentation `slideConfig{}`,
 * so it takes effect as a stack-local override. Note that this only covers reveal.js `data-*`
 * options: the kslides-managed font properties (`fontSize`, `codeFontSize`, `codeWrap`) are
 * resolved per-slide via `Slide.mergedSlideConfig` (global -> presentation -> slide) and a
 * stack-level setting for them is silently ignored — set those per-slide or in the
 * presentation/global `slideConfig{}` instead.
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
   * Configure a [SlideConfig] whose reveal.js `data-*` options (transitions, backgrounds,
   * Markdown separators) apply to every child slide in this vertical stack via the wrapper
   * `<section>`. The kslides-managed font properties (`fontSize`, `codeFontSize`, `codeWrap`)
   * are *not* applied at the stack level — they are resolved per-slide, so set them inside each
   * child slide's own `slideConfig{}` or in the presentation/global `slideConfig{}` instead.
   */
  fun slideConfig(block: SlideConfig.() -> Unit) = slideConfig.block()

  internal fun resetContext() {
    verticalSlides.clear()
  }
}
