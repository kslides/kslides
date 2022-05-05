package com.kslides

interface MarkdownSlide {
  var _markdownBlock: () -> String
  var filename: String
  var indentToken: String
  var disableTrimIndent: Boolean
}

class HortizontalMarkdownSlide(presentation: Presentation, content: SlideArg) : HorizontalSlide(presentation, content),
  MarkdownSlide {
  internal var markdownAssigned = false
  override var _markdownBlock: () -> String = { "" }
  // User-accessible
  override var filename = ""
  override var indentToken = INDENT_TOKEN
  override var disableTrimIndent = false
  var charset = ""

  @KSlidesDslMarker
  fun content(markdownBlock: () -> String) {
    _markdownBlock = markdownBlock
    markdownAssigned = true
  }
}

class VerticalMarkdownSlide(presentation: Presentation, content: SlideArg) : VerticalSlide(presentation, content),
  MarkdownSlide {
  internal var markdownAssigned = false
  override var _markdownBlock: () -> String = { "" }
  override var filename = ""
  override var indentToken = INDENT_TOKEN
  override var disableTrimIndent = false
  var charset = ""

  @KSlidesDslMarker
  fun content(markdownBlock: () -> String) {
    _markdownBlock = markdownBlock
    markdownAssigned = true
  }
}