package com.kslides

import com.github.pambrose.common.util.*
import com.kslides.KSlides.Companion.topLevel
import com.kslides.Output.runHttpServer
import com.kslides.Output.writeToFileSystem
import com.kslides.Page.rawHtml
import kotlinx.css.*
import kotlinx.html.*
import mu.*

@HtmlTagMarker
fun kslides(block: KSlides.() -> Unit) {
  topLevel
    .apply {

      block()

      configBlock.invoke(globalConfig)

      presentationBlocks.forEach { block ->
        Presentation(this)
          .apply {
            block.invoke(this)
            validatePath()

            finalConfig =
              PresentationConfig()
                .apply {
                  merge(kslides.globalConfig)
                  merge(presentationConfig)
                }

            assignCssFiles()
            assignJsFiles()
            assignPlugins()
            assignDependencies()
          }
      }

      outputBlock.invoke(presentationOutput)

      if (presentationOutput.enableFileSystem)
        writeToFileSystem(presentationOutput)

      if (presentationOutput.enableHttp)
        runHttpServer(presentationOutput)

      if (!presentationOutput.enableFileSystem && !presentationOutput.enableHttp)
        KSlides.logger.warn { "Set enableHttp or enableFileSystem to true in the output block" }
    }
}

class KSlides {
  internal val globalConfig = PresentationConfig(true)
  internal val presentationOutput = PresentationOutput(this)
  internal var configBlock: PresentationConfig.() -> Unit = {}
  internal var presentationBlocks = mutableListOf<Presentation.() -> Unit>()
  internal var outputBlock: PresentationOutput.() -> Unit = {}
  internal val presentationMap = mutableMapOf<String, Presentation>()

  val staticRoots = mutableListOf("assets", "css", "dist", "js", "plugin")

  @HtmlTagMarker
  fun presentationDefault(block: PresentationConfig.() -> Unit) {
    configBlock = block
  }

  @HtmlTagMarker
  fun output(outputBlock: PresentationOutput.() -> Unit) {
    this.outputBlock = outputBlock
  }

  @HtmlTagMarker
  fun presentation(block: Presentation.() -> Unit) {
    presentationBlocks += block
  }

  companion object : KLogging() {
    internal val topLevel = KSlides()
  }
}

class Presentation(val kslides: KSlides) {
  internal val plugins = mutableListOf<String>()
  internal val dependencies = mutableListOf<String>()

  internal val jsFiles = mutableListOf(JsFile("dist/reveal.js"))
  internal val cssFiles =
    mutableListOf(
      CssFile("dist/reveal.css"),
      CssFile("dist/reset.css"),
    )
  internal val presentationConfig = PresentationConfig()
  internal val slides = mutableListOf<Slide>()
  internal lateinit var finalConfig: PresentationConfig

  var path = "/"
  var css = ""

  internal fun validatePath() {
    if (path.removePrefix("/") in kslides.staticRoots)
      throw IllegalArgumentException("Invalid presentation path: \"${"/${path.removePrefix("/")}"}\"")

    val adjustedPath = if (path.startsWith("/")) path else "/$path"
    if (kslides.presentationMap.containsKey(adjustedPath))
      throw IllegalArgumentException("Presentation path already defined: \"$adjustedPath\"")
    kslides.presentationMap[adjustedPath] = this
  }

  internal fun assignCssFiles() {
    cssFiles += CssFile("dist/theme/${finalConfig.theme.name.toLower()}.css", "theme")
    cssFiles += CssFile("plugin/highlight/${finalConfig.highlight.name.toLower()}.css", "highlight-theme")

    if (finalConfig.enableCodeCopy)
      cssFiles += CssFile("plugin/copycode/copycode.css")

    if (finalConfig.githubCornerHref.isNotEmpty())
      cssFiles += CssFile("plugin/githubCorner/githubCorner.css")

    // Add this last so it does not get overridden
    cssFiles += CssFile("css/custom.css")
  }

  internal fun assignJsFiles() {
    if (finalConfig.enableSpeakerNotes)
      jsFiles += JsFile("plugin/notes/notes.js")

    if (finalConfig.enableZoom)
      jsFiles += JsFile("plugin/zoom/zoom.js")

    if (finalConfig.enableSearch)
      jsFiles += JsFile("plugin/search/search.js")

    if (finalConfig.enableMarkdown)
      jsFiles += JsFile("plugin/markdown/markdown.js")

    if (finalConfig.enableHighlight)
      jsFiles += JsFile("plugin/highlight/highlight.js")

    if (finalConfig.enableMathKatex || finalConfig.enableMathJax2 || finalConfig.enableMathJax3)
      jsFiles += JsFile("plugin/math/math.js")

    if (finalConfig.enableCodeCopy) {
      jsFiles += JsFile("plugin/copycode/copycode.js")
      // Required for copycode.js
      jsFiles += JsFile("https://cdnjs.cloudflare.com/ajax/libs/clipboard.js/2.0.6/clipboard.min.js")
    }

    if (finalConfig.enableMenu)
      jsFiles += JsFile("plugin/menu/menu.js")

    // if (finalConfig.toolbar) {
    //   jsFiles += "plugin/toolbar/toolbar.js"
    // }
  }

  internal fun assignPlugins() {
    if (finalConfig.enableSpeakerNotes)
      plugins += "RevealNotes"

    if (finalConfig.enableZoom)
      plugins += "RevealZoom"

    if (finalConfig.enableSearch)
      plugins += "RevealSearch"

    if (finalConfig.enableMarkdown)
      plugins += "RevealMarkdown"

    if (finalConfig.enableHighlight)
      plugins += "RevealHighlight"

    if (finalConfig.enableMathKatex)
      plugins += "RevealMath.KaTeX"

    if (finalConfig.enableMathJax2)
      plugins += "RevealMath.MathJax2"

    if (finalConfig.enableMathJax3)
      plugins += "RevealMath.MathJax3"

    if (finalConfig.enableMenu)
      plugins += "RevealMenu"

    if (finalConfig.enableCodeCopy)
      plugins += "CopyCode"
  }

  internal fun assignDependencies() {
    // if (finalConfig.toolbar)
    //   dependencies += "plugin/toolbar/toolbar.js"
  }

  @HtmlTagMarker
  fun presentationConfig(block: PresentationConfig.() -> Unit) = block.invoke(presentationConfig)

  @HtmlTagMarker
  fun css(block: CssBuilder.() -> Unit) {
    css += CssBuilder().apply(block).toString()
  }

  @HtmlTagMarker
  fun verticalSlides(block: VerticalSlideContext.() -> Unit) =
    VerticalSlide(this) { div, slide ->
      div.apply {
        (slide as VerticalSlide).verticalContext
          .also { verticalContext ->
            block(verticalContext)
            section(classes = verticalContext.classes.nullIfEmpty()) {
              verticalContext.style.also { if (it.isNotEmpty()) style { rawHtml(it) } }
              verticalContext.id.also { if (it.isNotEmpty()) id = it }
              verticalContext.verticalSlides
                .forEach { verticalSlide ->
                  verticalSlide.content.invoke(div, verticalSlide)
                  rawHtml("\n")
                }
            }.also { rawHtml("\n") }
          }
      }
    }.also { slides += it }

  @HtmlTagMarker
  fun VerticalSlideContext.htmlSlide(slideContent: VerticalHtmlSlide.() -> Unit) =
    VerticalHtmlSlide(this@Presentation) { div, slide ->
      div.apply {
        (slide as VerticalHtmlSlide).also { s ->
          slideContent(s)
          section(classes = s.classes.nullIfEmpty()) {
            s.processSlide(this)
            val text =
              s.htmlBlock()
                .indentInclude(s.indentToken)
                .let { if (!s.disableTrimIndent) it.trimIndent() else it }
            rawHtml("\n$text")
          }.also { rawHtml("\n") }
        }
      }
    }.also { verticalSlides += it }

  @HtmlTagMarker
  fun htmlSlide(slideContent: HtmlSlide.() -> Unit) =
    HtmlSlide(this) { div, slide ->
      div.apply {
        (slide as HtmlSlide).also { s ->
          slideContent(s)
          section(classes = s.classes.nullIfEmpty()) {
            s.processSlide(this)
            val text =
              s.htmlBlock()
                .indentInclude(s.indentToken)
                .let { if (!s.disableTrimIndent) it.trimIndent() else it }
            rawHtml("\n$text")
          }.also { rawHtml("\n") }
        }
      }
    }.also { slides += it }

  @HtmlTagMarker
  fun VerticalSlideContext.dslSlide(slideContent: VerticalDslSlide.() -> Unit) =
    VerticalDslSlide(this@Presentation) { div, slide ->
      div.apply {
        (slide as VerticalDslSlide).also { s ->
          slideContent(s)
          section(classes = s.classes.nullIfEmpty()) {
            s.processSlide(this)
            s.dslBlock.invoke(this, s)
          }.also { rawHtml("\n") }
        }
      }
    }.also { verticalSlides += it }

  @HtmlTagMarker
  fun dslSlide(slideContent: DslSlide.() -> Unit) =
    DslSlide(this) { div, slide ->
      div.apply {
        (slide as DslSlide).also { s ->
          slideContent(s)
          section(classes = s.classes.nullIfEmpty()) {
            s.processSlide(this)
            s.dslBlock.invoke(this, s)
          }.also { rawHtml("\n") }
        }
      }
    }.also { slides += it }

  @HtmlTagMarker
  fun VerticalSlideContext.markdownSlide(slideContent: VerticalMarkdownSlide.() -> Unit = { }) =
    VerticalMarkdownSlide(this@Presentation) { div, slide ->
      div.apply {
        (slide as VerticalMarkdownSlide).also { s ->
          slideContent(s)
          section(classes = s.classes.nullIfEmpty()) {
            s.processSlide(this)

            // If this value is == "" it means read content inline
            attributes["data-markdown"] = s.filename

            if (s.charset.isNotEmpty())
              attributes["data-charset"] = s.charset

            // These are not applicable for vertical markdown slides
            attributes["data-separator"] = ""
            attributes["data-separator-vertical"] = ""

            //if (notes.isNotEmpty())
            //    attributes["data-separator-notes"] = notes

            if (s.filename.isEmpty()) {
              s.markdownBlock().also { markdown ->
                if (markdown.isNotEmpty())
                  script("text/template") {
                    val text =
                      markdown
                        .indentInclude(s.indentToken)
                        .let { if (!s.disableTrimIndent) it.trimIndent() else it }
                    rawHtml("\n$text\n")
                  }
              }
            }
          }.also { rawHtml("\n") }
        }
      }
    }.also { verticalSlides += it }

  @HtmlTagMarker
  fun markdownSlide(slideContent: MarkdownSlide.() -> Unit = {}) =
    MarkdownSlide(this) { div, slide ->
      div.apply {
        (slide as MarkdownSlide).also { s ->
          slideContent(s)
          section(classes = s.classes.nullIfEmpty()) {
            s.processSlide(this)

            // If this value is == "" it means read content inline
            attributes["data-markdown"] = s.filename

            if (s.charset.isNotEmpty())
              attributes["data-charset"] = s.charset

            s.mergedConfig.apply {
              if (markdownSeparator.isNotEmpty())
                this@section.attributes["data-separator"] = markdownSeparator

              if (markdownVerticalSeparator.isNotEmpty())
                this@section.attributes["data-separator-vertical"] = markdownVerticalSeparator

              // If any of the data-separator values are defined, then plain --- in markdown will not work
              // So do not define data-separator-notes unless using other data-separator values
              if (markdownNotesSeparator.isNotEmpty() && markdownSeparator.isNotEmpty() && markdownVerticalSeparator.isNotEmpty())
                this@section.attributes["data-separator-notes"] = markdownNotesSeparator
            }

            if (s.filename.isEmpty()) {
              s.markdownBlock().also { markdown ->
                if (markdown.isNotEmpty())
                  script("text/template") {
                    val text =
                      markdown
                        .indentInclude(s.indentToken)
                        .let { if (!s.disableTrimIndent) it.trimIndent() else it }
                    rawHtml("\n$text\n")
                  }
              }
            }
          }.also { rawHtml("\n") }
        }
      }
    }.also { slides += it }

  private fun toJsValue(key: String, value: Any) =
    when (value) {
      is Boolean, is Number -> "$key: $value"
      is String -> "$key: '$value'"
      is Transition -> "$key: '${value.name.toLower()}'"
      is Speed -> "$key: '${value.name.toLower()}'"
      is List<*> -> "$key: [${value.joinToString(", ") { "'$it'" }}]"
      else -> throw IllegalArgumentException("Invalid value for $key: $value")
    }

  fun toJs(config: PresentationConfig, srcPrefix: String) =
    buildString {
      config.unmanagedValues.also { vals ->
        if (vals.isNotEmpty()) {
          vals.forEach { (k, v) ->
            append("\t\t\t${toJsValue(k, v)},\n")
          }
          appendLine()
        }
      }

      config.autoSlide
        .also { autoSlide ->
          when {
            autoSlide is Boolean && !autoSlide -> {
              append("\t\t\t${toJsValue("autoSlide", autoSlide)},\n")
              appendLine()
            }
            autoSlide is Int -> {
              if (autoSlide > 0) {
                append("\t\t\t${toJsValue("autoSlide", autoSlide)},\n")
                appendLine()
              }
            }
            else -> error("Invalid value for autoSlide: $autoSlide")
          }
        }

      config.slideNumber
        .also { slideNumber ->
          when (slideNumber) {
            is Boolean -> {
              if (slideNumber) {
                append("\t\t\t${toJsValue("slideNumber", slideNumber)},\n")
                appendLine()
              }
            }
            is String -> {
              append("\t\t\t${toJsValue("slideNumber", slideNumber)},\n")
              appendLine()
            }
            else -> error("Invalid value for slideNumber: $slideNumber")
          }
        }

      config.menuConfig.unmanagedValues.also { vals ->
        if (vals.isNotEmpty()) {
          appendLine(
            buildString {
              appendLine("menu: {")
              appendLine(vals.map { (k, v) -> "\t${toJsValue(k, v)}" }.joinToString(",\n"))
              appendLine("},")
            }.prependIndent("\t\t\t")
          )
        }
      }

      config.copyCodeConfig.unmanagedValues.also { vals ->
        if (vals.isNotEmpty()) {
          appendLine(
            buildString {
              appendLine("copycode: {")
              appendLine(vals.map { (k, v) -> "\t${toJsValue(k, v)}" }.joinToString(",\n"))
              appendLine("},")
            }.prependIndent("\t\t\t")
          )
        }
      }

      if (dependencies.isNotEmpty()) {
        appendLine(
          buildString {
            appendLine("dependencies: [")
            appendLine(dependencies.joinToString(",\n") { "\t{ src: '${if (it.startsWith("http")) it else "$srcPrefix$it"}' }" })
            appendLine("],")
          }.prependIndent("\t\t\t")
        )
      }

      appendLine("\t\t\tplugins: [ ${plugins.joinToString(", ")} ]")
    }

  companion object : KLogging()
}

class VerticalSlideContext {
  internal val verticalSlides = mutableListOf<VerticalSlide>()
  var id = ""
  var classes = ""
  var style = ""
}

class JsFile(val filename: String)

class CssFile(val filename: String, val id: String = "")