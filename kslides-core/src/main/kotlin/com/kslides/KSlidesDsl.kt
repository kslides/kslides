package com.kslides

import com.github.pambrose.common.util.*
import com.kslides.InternalUtils.stripBraces
import com.kslides.InternalUtils.writeIframeContent
import com.kslides.Playground.playgroundContent
import com.kslides.Plotly.plotlyContent
import com.kslides.config.*
import com.kslides.slide.*
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
  srcName: String,
  vararg otherSrcs: String = emptyArray(),
  configBlock: PlaygroundConfig.() -> Unit = {},
) {
  val iframeId = _iframeCount++
  val kslides = presentation.kslides
  val config =
    PlaygroundConfig()
      .apply { merge(presentation.kslides.globalPresentationConfig.playgroundConfig) }
      .apply { merge(presentation.presentationConfig.playgroundConfig) }
      .apply { merge(PlaygroundConfig().also { configBlock(it) }) }

  recordContent(
    kslides,
    config.staticContent,
    filename(iframeId),
    kslides.outputConfig.playgroundPath
  ) {
    playgroundContent(kslides, config, srcName, otherSrcs.toList())
  }

  _section?.iframe {
    src = playgroundFilename(iframeId)
    config.width.also { if (it.isNotBlank()) width = it }
    config.height.also { if (it.isNotBlank()) height = it }
    config.style.also { if (it.isNotBlank()) style = it }
    config.title.also { if (it.isNotBlank()) title = it }
  } ?: error("playground{} must be called from within a content{} block")
}

@KSlidesDslMarker
fun DslSlide.plotly(
  block: SECTION.() -> Unit = {},
) {
  val iframeId = _iframeCount++
  val kslides = presentation.kslides
  val config =
    PlotlyConfig()
      .apply { merge(presentation.kslides.globalPresentationConfig.plotlyConfig) }
      .apply { merge(presentation.presentationConfig.plotlyConfig) }
      .apply { merge(PlotlyConfig().also { _plotlyBlock(it) }) }

  recordContent(
    kslides,
    config.staticContent,
    filename(iframeId),
    kslides.outputConfig.plotlyPath
  ) {
    plotlyContent(kslides.kslidesConfig, block)
  }

  _section?.iframe {
    src = plotlyFilename(iframeId)
    config.width.also { if (it.isNotBlank()) width = it }
    config.height.also { if (it.isNotBlank()) height = it }
    config.style.also { if (it.isNotBlank()) style = it }
    config.title.also { if (it.isNotBlank()) title = it }
  } ?: error("plotly{} must be called from within a content{} block")
}

private fun DslSlide.recordContent(
  kslides: KSlides,
  staticContent: Boolean,
  filename: String,
  path: String,
  content: () -> String
) {
  if (_useHttp) {
    if (staticContent) {
      kslides.staticIframeContent.computeIfAbsent(filename) {
        KSlidesDsl.logger.info { "Saving source: $filename" }
        content()
      }
    } else {
      kslides.dynamicIframeContent.computeIfAbsent(filename) {
        KSlidesDsl.logger.info { "Saving lambda: $filename" }
        content
      }
    }
  } else {
    writeIframeContent(path, filename, content())
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