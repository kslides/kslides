package com.kslides

import com.kslides.CssValue.Companion.cssError
import com.kslides.config.*
import com.kslides.slide.*
import kotlinx.css.*

class VerticalSlidesContext {
  // This slideConfig is not overridden by the global and presentation slideConfig
  internal val slideConfig = SlideConfig().apply { assignDefaults() }
  internal val verticalSlides = mutableListOf<VerticalSlide>()

  // This is used to catch incorrect usages of css
  val css = CssValue(valid = false)

  // User variables
  var classes = ""
  var id = ""
  var style = ""

  @KSlidesDslMarker
  fun css(block: CssBuilder.() -> Unit): Unit = cssError()

  @KSlidesDslMarker
  fun slideConfig(block: SlideConfig.() -> Unit) = block(slideConfig)

  internal fun resetContext() {
    verticalSlides.clear()
  }
}