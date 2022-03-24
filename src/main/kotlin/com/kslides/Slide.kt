package com.kslides

import com.kslides.Presentation.Companion.globalDefaults
import kotlinx.html.*

typealias SlideArg = (DIV, Slide) -> Unit

fun slideBackground(color: String) = "<!-- .slide: data-background=\"$color\" -->"

fun fragmentIndex(index: Int) = "<!-- .element: class=\"fragment\" data-fragment-index=\"$index\" -->"

abstract class Slide(internal val presentation: Presentation, internal val content: SlideArg) {
  private val slideDefaults = SlideConfig()

  internal fun mergedConfig() =
    SlideConfig()
      .apply { combine(globalDefaults.slideDefaults) }
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
  internal var indentToken: String = INDENT_TOKEN
  internal var disableTrimIndent: Boolean = false
  internal var htmlBlock: () -> String = { "" }

  @HtmlTagMarker
  fun content(indentToken: String = INDENT_TOKEN, disableTrimIndent: Boolean = false, htmlBlock: () -> String) {
    this.indentToken = indentToken
    this.disableTrimIndent = disableTrimIndent
    this.htmlBlock = htmlBlock
  }
}

class MarkdownSlide(presentation: Presentation, content: SlideArg) : HorizontalSlide(presentation, content) {
  internal var filename: String = ""
  internal var separator: String = ""
  internal var verticalSeparator: String = ""
  internal var notesSeparator: String = "^Note:"
  internal var indentToken: String = INDENT_TOKEN
  internal var disableTrimIndent: Boolean = false
  internal var markdownBlock: () -> String = { "" }

  @HtmlTagMarker
  fun content(
    filename: String = "",
    separator: String = "",
    verticalSeparator: String = "",
    notesSeparator: String = "^Note:",
    indentToken: String = INDENT_TOKEN,
    disableTrimIndent: Boolean = false,
    markdownBlock: () -> String
  ) {
    this.filename = filename
    this.separator = separator
    this.verticalSeparator = verticalSeparator
    this.notesSeparator = notesSeparator
    this.indentToken = indentToken
    this.disableTrimIndent = disableTrimIndent
    this.markdownBlock = markdownBlock
  }
}

class DslSlide(presentation: Presentation, content: SlideArg) : HorizontalSlide(presentation, content) {
  var dslBlock: SECTION.(DslSlide) -> Unit = { }

  @HtmlTagMarker
  fun content(dslBlock: SECTION.(DslSlide) -> Unit) {
    this.dslBlock = dslBlock
  }
}

open class VerticalSlide(presentation: Presentation, content: SlideArg) : Slide(presentation, content) {
  val vertContext = VerticalContext()
}

class VerticalHtmlSlide(presentation: Presentation, content: SlideArg) : VerticalSlide(presentation, content) {
  internal var indentToken: String = INDENT_TOKEN
  internal var disableTrimIndent: Boolean = false
  internal var htmlBlock: () -> String = { "" }

  @HtmlTagMarker
  fun content(indentToken: String = INDENT_TOKEN, disableTrimIndent: Boolean = false, htmlBlock: () -> String) {
    this.indentToken = indentToken
    this.disableTrimIndent = disableTrimIndent
    this.htmlBlock = htmlBlock
  }
}

class VerticalMarkdownSlide(presentation: Presentation, content: SlideArg) : VerticalSlide(presentation, content) {
  internal var filename: String = ""
  internal var indentToken: String = INDENT_TOKEN
  internal var disableTrimIndent: Boolean = false
  internal var markdownBlock: () -> String = { "" }

  @HtmlTagMarker
  fun content(
    filename: String = "",
    indentToken: String = INDENT_TOKEN,
    disableTrimIndent: Boolean = false,
    markdownBlock: () -> String
  ) {
    this.filename = filename
    this.indentToken = indentToken
    this.disableTrimIndent = disableTrimIndent
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