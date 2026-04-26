package com.kslides

import com.kslides.DiagramOutputType.SVG
import com.kslides.InternalUtils.stripBraces
import com.kslides.InternalUtils.writeByteArray
import com.kslides.InternalUtils.writeString
import com.kslides.config.CodeSnippetConfig
import com.pambrose.common.util.nullIfBlank
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.html.*

object KSlidesDsl {
  internal val logger = KotlinLogging.logger {}
}

/**
 * Emit a reveal.js-compatible `<pre><code>` code block with optional line-number highlighting and
 * a copy-to-clipboard button (when the CopyCode plugin is enabled). Call from inside a
 * [com.kslides.slide.DslSlide] `content{}` block.
 *
 * @param block populates the [CodeSnippetConfig]; set `+"..."` inside the block to append code.
 */
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

/**
 * Record iframe content for later retrieval. In HTTP mode, the content is cached (by [filename])
 * either as a materialized string (when [staticContent] is true) or as a lambda that re-renders on
 * each request. In filesystem mode, the content is written to `<path>/<filename>` immediately.
 *
 * Used internally by [com.kslides.playground] and the `letsPlot{}` DSL; exposed publicly so
 * custom extension DSLs in the same shape can reuse the caching pipeline.
 *
 * @param useHttp `true` when serving via Ktor, `false` when writing static files.
 * @param staticContent when `useHttp` is `true`, `true` caches the rendered string once;
 *   `false` caches the lambda so content is regenerated on every request.
 * @param kslides owning [KSlides] instance (provides the shared iframe caches).
 * @param path output directory for filesystem mode.
 * @param filename file / cache key for the iframe content.
 * @param contentBlock produces the HTML payload for the iframe.
 */
fun recordIframeContent(
  useHttp: Boolean,
  staticContent: Boolean,
  kslides: KSlides,
  path: String,
  filename: String,
  contentBlock: () -> String,
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
  diagramBlock: () -> ByteArray,
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

/** Emit a `<ul>` with one `<li>` per string in [items]. */
inline fun FlowContent.unorderedList(
  items: List<String>,
  crossinline block: UL.() -> Unit = {},
) = unorderedList(*items.toTypedArray(), block = block)

/** Emit a `<ul>` with one `<li>` per string in [items]. */
inline fun FlowContent.unorderedList(
  vararg items: String,
  crossinline block: UL.() -> Unit = {},
) {
  val funcs: List<LI.() -> Unit> = items.map { { +it } }
  unorderedList(*funcs.toTypedArray(), block = block)
}

/**
 * Emit a `<ul>` where each item is an LI-builder lambda. Use this form when the list items
 * need nested markup (e.g. links, inline code).
 *
 * @param block applied to the enclosing `<ul>` before items are appended.
 */
inline fun FlowContent.unorderedList(
  vararg items: LI.() -> Unit,
  crossinline block: UL.() -> Unit = {},
) = ul {
  block()
  items.forEach {
    li { it() }
  }
}

/** Emit an `<ol>` with one `<li>` per string in [items]. */
inline fun FlowContent.orderedList(
  items: List<String>,
  crossinline block: OL.() -> Unit = {},
) = orderedList(*items.toTypedArray(), block = block)

/** Emit an `<ol>` with one `<li>` per string in [items]. */
inline fun FlowContent.orderedList(
  vararg items: String,
  crossinline block: OL.() -> Unit = {},
) {
  val funcs: List<LI.() -> Unit> = items.map { { +it } }
  orderedList(*funcs.toTypedArray(), block = block)
}

/**
 * Emit an `<ol>` where each item is an LI-builder lambda.
 *
 * @param block applied to the enclosing `<ol>` before items are appended.
 */
inline fun FlowContent.orderedList(
  vararg items: LI.() -> Unit,
  crossinline block: OL.() -> Unit = {},
) = ol {
  block()
  items.forEach {
    li { it() }
  }
}

/**
 * Emit an `<a>` inside an `<li>`. Displays [text] if non-blank, otherwise falls back to the
 * raw [url].
 *
 * @param url the `href` attribute.
 * @param text link text; defaults to [url] when blank.
 * @param target `<a target>` behavior; `HrefTarget.SELF` omits the attribute.
 * @param classes CSS classes for the anchor; blank omits the attribute.
 * @param block additional configuration applied to the anchor element.
 */
inline fun LI.listHref(
  url: String,
  text: String = "",
  target: HrefTarget = HrefTarget.SELF,
  classes: String = "",
  crossinline block: A.() -> Unit = {},
) {
  a(classes = classes.nullIfBlank()) {
    if (target != HrefTarget.SELF) this.target = target.htmlVal
    href = url
    block()
    +(text.ifBlank { url })
  }
}

/** Emit a `<tr>` inside a `<thead>` with one `<th>` per string in [items]. */
fun THEAD.headRow(vararg items: String) {
  val funcs: List<TH.() -> Unit> = items.map { { +it } }
  headRow(*funcs.toTypedArray())
}

/** Emit a `<tr>` inside a `<thead>` where each cell is a TH-builder lambda. */
fun THEAD.headRow(vararg items: TH.() -> Unit) =
  tr {
    items.forEach {
      th { it() }
    }
  }

/** Emit a `<tr>` inside a `<tbody>` with one `<td>` per string in [items]. */
fun TBODY.bodyRow(vararg items: String) {
  val funcs: List<TD.() -> Unit> = items.map { { +it } }
  bodyRow(*funcs.toTypedArray())
}

/** Emit a `<tr>` inside a `<tbody>` where each cell is a TD-builder lambda. */
fun TBODY.bodyRow(vararg items: TD.() -> Unit) =
  tr {
    items.forEach {
      td { it() }
    }
  }

/**
 * Emit an `<a>` anchor with the given [text] and [href]. By default, opens in a new window
 * (`target="_blank"`).
 *
 * @param newWindow when `true`, sets `target="_blank"`; otherwise omits the attribute.
 */
fun FlowOrInteractiveOrPhrasingContent.atag(
  text: String,
  href: String,
  newWindow: Boolean = true,
) {
  a {
    +text
    this.href = href
    if (newWindow) this.target = "_blank"
  }
}
