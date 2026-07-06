package com.kslides.slide

import com.kslides.KSlidesDslMarker
import com.kslides.Presentation
import com.kslides.config.SlideConfig
import kotlinx.html.SECTION

/**
 * Shared contract for slides whose content is authored in Markdown. Implemented by
 * [HorizontalMarkdownSlide] and [VerticalMarkdownSlide].
 *
 * Members prefixed with `private_` are implementation details that cross the slide/renderer
 * boundary; they are not part of the public API and should not be called by user code.
 *
 * @property filename optional path to an external `.md` file. When non-blank, Markdown is loaded
 *   from that file (relative to the reveal.js static root) via reveal.js's `data-markdown`
 *   mechanism instead of from the `content{}` block.
 */
@KSlidesDslMarker
@Suppress("VariableNaming")
interface MarkdownSlide {
  /** Implementation detail — unique slide id, used in renderer diagnostics. */
  val private_slideId: Int

  /** Implementation detail — do not use. */
  var private_markdownBlock: () -> String

  /** Implementation detail — `true` once a `content{}` block has been supplied. */
  var private_markdownAssigned: Boolean

  /** CSS class list applied to the `<section>`. */
  var classes: String

  /** HTML `id` applied to the `<section>`; blank omits the attribute. */
  var id: String

  /** Inline CSS applied to the `<section>`; blank omits the attribute. */
  var style: String

  /** See [MarkdownSlide] documentation. */
  var filename: String

  /** Apply the slide's attributes to the enclosing `<section>`. Invoked by the renderer. */
  fun processSlide(section: SECTION)

  /** Configure per-slide reveal.js attributes. Implemented by [Slide.slideConfig]. */
  fun slideConfig(block: SlideConfig.() -> Unit)

  /**
   * Supply inline Markdown content. Mutually exclusive with [filename]: exactly one of the two
   * must be set before rendering.
   */
  fun content(block: () -> String) {
    private_markdownBlock = block
    private_markdownAssigned = true
  }
}

/**
 * A top-level Markdown slide. Add via [com.kslides.Presentation.markdownSlide]; define content
 * with the [content] block or by assigning [filename].
 */
class HorizontalMarkdownSlide(
  presentation: Presentation,
  content: SlideArgs,
) : HorizontalSlide(presentation, content),
  MarkdownSlide {
  override var private_markdownAssigned = false
  override var private_markdownBlock: () -> String = { "" }

  override var filename = ""
}

/**
 * A Markdown slide inside a `verticalSlides{}` block. Same semantics as [HorizontalMarkdownSlide]
 * but lives in a vertical stack.
 */
class VerticalMarkdownSlide(
  presentation: Presentation,
  content: SlideArgs,
) : VerticalSlide(presentation, content),
  MarkdownSlide {
  override var private_markdownAssigned = false
  override var private_markdownBlock: () -> String = { "" }

  override var filename = ""
}
