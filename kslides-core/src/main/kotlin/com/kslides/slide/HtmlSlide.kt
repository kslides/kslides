package com.kslides

import kotlinx.html.*

interface HtmlSlide {
  var _htmlBlock: () -> String
  var _htmlAssigned: Boolean
  var classes: String
  var style: String
  var indentToken: String
  var disableTrimIndent: Boolean

  fun processSlide(section: SECTION)
}

class HorizontalHtmlSlide(presentation: Presentation, content: SlideArg) : HorizontalSlide(presentation, content),
  HtmlSlide {
  override var _htmlBlock: () -> String = { "" }
  override var _htmlAssigned = false
  // User-accessible
  override var style = ""
  override var indentToken = INDENT_TOKEN
  override var disableTrimIndent = false

  @KSlidesDslMarker
  fun content(htmlBlock: () -> String) {
    _htmlBlock = htmlBlock
    _htmlAssigned = true
  }
}

class VerticalHtmlSlide(presentation: Presentation, content: SlideArg) : VerticalSlide(presentation, content),
  HtmlSlide {
  override var _htmlBlock: () -> String = { "" }
  override var _htmlAssigned = false
  // User-accessible
  override var style = ""
  override var indentToken = INDENT_TOKEN
  override var disableTrimIndent = false

  @KSlidesDslMarker
  fun content(htmlBlock: () -> String) {
    _htmlBlock = htmlBlock
    _htmlAssigned = true
  }
}