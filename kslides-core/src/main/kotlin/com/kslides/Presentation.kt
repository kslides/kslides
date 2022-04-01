package com.kslides

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

      presentationBlocks.forEach {
        Presentation(this)
          .also { presentation ->
            it.invoke(presentation)
            presentation.validatePath()
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
  private val plugins = mutableListOf<String>()
  private val dependencies = mutableListOf<String>()

  internal val jsFiles = mutableListOf(JsFile("dist/reveal.js"))
  internal val cssFiles =
    mutableListOf(
      CssFile("dist/reveal.css"),
      CssFile("dist/reset.css"),
    )
  internal val presentationConfig = PresentationConfig()
  internal val slides = mutableListOf<Slide>()

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
        section {
          (slide as VerticalSlide).verticalContext
            .also { verticalContext ->
              block.invoke(verticalContext)
              verticalContext.verticalSlides.forEach { verticalSlide ->
                verticalSlide.content.invoke(div, verticalSlide)
                rawHtml("\n")
              }
            }
        }.also { rawHtml("\n") }
      }
    }.also { slides += it }

  @HtmlTagMarker
  fun VerticalSlideContext.htmlSlide(slideContent: VerticalHtmlSlide.() -> Unit) =
    VerticalHtmlSlide(this@Presentation) { div, slide ->
      div.apply {
        section {
          (slide as VerticalHtmlSlide).apply {
            slideContent.invoke(this)
            assignAttribs(this@section, id, hidden, uncounted, autoAnimate)
            applyConfig(mergedConfig())
            val text =
              htmlBlock()
                .indentInclude(indentToken)
                .let { if (!disableTrimIndent) it.trimIndent() else it }
            rawHtml("\n$text")
          }
        }.also { rawHtml("\n") }
      }
    }.also { verticalSlides += it }

  @HtmlTagMarker
  fun htmlSlide(slideContent: HtmlSlide.() -> Unit) =
    HtmlSlide(this) { div, slide ->
      div.apply {
        section {
          (slide as HtmlSlide).apply {
            slideContent.invoke(this)
            assignAttribs(this@section, id, hidden, uncounted, autoAnimate)
            applyConfig(mergedConfig())
            val text =
              htmlBlock()
                .indentInclude(indentToken)
                .let { if (!disableTrimIndent) it.trimIndent() else it }
            rawHtml("\n$text")
          }
        }.also { rawHtml("\n") }
      }
    }.also { slides += it }

  @HtmlTagMarker
  fun VerticalSlideContext.dslSlide(slideContent: VerticalDslSlide.() -> Unit) =
    VerticalDslSlide(this@Presentation) { div, slide ->
      div.apply {
        section {
          (slide as VerticalDslSlide).apply {
            slideContent.invoke(this)
            assignAttribs(this@section, id, hidden, uncounted, autoAnimate)
            applyConfig(mergedConfig())
            dslBlock.invoke(this@section, this)
          }
        }.also { rawHtml("\n") }
      }
    }.also { verticalSlides += it }

  @HtmlTagMarker
  fun dslSlide(slideContent: DslSlide.() -> Unit) =
    DslSlide(this) { div, slide ->
      div.apply {
        section {
          (slide as DslSlide).apply {
            slideContent.invoke(this)
            assignAttribs(this@section, id, hidden, uncounted, autoAnimate)
            applyConfig(mergedConfig())
            dslBlock.invoke(this@section, this)
          }
        }.also { rawHtml("\n") }
      }
    }.also { slides += it }

  @HtmlTagMarker
  fun VerticalSlideContext.markdownSlide(slideContent: VerticalMarkdownSlide.() -> Unit = { }) =
    VerticalMarkdownSlide(this@Presentation) { div, slide ->
      div.apply {
        section {
          (slide as VerticalMarkdownSlide).apply {
            slideContent.invoke(this)
            assignAttribs(this@section, id, hidden, uncounted, autoAnimate)
            applyConfig(mergedConfig())

            // If this value is == "" it means read content inline
            attributes["data-markdown"] = filename

            if (charset.isNotEmpty())
              attributes["data-charset"] = charset

            // These are not applicable for vertical markdown slides
            attributes["data-separator"] = ""
            attributes["data-separator-vertical"] = ""

            //if (notes.isNotEmpty())
            //    attributes["data-separator-notes"] = notes

            if (filename.isEmpty()) {
              markdownBlock().also { markdown ->
                if (markdown.isNotEmpty())
                  script {
                    type = "text/template"
                    val text =
                      markdown
                        .indentInclude(indentToken)
                        .let { if (!disableTrimIndent) it.trimIndent() else it }
                    rawHtml("\n$text\n")
                  }
              }
            }
          }
        }.also { rawHtml("\n") }
      }
    }.also { verticalSlides += it }

  @HtmlTagMarker
  fun markdownSlide(slideContent: MarkdownSlide.() -> Unit = {}) =
    MarkdownSlide(this) { div, slide ->
      div.apply {
        section {
          (slide as MarkdownSlide).apply {
            slideContent.invoke(this)
            assignAttribs(this@section, id, hidden, uncounted, autoAnimate)
            val config = mergedConfig()
            applyConfig(config)

            // If this value is == "" it means read content inline
            attributes["data-markdown"] = filename

            if (charset.isNotEmpty())
              attributes["data-charset"] = charset

            if (config.markdownSeparator.isNotEmpty())
              attributes["data-separator"] = config.markdownSeparator

            if (config.markdownVerticalSeparator.isNotEmpty())
              attributes["data-separator-vertical"] = config.markdownVerticalSeparator

            // If any of the data-separator values are defined, then plain --- in markdown will not work
            // So do not define data-separator-notes unless using other data-separator values
            if (config.markdownNotesSeparator.isNotEmpty() && config.markdownSeparator.isNotEmpty() && config.markdownVerticalSeparator.isNotEmpty())
              attributes["data-separator-notes"] = config.markdownNotesSeparator

            if (filename.isEmpty()) {
              markdownBlock().also { markdown ->
                if (markdown.isNotEmpty())
                  script {
                    type = "text/template"
                    val text =
                      markdown
                        .indentInclude(indentToken)
                        .let { if (!disableTrimIndent) it.trimIndent() else it }
                    rawHtml("\n$text\n")
                  }
              }
            }
          }
        }.also { rawHtml("\n") }
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

      // Dependencies

      // if (toolbar)
      //   dependencies += "plugin/toolbar/toolbar.js"

      if (dependencies.isNotEmpty()) {
        appendLine(
          buildString {
            appendLine("dependencies: [")
            appendLine(dependencies.joinToString(",\n") { "\t{ src: '${if (it.startsWith("http")) it else "$srcPrefix$it"}' }" })
            appendLine("],")
          }.prependIndent("\t\t\t")
        )
      }

      // Plugins
      if (config.enableSpeakerNotes)
        plugins += "RevealNotes"

      if (config.enableZoom)
        plugins += "RevealZoom"

      if (config.enableSearch)
        plugins += "RevealSearch"

      if (config.enableMarkdown)
        plugins += "RevealMarkdown"

      if (config.enableHighlight)
        plugins += "RevealHighlight"

      if (config.enableMathKatex)
        plugins += "RevealMath.KaTeX"

      if (config.enableMathJax2)
        plugins += "RevealMath.MathJax2"

      if (config.enableMathJax3)
        plugins += "RevealMath.MathJax3"

      if (config.enableMenu)
        plugins += "RevealMenu"

      if (config.enableCodeCopy)
        plugins += "CopyCode"

      appendLine("\t\t\tplugins: [ ${plugins.joinToString(", ")} ]")
    }

  companion object : KLogging() {
    private fun SECTION.applyConfig(config: SlideConfig) {
      if (config.transition != Transition.SLIDE) {
        attributes["data-transition"] = config.transition.asInOut()
      } else {
        if (config.transitionIn != Transition.SLIDE || config.transitionOut != Transition.SLIDE)
          attributes["data-transition"] = "${config.transitionIn.asIn()} ${config.transitionOut.asOut()}"
      }

      if (config.transitionSpeed != Speed.DEFAULT)
        attributes["data-transition-speed"] = config.transitionSpeed.name.toLower()

      if (config.backgroundColor.isNotEmpty())
        attributes["data-background-color"] = config.backgroundColor

      if (config.backgroundIframe.isNotEmpty()) {
        attributes["data-background-iframe"] = config.backgroundIframe

        if (config.backgroundInteractive)
          attributes["data-background-interactive"] = ""
      }

      if (config.backgroundVideo.isNotEmpty())
        attributes["data-background-video"] = config.backgroundVideo
    }
  }
}

class VerticalSlideContext {
  internal val verticalSlides = mutableListOf<VerticalSlide>()
}

class JsFile(val filename: String)

class CssFile(val filename: String, val id: String = "")