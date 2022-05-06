package com.kslides

import com.github.pambrose.common.util.*
import com.kslides.KSlidesDsl.logger
import com.kslides.Playground.otherNames
import com.kslides.Playground.sourceName
import com.kslides.config.*
import kotlinx.html.*
import mu.*

object KSlidesDsl : KLogging()

//context(Presentation, DslSlide, SECTION)
@KSlidesDslMarker
fun DslSlide.playground(
  source: String,
  vararg otherSources: String = emptyArray(),
  block: PlaygroundConfig.() -> Unit = {},
) {
  val config =
    PlaygroundConfig()
      .apply { merge(presentation.kslides.globalPresentationConfig.playgroundConfig) }
      .apply { merge(presentation.presentationConfig.playgroundConfig) }
      .apply { merge(PlaygroundConfig().also { block(it) }) }

  val pgUrl =
    buildString {
      append(presentation.kslides.kslidesConfig.playgroundEndpoint)
      append("?")
      append("$sourceName=${source.encode()}")
      if (otherSources.isNotEmpty())
        append("&$otherNames=${otherSources.joinToString(",").encode()}")
      append(config.toQueryString())
    }

  if (!_useHttp)
    presentation.kslides.playgroundUrls += _slideName to pgUrl

  (if (_useHttp) pgUrl else _slideName)
    .also { url ->
      logger.debug { "Query string: $url" }
      _section?.iframe {
        src = url
        config.width.also { if (it.isNotBlank()) width = it }
        config.height.also { if (it.isNotBlank()) height = it }
        config.style.also { if (it.isNotBlank()) style = it }
        config.title.also { if (it.isNotBlank()) title = it }
      } ?: error("playground{} must be called from within a content{}")
    }
}

@HtmlTagMarker
fun FlowContent.codeSnippet(
  language: String,
  text: String,
  highlightPattern: String = "", // "" turns on all lines, "none" turns off line numbers
  lineOffSet: Int = -1,
  dataId: String = "",           // For animation
  trim: Boolean = true,
  escapeHtml: Boolean = false,
  copyButton: Boolean = true,    // Adds COPY button
  copyButtonText: String = "",
  copyButtonMsg: String = "",
) {
  pre {
    if (dataId.isNotBlank())
      attributes["data-id"] = dataId
    if (!copyButton)
      attributes["data-cc"] = copyButton.toString()
    if (copyButtonText.isNotBlank())
      attributes["data-cc-copy"] = copyButtonText
    if (copyButtonMsg.isNotBlank())
      attributes["data-cc-copied"] = copyButtonMsg

    code(language.nullIfBlank()) {
      if (!highlightPattern.toLower().contains("none"))
        attributes["data-line-numbers"] = highlightPattern.stripBraces()
      if (lineOffSet != -1)
        attributes["data-ln-start-from"] = lineOffSet.toString()
      if (trim)
        attributes["data-trim"] = ""
      if (!escapeHtml)
        attributes["data-noescape"] = ""
      //script { // This will allow unwrapped html
      //type = "text/template"
      +text
      //}
    }
  }
}

@HtmlTagMarker
inline fun LI.listHref(
  url: String,
  text: String = "",
  classes: String = "",
  newWindow: Boolean = false,
  crossinline block: A.() -> Unit = {}
) {
  a(classes = classes.nullIfBlank()) {
    if (newWindow) target = "_blank"
    href = url
    block()
    +(text.ifBlank { url })
  }
}

@HtmlTagMarker
inline fun DIV.unorderedList(vararg items: String, crossinline block: UL.() -> Unit = {}) {
  val funcs: List<LI.() -> Unit> = items.map { { +it } }
  unorderedList(*funcs.toTypedArray(), block = block)
}

@HtmlTagMarker
inline fun DIV.unorderedList(vararg items: LI.() -> Unit, crossinline block: UL.() -> Unit = {}) =
  ul {
    block()
    items.forEach {
      li { it() }
    }
  }

@HtmlTagMarker
inline fun DIV.orderedList(vararg items: String, crossinline block: OL.() -> Unit = {}) {
  val funcs: List<LI.() -> Unit> = items.map { { +it } }
  orderedList(*funcs.toTypedArray(), block = block)
}

@HtmlTagMarker
inline fun DIV.orderedList(vararg items: LI.() -> Unit, crossinline block: OL.() -> Unit = {}) =
  ol {
    block()
    items.forEach {
      li { it() }
    }
  }

@HtmlTagMarker
inline fun SECTION.unorderedList(vararg items: String, crossinline block: UL.() -> Unit = {}) {
  val funcs: List<LI.() -> Unit> = items.map { { +it } }
  unorderedList(*funcs.toTypedArray(), block = block)
}

@HtmlTagMarker
inline fun SECTION.unorderedList(vararg items: LI.() -> Unit, crossinline block: UL.() -> Unit = {}) =
  ul {
    block()
    items.forEach {
      li { it() }
    }
  }

@HtmlTagMarker
inline fun SECTION.orderedList(vararg items: String, crossinline block: OL.() -> Unit = {}) {
  val funcs: List<LI.() -> Unit> = items.map { { +it } }
  orderedList(*funcs.toTypedArray(), block = block)
}

@HtmlTagMarker
inline fun SECTION.orderedList(vararg items: LI.() -> Unit, crossinline block: OL.() -> Unit = {}) =
  ol {
    block()
    items.forEach {
      li { it() }
    }
  }

@HtmlTagMarker
fun THEAD.headRow(vararg items: String) {
  val funcs: List<TH.() -> Unit> = items.map { { +it } }
  headRow(*funcs.toTypedArray())
}

@HtmlTagMarker
fun THEAD.headRow(vararg items: TH.() -> Unit) =
  tr {
    items.forEach {
      th { it() }
    }
  }

@HtmlTagMarker
fun TBODY.bodyRow(vararg items: String) {
  val funcs: List<TD.() -> Unit> = items.map { { +it } }
  bodyRow(*funcs.toTypedArray())
}

@HtmlTagMarker
fun TBODY.bodyRow(vararg items: TD.() -> Unit) =
  tr {
    items.forEach {
      td { it() }
    }
  }