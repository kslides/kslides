package com.kslides

import kotlinx.html.*

interface DslSlide {
  val _slideName: String
  var _useHttp: Boolean
  var _dslAssigned: Boolean

  // User variables
  var style: String
  var classes: String

  fun processSlide(section: SECTION)
  var _dslBlock: SECTION.() -> Unit
}

class HorizontalDslSlide(presentation: Presentation, content: SlideArgs) : HorizontalSlide(presentation, content),
  DslSlide {
  override var _dslBlock: SECTION.() -> Unit = { }
  override var _useHttp: Boolean = false
  override var _dslAssigned = false

  // User variables
  override var style = ""

  @KSlidesDslMarker
  fun content(dslBlock: SECTION.() -> Unit) {
    this._dslBlock = dslBlock
    _dslAssigned = true
  }

  @KSlidesDslMarker
  inline fun SectioningOrFlowContent.notes(crossinline block: ASIDE.() -> Unit = {}) =
    ASIDE(attributesMapOf("class", "notes"), consumer).visit(block)
}

class VerticalDslSlide(presentation: Presentation, content: SlideArgs) : VerticalSlide(presentation, content),
  DslSlide {
  override var _dslBlock: SECTION.() -> Unit = { }
  override var _useHttp: Boolean = false
  override var _dslAssigned = false

  // User variables
  override var style = ""

  @KSlidesDslMarker
  fun content(dslBlock: SECTION.() -> Unit) {
    this._dslBlock = dslBlock
    _dslAssigned = true
  }

  @KSlidesDslMarker
  inline fun SectioningOrFlowContent.notes(crossinline block: ASIDE.() -> Unit = {}) =
    ASIDE(attributesMapOf("class", "notes"), consumer).visit(block)
}