package com.kslides

import com.kslides.Presentation.Companion.globalDefaults
import kotlinx.html.*

typealias SlideArg = (DIV, Slide) -> Unit

abstract class Slide(internal val presentation: Presentation, internal val content: SlideArg) {
  private val slideDefaults = SlideConfig()

  internal fun mergedConfig() =
    SlideConfig()
      .apply { combine(globalDefaults.slideDefaults) }
      .apply { combine(presentation.presentationDefaults.slideDefaults) }
      .apply { combine(slideDefaults) }

  fun assignId(section: SECTION, id: String) {
    if (id.isNotEmpty())
      section.id = id
  }

  @HtmlTagMarker
  fun slideConfig(block: SlideConfig.() -> Unit) = block(slideDefaults)
}

abstract class HorizontalSlide(presentation: Presentation, content: SlideArg) : Slide(presentation, content)

class HtmlSlide(presentation: Presentation, content: SlideArg) : HorizontalSlide(presentation, content) {
  var htmlBlock: () -> String = { "" }

  @HtmlTagMarker
  fun content(htmlBlock: () -> String) {
    this.htmlBlock = htmlBlock
  }
}

class MarkdownSlide(presentation: Presentation, content: SlideArg) : HorizontalSlide(presentation, content) {
  var filename: String = ""
  var separator: String = ""
  var verticalSeparator: String = ""
  var notesSeparator: String = "^Note:"
  var disableTrimIndent: Boolean = false
  var markdownBlock: () -> String = { "" }

  @HtmlTagMarker
  fun content(
    filename: String = "",
    separator: String = "",
    verticalSeparator: String = "",
    notesSeparator: String = "^Note:",
    disableTrimIndent: Boolean = false,
    markdownBlock: () -> String
  ) {
    this.filename = filename
    this.separator = separator
    this.verticalSeparator = verticalSeparator
    this.notesSeparator = notesSeparator
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
  var htmlBlock: () -> String = { "" }

  @HtmlTagMarker
  fun content(htmlBlock: () -> String) {
    this.htmlBlock = htmlBlock
  }
}

class VerticalMarkdownSlide(presentation: Presentation, content: SlideArg) : VerticalSlide(presentation, content) {
  var filename: String = ""
  var disableTrimIndent: Boolean = false
  var markdownBlock: () -> String = { "" }

  @HtmlTagMarker
  fun content(filename: String = "", disableTrimIndent: Boolean = false, markdownBlock: () -> String) {
    this.filename = filename
    this.disableTrimIndent = disableTrimIndent
    this.markdownBlock = markdownBlock
  }
}

class VerticalDslSlide(presentation: Presentation, content: SlideArg) : VerticalSlide(presentation, content) {
  var dslBlock: SECTION.(VerticalDslSlide) -> Unit = { }

  @HtmlTagMarker
  fun content(dslBlock: SECTION.(VerticalDslSlide) -> Unit) {
    this.dslBlock = dslBlock
  }
}