package com.kslides.slide

import com.github.pambrose.common.util.*
import com.kslides.*
import kotlinx.html.*

interface DslSlide {
  val presentation: Presentation
  var _section: SECTION? // TODO This is a hack that will go away when context receivers work
  val _slideFilename: String
  var _useHttp: Boolean
  var _dslAssigned: Boolean
  var _dslBlock: SECTION.() -> Unit

  var classes: String
  var id: String
  var style: String

  fun processSlide(section: SECTION)

  val playgroundFilename get() = listOf(presentation.kslides.outputConfig.playgroundDir, _slideFilename).toPath(false, false)
  val plotlyFilename get() = listOf(presentation.kslides.outputConfig.plotlyDir, _slideFilename).toPath(false, false)
}

class HorizontalDslSlide(override val presentation: Presentation, content: SlideArgs) :
  HorizontalSlide(presentation, content), DslSlide {
  override var _section: SECTION? = null
  override var _dslBlock: SECTION.() -> Unit = { }
  override var _useHttp: Boolean = false
  override var _dslAssigned = false

  @KSlidesDslMarker
  fun content(dslBlock: SECTION.() -> Unit) {
    _dslBlock = dslBlock
    _dslAssigned = true
  }

  @KSlidesDslMarker
  inline fun SectioningOrFlowContent.notes(crossinline block: ASIDE.() -> Unit = {}) =
    ASIDE(attributesMapOf("class", "notes"), consumer).visit(block)
}

class VerticalDslSlide(override val presentation: Presentation, content: SlideArgs) :
  VerticalSlide(presentation, content), DslSlide {
  override var _section: SECTION? = null
  override var _dslBlock: SECTION.() -> Unit = { }
  override var _useHttp: Boolean = false
  override var _dslAssigned = false

  @KSlidesDslMarker
  fun content(dslBlock: SECTION.() -> Unit) {
    _dslBlock = dslBlock
    _dslAssigned = true
  }

  @KSlidesDslMarker
  inline fun SectioningOrFlowContent.notes(crossinline block: ASIDE.() -> Unit = {}) =
    ASIDE(attributesMapOf("class", "notes"), consumer).visit(block)
}