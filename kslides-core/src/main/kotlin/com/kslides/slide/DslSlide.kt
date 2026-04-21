package com.kslides.slide

import com.kslides.KSlidesDslMarker
import com.kslides.Presentation
import com.pambrose.common.util.toPath
import kotlinx.html.*

@KSlidesDslMarker
interface DslSlide {
  val presentation: Presentation
  var private_section: SECTION? // TODO This is a hack that will go away when context receivers work
  val private_slideId: Int
  var private_useHttp: Boolean
  var private_dslAssigned: Boolean
  var private_dslBlock: SECTION.() -> Unit
  var private_iframeCount: Int

  var classes: String
  var id: String
  var style: String

  // These expose internal values
  val globalPlaygroundConfig get() = presentation.kslides.globalPresentationConfig.playgroundConfig
  val presentationPlaygroundConfig get() = presentation.presentationConfig.playgroundConfig
  val playgroundPath get() = presentation.kslides.outputConfig.playgroundPath

  val globalLetsPlotConfig get() = presentation.kslides.globalPresentationConfig.letsPlotIframeConfig
  val presentationLetsPlotConfig get() = presentation.presentationConfig.letsPlotIframeConfig
  val letsPlotPath get() = presentation.kslides.outputConfig.letsPlotPath
  val letsPlotJsVersion get() = presentation.kslides.kslidesConfig.letsPlotJsVersion

  val globalDiagramConfig get() = presentation.kslides.globalPresentationConfig.diagramConfig
  val presentationDiagramConfig get() = presentation.presentationConfig.diagramConfig
  val krokiPath get() = presentation.kslides.outputConfig.krokiPath

  fun processSlide(section: SECTION)

  fun newFilename(suffix: String = "html") = "slide-$private_slideId-${private_iframeCount++}.$suffix"

  fun playgroundFilename(
    filename: String,
  ) = listOf(presentation.kslides.outputConfig.playgroundDir, filename).toPath(addPrefix = false, addTrailing = false)

  fun letsPlotFilename(
    filename: String,
  ) = listOf(presentation.kslides.outputConfig.letsPlotDir, filename).toPath(addPrefix = false, addTrailing = false)

  fun krokiFilename(
    filename: String,
  ) = listOf(presentation.kslides.outputConfig.krokiDir, filename).toPath(addPrefix = false, addTrailing = false)
}

class HorizontalDslSlide(
  override val presentation: Presentation,
  content: SlideArgs,
) : HorizontalSlide(presentation, content),
  DslSlide {
  override var private_section: SECTION? = null
  override var private_dslBlock: SECTION.() -> Unit = { }
  override var private_iframeCount = 1
  override var private_useHttp: Boolean = false
  override var private_dslAssigned = false

  fun content(dslBlock: SECTION.() -> Unit) {
    private_dslBlock = dslBlock
    private_dslAssigned = true
  }

  inline fun SectioningOrFlowContent.notes(
    crossinline block: ASIDE.() -> Unit = {},
  ) = ASIDE(attributesMapOf("class", "notes"), consumer).visit(block)
}

class VerticalDslSlide(
  override val presentation: Presentation,
  content: SlideArgs,
) : VerticalSlide(presentation, content),
  DslSlide {
  override var private_section: SECTION? = null
  override var private_dslBlock: SECTION.() -> Unit = { }
  override var private_iframeCount = 1
  override var private_useHttp: Boolean = false
  override var private_dslAssigned = false

  fun content(dslBlock: SECTION.() -> Unit) {
    private_dslBlock = dslBlock
    private_dslAssigned = true
  }

  inline fun SectioningOrFlowContent.notes(
    crossinline block: ASIDE.() -> Unit = {},
  ) = ASIDE(attributesMapOf("class", "notes"), consumer).visit(block)
}
