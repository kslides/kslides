package com.kslides

import kotlinx.html.*

typealias SlideArg = (DIV, Slide) -> Unit

abstract class Slide(internal val presentation: Presentation, internal val content: SlideArg) {
  private val slideConfig = SlideConfig()
  var id = ""
  var classes = ""
  var hidden = false
  var uncounted = false
  var autoAnimate = false

  internal val mergedConfig by lazy {
    SlideConfig()
      .apply { combine(presentation.kslides.globalConfig.slideConfig) }
      .apply { combine(presentation.presentationConfig.slideConfig) }
      .apply { combine(slideConfig) }
  }

  internal fun processSlide(section: SECTION) {
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

  @HtmlTagMarker
  fun slideConfig(block: SlideConfig.() -> Unit) = block(slideConfig)
}

abstract class HorizontalSlide(presentation: Presentation, content: SlideArg) : Slide(presentation, content)

class HtmlSlide(presentation: Presentation, content: SlideArg) : HorizontalSlide(presentation, content) {
  internal var htmlBlock: () -> String = { "" }
  internal var htmlAssigned = false
  var indentToken = INDENT_TOKEN
  var disableTrimIndent = false

  @HtmlTagMarker
  fun content(htmlBlock: () -> String) {
    this.htmlBlock = htmlBlock
    this.htmlAssigned = true
  }
}

class MarkdownSlide(presentation: Presentation, content: SlideArg) : HorizontalSlide(presentation, content) {
  internal var markdownBlock: () -> String = { "" }
  internal var markdownAssigned = false
  var filename = ""
  var charset = ""
  var indentToken = INDENT_TOKEN
  var disableTrimIndent = false

  @HtmlTagMarker
  fun content(markdownBlock: () -> String) {
    this.markdownBlock = markdownBlock
    this.markdownAssigned = true
  }
}

class DslSlide(presentation: Presentation, content: SlideArg) : HorizontalSlide(presentation, content) {
  internal var dslBlock: SECTION.(DslSlide) -> Unit = { }
  internal var dslAssigned = false

  @HtmlTagMarker
  fun content(dslBlock: SECTION.(DslSlide) -> Unit) {
    this.dslBlock = dslBlock
    this.dslAssigned = true
  }

  @HtmlTagMarker
  inline fun SectioningOrFlowContent.notes(crossinline block: ASIDE.() -> Unit = {}) =
    ASIDE(attributesMapOf("class", "notes"), consumer).visit(block)
}

open class VerticalSlide(presentation: Presentation, content: SlideArg) : Slide(presentation, content) {
  val verticalContext = VerticalSlideContext()
}

class VerticalHtmlSlide(presentation: Presentation, content: SlideArg) : VerticalSlide(presentation, content) {
  internal var htmlBlock: () -> String = { "" }
  internal var htmlAssigned = false
  var indentToken = INDENT_TOKEN
  var disableTrimIndent = false

  @HtmlTagMarker
  fun content(htmlBlock: () -> String) {
    this.htmlBlock = htmlBlock
    this.htmlAssigned = true
  }
}

class VerticalMarkdownSlide(presentation: Presentation, content: SlideArg) : VerticalSlide(presentation, content) {
  internal var markdownBlock: () -> String = { "" }
  internal var markdownAssigned = false
  var filename = ""
  var charset = ""
  var indentToken = INDENT_TOKEN
  var disableTrimIndent = false

  @HtmlTagMarker
  fun content(markdownBlock: () -> String) {
    this.markdownBlock = markdownBlock
    this.markdownAssigned = true
  }
}

class VerticalDslSlide(presentation: Presentation, content: SlideArg) : VerticalSlide(presentation, content) {
  internal var dslBlock: SECTION.(VerticalDslSlide) -> Unit = { }
  internal var dslAssigned = false

  @HtmlTagMarker
  fun content(dslBlock: SECTION.(VerticalDslSlide) -> Unit) {
    this.dslBlock = dslBlock
    this.dslAssigned = true
  }

  @HtmlTagMarker
  inline fun SectioningOrFlowContent.notes(crossinline block: ASIDE.() -> Unit = {}) =
    ASIDE(attributesMapOf("class", "notes"), consumer).visit(block)
}