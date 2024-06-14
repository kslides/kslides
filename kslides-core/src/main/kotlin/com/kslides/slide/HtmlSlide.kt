package com.kslides.slide

import com.kslides.KSlidesDslMarker
import com.kslides.Presentation
import kotlinx.html.SECTION

interface HtmlSlide {
  var private_htmlBlock: () -> String
  var private_htmlAssigned: Boolean

  var classes: String
  var id: String
  var style: String

  fun processSlide(section: SECTION)
}

class HorizontalHtmlSlide(presentation: Presentation, content: SlideArgs) :
  HorizontalSlide(presentation, content), HtmlSlide {
  override var private_htmlBlock: () -> String = { "" }
  override var private_htmlAssigned = false

  @KSlidesDslMarker
  fun content(htmlBlock: () -> String) {
    private_htmlBlock = htmlBlock
    private_htmlAssigned = true
  }
}

class VerticalHtmlSlide(presentation: Presentation, content: SlideArgs) :
  VerticalSlide(presentation, content), HtmlSlide {
  override var private_htmlBlock: () -> String = { "" }
  override var private_htmlAssigned = false

  @KSlidesDslMarker
  fun content(htmlBlock: () -> String) {
    private_htmlBlock = htmlBlock
    private_htmlAssigned = true
  }
}
