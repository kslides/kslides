package com.kslides.slide

import com.kslides.KSlidesDslMarker
import com.kslides.Presentation

@KSlidesDslMarker
interface MarkdownSlide {
  var private_markdownBlock: () -> String

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
  override var private_markdownBlock: () -> String = { "" }

  // User variables
  override var filename = ""

  fun content(block: () -> String) {
    private_markdownBlock = block
    markdownAssigned = true
  }
}

class VerticalMarkdownSlide(
  presentation: Presentation,
  content: SlideArgs,
) : VerticalSlide(presentation, content),
  MarkdownSlide {
  internal var markdownAssigned = false
  override var private_markdownBlock: () -> String = { "" }

  // User variables
  override var filename = ""

  fun content(markdownBlock: () -> String) {
    private_markdownBlock = markdownBlock
    markdownAssigned = true
  }
}
