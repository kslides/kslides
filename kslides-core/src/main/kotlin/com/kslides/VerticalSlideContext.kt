package com.kslides

class VerticalSlideContext {
  // This slideConfig is not overridden by the global and presentation slideConfig
  internal val slideConfig = SlideConfig().apply { assignDefaults() }
  internal val verticalSlides = mutableListOf<VerticalSlide>()

  // User variables
  var id = ""
  var classes = ""

  @KSlidesDslMarker
  fun slideConfig(block: SlideConfig.() -> Unit) = block(slideConfig)

  internal fun resetContext() {
    verticalSlides.clear()
  }
}