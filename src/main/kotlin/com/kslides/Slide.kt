package com.kslides

import com.kslides.Presentation.Companion.globalConfig
import kotlinx.html.*

typealias SlideArg = (DIV, Slide) -> Unit

abstract class Slide(internal val presentation: Presentation, internal val content: SlideArg) {
  private val slideConfig = SlideConfig()

  internal fun mergedConfig(): SlideConfig {
    val globalLevel = SlideConfig().apply { combine(globalConfig.slideConfig) }
    val presentationLevel = globalLevel.apply { combine(presentation.presentationConfig.slideConfig) }
    return presentationLevel.apply { combine(slideConfig) }
  }

  fun assignId(section: SECTION, id: String) {
    if (id.isNotEmpty())
      section.id = id
  }

  @HtmlTagMarker
  fun slideConfig(block: SlideConfig.() -> Unit) = block(slideConfig)
}

abstract class HorizontalSlide(presentation: Presentation, content: SlideArg) : Slide(presentation, content)

class HtmlSlide(presentation: Presentation, content: SlideArg) : HorizontalSlide(presentation, content) {
  var htmlBlock: () -> String = { "" }

  @HtmlTagMarker
  fun content(block: () -> String) {
    htmlBlock = block
  }
}

class MarkdownSlide(presentation: Presentation, content: SlideArg) : HorizontalSlide(presentation, content) {
  var markdownBlock: () -> String = { "" }

  @HtmlTagMarker
  fun content(block: () -> String) {
    markdownBlock = block
  }
}

class DslSlide(presentation: Presentation, content: SlideArg) : HorizontalSlide(presentation, content) {
  class DslBlock(val dsl: SECTION.(DslSlide) -> Unit)

  var dslBlock: DslBlock = DslBlock { }

  @HtmlTagMarker
  fun content(block: SECTION.(DslSlide) -> Unit) {
    dslBlock = DslBlock(block)
  }
}

open class VerticalSlide(presentation: Presentation, content: SlideArg) : Slide(presentation, content)

class VerticalHtmlSlide(presentation: Presentation, content: SlideArg) : VerticalSlide(presentation, content) {
  var htmlBlock: () -> String = { "" }

  @HtmlTagMarker
  fun content(block: () -> String) {
    htmlBlock = block
  }
}

class VerticalMarkdownSlide(presentation: Presentation, content: SlideArg) : VerticalSlide(presentation, content) {
  var markdownBlock: () -> String = { "" }

  @HtmlTagMarker
  fun content(block: () -> String) {
    markdownBlock = block
  }
}

class VerticalDslSlide(presentation: Presentation, content: SlideArg) : VerticalSlide(presentation, content) {
  class VerticalDslBlock(val dsl: SECTION.(VerticalDslSlide) -> Unit)

  var dslBlock: VerticalDslBlock = VerticalDslBlock { }

  @HtmlTagMarker
  fun content(block: SECTION.(VerticalDslSlide) -> Unit) {
    dslBlock = VerticalDslBlock(block)
  }
}
