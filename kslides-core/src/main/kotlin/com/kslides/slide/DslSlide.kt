package com.kslides

import kotlinx.html.*

interface DslSlide {
  val _slideName: String
  var _useHttp: Boolean
  var _dslAssigned: Boolean

  // User variables
  var style: String

  fun processSlide(section: SECTION)
}

class HorizontalDslSlide(presentation: Presentation, content: SlideArgs) : HorizontalSlide(presentation, content),
  DslSlide {
  internal var dslBlock: SECTION.(HorizontalDslSlide) -> Unit = { }
  override var _useHttp: Boolean = false
  override var _dslAssigned = false

  // User variables
  override var style = ""

  @KSlidesDslMarker
  fun content(dslBlock: SECTION.(HorizontalDslSlide) -> Unit) {
    this.dslBlock = dslBlock
    _dslAssigned = true
  }

  @KSlidesDslMarker
  inline fun SectioningOrFlowContent.notes(crossinline block: ASIDE.() -> Unit = {}) =
    ASIDE(attributesMapOf("class", "notes"), consumer).visit(block)
}

class VerticalDslSlide(presentation: Presentation, content: SlideArgs) : VerticalSlide(presentation, content),
  DslSlide {
  internal var dslBlock: SECTION.(VerticalDslSlide) -> Unit = { }
  override var _useHttp: Boolean = false
  override var _dslAssigned = false

  // User variables
  override var style = ""

  @KSlidesDslMarker
  fun content(dslBlock: SECTION.(VerticalDslSlide) -> Unit) {
    this.dslBlock = dslBlock
    _dslAssigned = true
  }

  @KSlidesDslMarker
  inline fun SectioningOrFlowContent.notes(crossinline block: ASIDE.() -> Unit = {}) =
    ASIDE(attributesMapOf("class", "notes"), consumer).visit(block)
}