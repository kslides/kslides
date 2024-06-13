package com.kslides.slide

import com.kslides.KSlidesDslMarker
import com.kslides.Presentation

interface MarkdownSlide {
  var _markdownBlock: () -> String

  var classes: String
  var id: String
  var style: String
  var filename: String
}

class HorizontalMarkdownSlide(
  presentation: Presentation,
  content: SlideArgs,
) : HorizontalSlide(presentation, content),
  MarkdownSlide {
  internal var markdownAssigned = false
  override var _markdownBlock: () -> String = { "" }

  // User variables
  override var filename = ""

  @KSlidesDslMarker
  fun content(block: () -> String) {
    _markdownBlock = block
    markdownAssigned = true
  }
}

class VerticalMarkdownSlide(
  presentation: Presentation,
  content: SlideArgs,
) : VerticalSlide(presentation, content), MarkdownSlide {
  internal var markdownAssigned = false
  override var _markdownBlock: () -> String = { "" }

  // User variables
  override var filename = ""

  @KSlidesDslMarker
  fun content(markdownBlock: () -> String) {
    _markdownBlock = markdownBlock
    markdownAssigned = true
  }
}
