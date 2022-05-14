package com.kslides

import com.github.pambrose.common.util.*
import com.kslides.InternalUtils.stripBraces
import com.kslides.Playground.logger
import com.kslides.Playground.otherNames
import com.kslides.Playground.sourceName
import com.kslides.config.*
import kotlinx.html.*
import mu.*

object KSlidesDsl : KLogging()

@HtmlTagMarker
fun FlowContent.codeSnippet(block: CodeSnippetConfig.() -> Unit) {
  val config = CodeSnippetConfig().apply { block(this) }
  pre {
    if (config.dataId.isNotBlank())
      attributes["data-id"] = config.dataId
    if (!config.copyButton)
      attributes["data-cc"] = config.copyButton.toString()
    if (config.copyButtonText.isNotBlank())
      attributes["data-cc-copy"] = config.copyButtonText
    if (config.copyButtonMsg.isNotBlank())
      attributes["data-cc-copied"] = config.copyButtonMsg

    code(config.language.nullIfBlank()) {
      if (!config.highlightPattern.toLower().contains("none"))
        attributes["data-line-numbers"] = config.highlightPattern.stripBraces()
      if (config.lineOffSet != -1)
        attributes["data-ln-start-from"] = config.lineOffSet.toString()
      if (config.trim)
        attributes["data-trim"] = ""
      if (!config.escapeHtml)
        attributes["data-noescape"] = ""
      //script { // This will allow unwrapped html
      //type = "text/template"
      +config.code
      //}
    }
  }
}

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

  // Add to list of pages to generate and later grab with an iframe
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
      } ?: error("playground{} must be called from within a content{} block")
    }
}

@HtmlTagMarker
inline fun FlowContent.unorderedList(items: List<String>, crossinline block: UL.() -> Unit = {}) =
  unorderedList(*items.toTypedArray(), block = block)

@HtmlTagMarker
inline fun FlowContent.unorderedList(vararg items: String, crossinline block: UL.() -> Unit = {}) {
  val funcs: List<LI.() -> Unit> = items.map { { +it } }
  unorderedList(*funcs.toTypedArray(), block = block)
}

@HtmlTagMarker
inline fun FlowContent.unorderedList(vararg items: LI.() -> Unit, crossinline block: UL.() -> Unit = {}) =
  ul {
    block()
    items.forEach {
      li { it() }
    }
  }

@HtmlTagMarker
inline fun FlowContent.orderedList(items: List<String>, crossinline block: OL.() -> Unit = {}) =
  orderedList(*items.toTypedArray(), block = block)

@HtmlTagMarker
inline fun FlowContent.orderedList(vararg items: String, crossinline block: OL.() -> Unit = {}) {
  val funcs: List<LI.() -> Unit> = items.map { { +it } }
  orderedList(*funcs.toTypedArray(), block = block)
}

@HtmlTagMarker
inline fun FlowContent.orderedList(vararg items: LI.() -> Unit, crossinline block: OL.() -> Unit = {}) =
  ol {
    block()
    items.forEach {
      li { it() }
    }
  }

@HtmlTagMarker
inline fun LI.listHref(
  url: String,
  text: String = "",
  target: HrefTarget = HrefTarget.SELF,
  classes: String = "",
  crossinline block: A.() -> Unit = {}
) {
  a(classes = classes.nullIfBlank()) {
    if (target != HrefTarget.SELF) this.target = target.htmlVal
    href = url
    block()
    +(text.ifBlank { url })
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