package com.kslides.slide

import com.kslides.KSlidesDslMarker
import com.kslides.Presentation
import kotlinx.html.SECTION

/**
 * Shared contract for slides whose content is a raw HTML string. Implemented by
 * [HorizontalHtmlSlide] and [VerticalHtmlSlide].
 *
 * Members prefixed with `private_` are implementation details and should not be called by
 * user code.
 */
@KSlidesDslMarker
interface HtmlSlide {
  /** Implementation detail — do not use. */
  var private_htmlBlock: () -> String

  /** Implementation detail — do not use. */
  var private_htmlAssigned: Boolean

  /** CSS class list applied to the `<section>`. */
  var classes: String

  /** HTML `id` applied to the `<section>`; blank omits the attribute. */
  var id: String

  /** Inline CSS applied to the `<section>`; blank omits the attribute. */
  var style: String

  /** Apply the slide's attributes to the enclosing `<section>`. Invoked by the renderer. */
  fun processSlide(section: SECTION)
}

/**
 * A top-level slide whose content is raw HTML. Add via [com.kslides.Presentation.htmlSlide].
 */
class HorizontalHtmlSlide(
  presentation: Presentation,
  content: SlideArgs,
) : HorizontalSlide(presentation, content),
  HtmlSlide {
  override var private_htmlBlock: () -> String = { "" }
  override var private_htmlAssigned = false

  /** Supply the HTML content for this slide. Required — blank throws at render time. */
  fun content(htmlBlock: () -> String) {
    private_htmlBlock = htmlBlock
    private_htmlAssigned = true
  }
}

/**
 * A raw-HTML slide inside a `verticalSlides{}` block. Same semantics as [HorizontalHtmlSlide]
 * but lives in a vertical stack.
 */
class VerticalHtmlSlide(
  presentation: Presentation,
  content: SlideArgs,
) : VerticalSlide(presentation, content),
  HtmlSlide {
  override var private_htmlBlock: () -> String = { "" }
  override var private_htmlAssigned = false

  /** Supply the HTML content for this slide. Required — blank throws at render time. */
  fun content(htmlBlock: () -> String) {
    private_htmlBlock = htmlBlock
    private_htmlAssigned = true
  }
}
