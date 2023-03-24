package com.kslides

import com.github.pambrose.common.util.nullIfBlank
import com.kslides.DiagramOutputType.SVG
import com.kslides.InternalUtils.stripBraces
import com.kslides.InternalUtils.writeByteArray
import com.kslides.InternalUtils.writeString
import com.kslides.config.CodeSnippetConfig
import kotlinx.html.*
import mu.two.KLogging

object KSlidesDsl : KLogging()

@HtmlTagMarker
fun FlowContent.codeSnippet(block: CodeSnippetConfig.() -> Unit) {
  val config = CodeSnippetConfig().apply { block() }
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
      if (!config.highlightPattern.lowercase().contains("none"))
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

// Changed from internal
fun recordIframeContent(
  useHttp: Boolean,
  staticContent: Boolean,
  kslides: KSlides,
  path: String,
  filename: String,
  contentBlock: () -> String
) {
  if (useHttp) {
    if (staticContent) {
      kslides.staticIframeContent.computeIfAbsent(filename) {
        KSlidesDsl.logger.info { "Caching iframe content: $filename" }
        contentBlock()
      }
    } else {
      kslides.dynamicIframeContent.computeIfAbsent(filename) {
        KSlidesDsl.logger.info { "Caching iframe lambda: $filename" }
        contentBlock
      }
    }
  } else {
    writeString(path, filename, contentBlock())
  }
}

internal fun recordKrokiContent(
  useHttp: Boolean,
  kslides: KSlides,
  outputType: DiagramOutputType,
  path: String,
  filename: String,
  diagramBlock: () -> ByteArray
) {
  // Caching the content will limit the calls to the Kroki server
  val bytes =
    kslides.staticKrokiContent.computeIfAbsent(filename) {
      KSlidesDsl.logger.info { "Caching kroki content: $filename" }
      diagramBlock()
    }

  if (!useHttp) {
    when (outputType) {
      SVG -> writeString(path, filename, String(bytes))
      else -> writeByteArray(path, filename, bytes)
    }
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

@HtmlTagMarker
fun FlowOrInteractiveOrPhrasingContent.atag(text: String, href: String, newWindow: Boolean = true) {
  a {
    +text
    this.href = href
    if (newWindow) this.target = "_blank"
  }
}