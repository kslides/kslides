package com.kslides

import kotlinx.html.*

typealias SlideArg = (DIV, Slide) -> Unit

fun slideBackground(color: String) = "<!-- .slide: data-background=\"$color\" -->"

fun fragmentIndex(index: Int) = "<!-- .element: class=\"fragment\" data-fragment-index=\"$index\" -->"

abstract class Slide(internal val presentation: Presentation, internal val content: SlideArg) {
  private val slideDefaults = SlideConfig()

  var id = ""
  var hidden = false
  var uncounted = false
  var autoAnimate = false

  internal fun mergedConfig() =
    SlideConfig()
      .apply { combine(presentation.presentations.globalDefaults.slideDefaults) }
      .apply { combine(presentation.presentationDefaults.slideDefaults) }
      .apply { combine(slideDefaults) }

  internal fun assignAttribs(section: SECTION, id: String, hidden: Boolean, uncounted: Boolean, autoAnimate: Boolean) {
    if (id.isNotEmpty())
      section.id = id

    if (hidden)
      section.attributes["data-visibility"] = "hidden"

    if (uncounted)
      section.attributes["data-visibility"] = "uncounted"

    if (autoAnimate)
      section.attributes["data-auto-animate"] = ""
  }

  @HtmlTagMarker
  fun slideConfig(block: SlideConfig.() -> Unit) = block(slideDefaults)
}

abstract class HorizontalSlide(presentation: Presentation, content: SlideArg) : Slide(presentation, content)

class HtmlSlide(presentation: Presentation, content: SlideArg) : HorizontalSlide(presentation, content) {
  internal var htmlBlock: () -> String = { "" }

  var indentToken: String = INDENT_TOKEN
  var disableTrimIndent: Boolean = false

  @HtmlTagMarker
  fun content(htmlBlock: () -> String) {
    this.htmlBlock = htmlBlock
  }
}

class MarkdownSlide(presentation: Presentation, content: SlideArg) : HorizontalSlide(presentation, content) {
  internal var markdownBlock: () -> String = { "" }
  var filename: String = ""
  var separator: String = ""
  var verticalSeparator: String = ""
  var notesSeparator: String = "^Note:"
  var indentToken: String = INDENT_TOKEN
  var disableTrimIndent: Boolean = false

  @HtmlTagMarker
  fun content(markdownBlock: () -> String) {
    this.markdownBlock = markdownBlock
  }
}

class DslSlide(presentation: Presentation, content: SlideArg) : HorizontalSlide(presentation, content) {
  internal var dslBlock: SECTION.(DslSlide) -> Unit = { }

  @HtmlTagMarker
  fun content(dslBlock: SECTION.(DslSlide) -> Unit) {
    this.dslBlock = dslBlock
  }
}

open class VerticalSlide(presentation: Presentation, content: SlideArg) : Slide(presentation, content) {
  val vertContext = VerticalContext()
}

class VerticalHtmlSlide(presentation: Presentation, content: SlideArg) : VerticalSlide(presentation, content) {
  internal var htmlBlock: () -> String = { "" }
  var indentToken: String = INDENT_TOKEN
  var disableTrimIndent: Boolean = false

  @HtmlTagMarker
  fun content(htmlBlock: () -> String) {
    this.htmlBlock = htmlBlock
  }
}

class VerticalMarkdownSlide(presentation: Presentation, content: SlideArg) : VerticalSlide(presentation, content) {
  internal var markdownBlock: () -> String = { "" }
  var filename: String = ""
  var indentToken: String = INDENT_TOKEN
  var disableTrimIndent: Boolean = false

  @HtmlTagMarker
  fun content(markdownBlock: () -> String) {
    this.markdownBlock = markdownBlock
  }
}

class VerticalDslSlide(presentation: Presentation, content: SlideArg) : VerticalSlide(presentation, content) {
  internal var dslBlock: SECTION.(VerticalDslSlide) -> Unit = { }

  @HtmlTagMarker
  fun content(dslBlock: SECTION.(VerticalDslSlide) -> Unit) {
    this.dslBlock = dslBlock
  }
}