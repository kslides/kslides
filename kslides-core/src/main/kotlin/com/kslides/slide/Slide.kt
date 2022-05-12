package com.kslides

import com.kslides.CssValue.Companion.cssError
import com.kslides.config.*
import kotlinx.css.*
import kotlinx.html.*
import java.util.concurrent.atomic.*

typealias SlideArgs = (DIV, Slide, Boolean) -> Unit

abstract class Slide(private val presentation: Presentation, internal val content: SlideArgs) {
  private val slideConfig = SlideConfig() // Do not call init on this because it is merged with the presentation config
  val _slideName = "${presentation.playgroundPath}slide-${slideCount.incrementAndGet()}.html"
  internal val mergedConfig by lazy {
    SlideConfig()
      .apply { merge(presentation.kslides.globalPresentationConfig.slideConfig) }
      .apply { merge(presentation.presentationConfig.slideConfig) }
      .apply { merge(slideConfig) }
  }

  // This is used to catch incorrect usages of css
  val css = CssValue(valid = false)

  // User variables
  var classes = ""
  var id = ""
  var style = ""
  var hidden = false
  var uncounted = false
  var autoAnimate = false

  @KSlidesDslMarker
  fun css(block: CssBuilder.() -> Unit): Unit = cssError()

  @KSlidesDslMarker
  fun slideConfig(block: SlideConfig.() -> Unit) = block(slideConfig)

  fun processSlide(section: SECTION) {
    if (id.isNotBlank())
      section.id = id

    if (style.isNotBlank())
      section.style = style

    if (hidden)
      section.attributes["data-visibility"] = "hidden"

    if (uncounted)
      section.attributes["data-visibility"] = "uncounted"

    if (autoAnimate)
      section.attributes["data-auto-animate"] = ""

    mergedConfig.applyConfig(section)
  }

  companion object {
    private val slideCount = AtomicInteger(0)
  }
}

abstract class HorizontalSlide(presentation: Presentation, content: SlideArgs) : Slide(presentation, content)

open class VerticalSlide(presentation: Presentation, content: SlideArgs) : Slide(presentation, content) {
  internal val verticalContext = VerticalSlidesContext()
}