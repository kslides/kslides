package com.kslides.slide

import com.kslides.CssValue
import com.kslides.CssValue.Companion.cssError
import com.kslides.KSlidesDslMarker
import com.kslides.Presentation
import com.kslides.VerticalSlidesContext
import com.kslides.config.SlideConfig
import kotlinx.css.CssBuilder
import kotlinx.html.DIV
import kotlinx.html.SECTION
import kotlinx.html.id
import kotlinx.html.style

/**
 * Rendering callback invoked for each slide during page generation.
 *
 * Receives three arguments:
 * - the enclosing `<div class="slides">` element being populated,
 * - this [Slide] instance (useful when the callback needs to inspect its own state),
 * - `useHttp`: `true` when the page is being served over HTTP, `false` when writing static files.
 */
typealias SlideArgs = (div: DIV, slide: Slide, useHttp: Boolean) -> Unit

/**
 * Base class for all slide types ([MarkdownSlide], [HtmlSlide], [DslSlide]). Holds the shared
 * reveal.js attributes (id, classes, style, visibility flags, auto-animate flags) and a
 * per-slide [SlideConfig] that is merged with the global / presentation configs at render time.
 *
 * Instances are not constructed directly — use [Presentation.markdownSlide],
 * [Presentation.htmlSlide], [Presentation.dslSlide], or [Presentation.verticalSlides].
 */
@KSlidesDslMarker
abstract class Slide(
  private val presentation: Presentation,
  internal val content: SlideArgs,
) {
  private val slideConfig = SlideConfig() // Do not call init on this because it is merged with the presentation config
  val private_slideId = presentation.kslides.slideCount++

  internal val mergedSlideConfig by lazy {
    SlideConfig().also { config ->
      config.merge(presentation.kslides.globalPresentationConfig.slideConfig)
      config.merge(presentation.presentationConfig.slideConfig)
      config.merge(slideConfig)
    }
  }

  /**
   * Placeholder that intentionally throws when written to — CSS must be declared at the
   * `kslides{}` or `presentation{}` level, not on individual slides. Present so invalid usage
   * produces a clear error rather than silently compiling.
   */
  val css = CssValue(valid = false)

  /** CSS class list applied to the slide's `<section>` element. */
  var classes = ""

  /** HTML `id` applied to the slide's `<section>`; blank omits the attribute. */
  var id = ""

  /** Inline CSS applied to the slide's `<section>`; blank omits the attribute. */
  var style = ""

  /** When `true`, the slide is excluded from navigation and the slide counter. */
  var hidden = false

  /** When `true`, the slide is navigable but excluded from the slide counter. */
  var uncounted = false

  /** When `true`, reveal.js auto-animates between this slide and the previous one. */
  var autoAnimate = false

  /** When `true`, forces auto-animate to restart from this slide even if the previous used it. */
  var autoAnimateRestart = false

  /**
   * Guard against declaring `css {}` inside a slide — CSS belongs on the `kslides{}` or
   * `presentation{}` scope. Always throws [IllegalArgumentException].
   */
  fun css(
    @Suppress("UNUSED_PARAMETER") block: CssBuilder.() -> Unit,
  ): Unit = cssError()

  /**
   * Configure per-slide reveal.js attributes (transitions, backgrounds, Markdown separators,
   * etc.). Overrides values inherited from the global and presentation `slideConfig{}` blocks.
   */
  fun slideConfig(block: SlideConfig.() -> Unit) = slideConfig.block()

  /**
   * Apply this slide's id/style/visibility/auto-animate attributes plus the merged
   * [SlideConfig] to the given `<section>` element. Called by the slide renderer and not
   * typically invoked by user code.
   */
  fun processSlide(section: SECTION) {
    if (id.isNotBlank())
      section.id = id

    if (style.isNotBlank())
      section.style = style

    if (hidden)
      section.attributes["data-visibility"] = "hidden"

    if (uncounted)
      section.attributes["data-visibility"] = "uncounted"

    if (autoAnimate)
      section.attributes["data-auto-animate"] = ""

    if (autoAnimateRestart)
      section.attributes["data-auto-animate-restart"] = ""

    mergedSlideConfig.applyConfig(section)
  }
}

/** Marker superclass for slides that appear at the top level of a presentation. */
abstract class HorizontalSlide(
  presentation: Presentation,
  content: SlideArgs,
) : Slide(presentation, content)

/**
 * Marker superclass for slides that appear inside a `verticalSlides {}` block. A single
 * `VerticalSlide` wraps the entire vertical stack and carries the shared [VerticalSlidesContext]
 * used by its children.
 */
open class VerticalSlide(
  presentation: Presentation,
  content: SlideArgs,
) : Slide(presentation, content) {
  internal val verticalContext = VerticalSlidesContext()
}
