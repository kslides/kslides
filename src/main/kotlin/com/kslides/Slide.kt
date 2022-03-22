package com.kslides

import com.kslides.Presentation.Companion.globalConfig
import kotlinx.html.*

typealias SlideArg = (DIV, Slide, PresentationConfig) -> Unit

abstract class Slide(val presentation: Presentation, val content: SlideArg) {
  private val slideConfig = SlideConfig()

  fun mergedConfig(): SlideConfig {
    val global = SlideConfig().apply { combine(globalConfig.slideConfig) }
    val pres = global.apply { combine(presentation.presentationConfig.slideConfig) }
    return pres.apply { combine(slideConfig) }
  }

  @HtmlTagMarker
  fun config(block: SlideConfig.() -> Unit) = block(slideConfig)
}

abstract class HorizontalSlide(presentation: Presentation, content: SlideArg) : Slide(presentation, content)

class HtmlSlide(presentation: Presentation, content: SlideArg) : HorizontalSlide(presentation, content)

class DslSlide(presentation: Presentation, content: SlideArg) : HorizontalSlide(presentation, content) {

  class DslBlock(val dslContent: SECTION.(DslSlide) -> Unit)

  var dslBlock: DslBlock? = null

  @HtmlTagMarker
  fun content(block: SECTION.(DslSlide) -> Unit) {
    dslBlock = DslBlock(block)
  }
}

open class VerticalSlide(presentation: Presentation, content: SlideArg) : Slide(presentation, content)

class VerticalHtmlSlide(presentation: Presentation, content: SlideArg) : VerticalSlide(presentation, content)

class VerticalDslSlide(presentation: Presentation, content: SlideArg) : VerticalSlide(presentation, content) {

  class VerticalDslBlock(val dslContent: SECTION.(VerticalDslSlide) -> Unit)

  var dslBlock: VerticalDslBlock? = null

  @HtmlTagMarker
  fun content(block: SECTION.(VerticalDslSlide) -> Unit) {
    dslBlock = VerticalDslBlock(block)
  }
}