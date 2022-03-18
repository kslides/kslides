package com.kslides

import kotlinx.html.*

abstract class AbstractSlide() {
  val slideConfig = SlideConfig()

  fun config(block: SlideConfig.() -> Unit) {
    block(slideConfig)
  }
}

class Slide(val content: DIV.(SlideConfig) -> Unit) : AbstractSlide()

class VerticalSlide(val content: SECTION.(SlideConfig) -> Unit) : AbstractSlide()