package com.kslides.slide

import com.kslides.CssValue
import com.kslides.CssValue.Companion.cssError
import com.kslides.KSlidesDslMarker
import com.kslides.Presentation
import com.kslides.VerticalSlidesContext
import com.kslides.config.SlideConfig
import kotlinx.css.CssBuilder
import kotlinx.html.DIV
import kotlinx.html.SECTION
import kotlinx.html.id
import kotlinx.html.style

typealias SlideArgs = (div: DIV, slide: Slide, useHttp: Boolean) -> Unit

abstract class Slide(private val presentation: Presentation, internal val content: SlideArgs) {
  private val slideConfig = SlideConfig() // Do not call init on this because it is merged with the presentation config
  val private_slideId = presentation.kslides.slideCount++

  internal val mergedSlideConfig by lazy {
    SlideConfig().also { config ->
      config.merge(presentation.kslides.globalPresentationConfig.slideConfig)
      config.merge(presentation.presentationConfig.slideConfig)
      config.merge(slideConfig)
    }
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
  var autoAnimateRestart = false

  @KSlidesDslMarker
  fun css(
    @Suppress("UNUSED_PARAMETER") block: CssBuilder.() -> Unit,
  ): Unit = cssError()

  @KSlidesDslMarker
  fun slideConfig(block: SlideConfig.() -> Unit) = slideConfig.block()

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

    if (autoAnimateRestart)
      section.attributes["data-auto-animate-restart"] = ""

    mergedSlideConfig.applyConfig(section)
  }
}

abstract class HorizontalSlide(presentation: Presentation, content: SlideArgs) : Slide(presentation, content)

open class VerticalSlide(presentation: Presentation, content: SlideArgs) : Slide(presentation, content) {
  internal val verticalContext = VerticalSlidesContext()
}
