package com.kslides

import kotlinx.html.*

typealias SlideArg = (DIV, Slide) -> Unit

abstract class Slide(internal val presentation: Presentation, internal val content: SlideArg) {
  private val slideConfig = SlideConfig() // Do not call init on this because it is merged with the presentation config
  internal val mergedConfig by lazy {
    SlideConfig()
      .apply { merge(presentation.kslides.globalConfig.slideConfig) }
      .apply { merge(presentation.presentationConfig.slideConfig) }
      .apply { merge(slideConfig) }
  }
  // User-accessible
  var id = ""
  var classes = ""
  var hidden = false
  var uncounted = false
  var autoAnimate = false

  fun processSlide(section: SECTION) {
    if (id.isNotBlank())
      section.id = id

    if (hidden)
      section.attributes["data-visibility"] = "hidden"

    if (uncounted)
      section.attributes["data-visibility"] = "uncounted"

    if (autoAnimate)
      section.attributes["data-auto-animate"] = ""

    mergedConfig.applyConfig(section)
  }

  @KSlidesDslMarker
  fun slideConfig(block: SlideConfig.() -> Unit) = block(slideConfig)
}

abstract class HorizontalSlide(presentation: Presentation, content: SlideArg) : Slide(presentation, content)

open class VerticalSlide(presentation: Presentation, content: SlideArg) : Slide(presentation, content) {
  val verticalContext = VerticalSlideContext()
}