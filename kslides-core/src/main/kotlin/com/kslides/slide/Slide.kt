package com.kslides

import kotlinx.html.*
import java.util.concurrent.atomic.*

typealias SlideArgs = (DIV, Slide, Boolean) -> Unit

abstract class Slide(private val presentation: Presentation, internal val content: SlideArgs) {
  private val slideConfig = SlideConfig() // Do not call init on this because it is merged with the presentation config
  val _slideName = "${presentation.playgroundPath}slide-${slideCount.incrementAndGet()}.html"
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

  companion object {
    val slideCount = AtomicInteger(0)
  }
}

abstract class HorizontalSlide(presentation: Presentation, content: SlideArgs) : Slide(presentation, content)

open class VerticalSlide(presentation: Presentation, content: SlideArgs) : Slide(presentation, content) {
  val verticalContext = VerticalSlideContext()
}