package com.kslides.slide

import com.github.pambrose.common.util.toPath
import com.kslides.KSlidesDslMarker
import com.kslides.Presentation
import kotlinx.html.*

interface DslSlide {
  val presentation: Presentation
  var _section: SECTION? // TODO This is a hack that will go away when context receivers work
  val _slideId: Int
  var _useHttp: Boolean
  var _dslAssigned: Boolean
  var _dslBlock: SECTION.() -> Unit
  var _iframeCount: Int

  var classes: String
  var id: String
  var style: String

  // These expose internal values
  val globalPlaygroundConfig get() = presentation.kslides.globalPresentationConfig.playgroundConfig
  val presentationPlaygroundConfig get() = presentation.presentationConfig.playgroundConfig
  val playgroundPath get() = presentation.kslides.outputConfig.playgroundPath
  val globalPlotlyConfig get() = presentation.kslides.globalPresentationConfig.plotlyIframeConfig
  val presentationPlotlyConfig get() = presentation.presentationConfig.plotlyIframeConfig
  val plotlyPath get() = presentation.kslides.outputConfig.plotlyPath

  fun processSlide(section: SECTION)

  fun filename(iframeId: Int) = "slide-$_slideId-$iframeId.html"

  fun playgroundFilename(iframeId: Int) =
    listOf(presentation.kslides.outputConfig.playgroundDir, filename(iframeId))
      .toPath(addPrefix = false, addTrailing = false)

  fun plotlyFilename(iframeId: Int) =
    listOf(presentation.kslides.outputConfig.plotlyDir, filename(iframeId))
      .toPath(addPrefix = false, addTrailing = false)

  @KSlidesDslMarker
  fun SECTION.mermaid(text: String) {
    div("mermaid") {
      +text
    }
  }
}

class HorizontalDslSlide(override val presentation: Presentation, content: SlideArgs) :
  HorizontalSlide(presentation, content), DslSlide {
  override var _section: SECTION? = null
  override var _dslBlock: SECTION.() -> Unit = { }
  override var _iframeCount = 1
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
  override var _iframeCount = 1
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