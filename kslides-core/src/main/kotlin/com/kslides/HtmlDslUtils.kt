package com.kslides

import com.github.pambrose.common.util.*
import com.kslides.HtmlDslUtils.logger
import com.kslides.Playground.otherNames
import com.kslides.Playground.playgroundEndpoint
import com.kslides.Playground.sourceName
import com.kslides.config.*
import kotlinx.html.*
import mu.*

object HtmlDslUtils : KLogging()

context(Presentation, DslSlideContext, SECTION)
    @KSlidesDslMarker
fun playground(
  source: String,
  vararg otherSources: String = emptyArray(),
  block: PlaygroundConfig.() -> Unit = {},
) {
  val mergedConfig =
    PlaygroundConfig()
      .apply { merge(kslides.globalConfig.playgroundConfig) }
      .apply { merge(presentationConfig.playgroundConfig) }
      .apply { merge(PlaygroundConfig().also { block(it) }) }

  val url =
    buildString {
      append("$playgroundEndpoint?")
      append("$sourceName=${source.encode()}")
      if (otherSources.isNotEmpty())
        append("&$otherNames=${otherSources.joinToString(",").encode()}")
      append(mergedConfig.toQueryString())
    }

  iframe {
    logger.info { "Query string: $url" }
    src = url
    mergedConfig.width.also { if (it.isNotEmpty()) width = it }
    mergedConfig.height.also { if (it.isNotEmpty()) height = it }
    mergedConfig.style.also { if (it.isNotEmpty()) style = it }
    mergedConfig.title.also { if (it.isNotEmpty()) title = it }
  }
}

@HtmlTagMarker
fun FlowContent.codeSnippet(
  language: String,
  text: String,
  linePattern: String = "",    // none will turn off line numbers
  lineOffSet: Int = -1,
  dataId: String = "",         // For animation
  trim: Boolean = true,
  escapeHtml: Boolean = false,
  copyButton: Boolean = true,  // Adds COPY button
  copyButtonText: String = "",
  copyButtonMsg: String = "",
) {
  pre {
    if (dataId.isNotBlank())
      attributes["data-id"] = dataId

    attributes["data-cc"] = copyButton.toString()
    if (copyButtonText.isNotBlank())
      attributes["data-cc-copy"] = copyButtonText
    if (copyButtonMsg.isNotBlank())
      attributes["data-cc-copied"] = copyButtonMsg

    code(language.nullIfBlank()) {
      if (linePattern.toLower() != "none")
        attributes["data-line-numbers"] = linePattern
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
    +(if (text.isBlank()) url else text)
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