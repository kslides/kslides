package com.kslides

import com.kslides.CssValue.Companion.cssError
import com.kslides.config.SlideConfig
import com.kslides.slide.VerticalSlide
import kotlinx.css.CssBuilder

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
  fun css(
    @Suppress("UNUSED_PARAMETER") block: CssBuilder.() -> Unit,
  ): Unit = cssError()

  @KSlidesDslMarker
  fun slideConfig(block: SlideConfig.() -> Unit) = slideConfig.block()

  internal fun resetContext() {
    verticalSlides.clear()
  }
}
