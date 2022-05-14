package com.kslides

import kotlinx.html.*

interface HtmlSlide {
  var _htmlBlock: () -> String
  var _htmlAssigned: Boolean

  var classes: String
  var id: String
  var style: String

  fun processSlide(section: SECTION)
}

class HorizontalHtmlSlide(presentation: Presentation, content: SlideArgs) : HorizontalSlide(presentation, content),
  HtmlSlide {
  override var _htmlBlock: () -> String = { "" }
  override var _htmlAssigned = false

  @KSlidesDslMarker
  fun content(htmlBlock: () -> String) {
    _htmlBlock = htmlBlock
    _htmlAssigned = true
  }
}

class VerticalHtmlSlide(presentation: Presentation, content: SlideArgs) : VerticalSlide(presentation, content),
  HtmlSlide {
  override var _htmlBlock: () -> String = { "" }
  override var _htmlAssigned = false

  @KSlidesDslMarker
  fun content(htmlBlock: () -> String) {
    _htmlBlock = htmlBlock
    _htmlAssigned = true
  }
}