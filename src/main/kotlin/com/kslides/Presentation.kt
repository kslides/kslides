package com.kslides

import com.kslides.Page.generatePage
import com.kslides.Page.rawHtml
import com.kslides.Presentations.Companion.staticRoots
import com.kslides.Presentations.Companion.topLevelPresentations
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.css.*
import kotlinx.html.*
import mu.*
import java.io.*

@HtmlTagMarker
fun presentations(block: Presentations.() -> Unit) {
  topLevelPresentations
    .apply {
      block()
      configBlock.invoke(globalDefaults)
      presentationBlocks.forEach {
        Presentation(this)
          .also { presentation ->
            it.invoke(presentation)
            presentation.validatePath()
          }
      }
      outputBlock.invoke(presentationOutput)
    }
}

class Presentations {
  internal val globalDefaults = PresentationConfig(true)
  internal val presentationOutput = PresentationOutput(this)
  internal var configBlock: PresentationConfig.() -> Unit = {}
  internal var presentationBlocks = mutableListOf<Presentation.() -> Unit>()
  internal var outputBlock: PresentationOutput.() -> Unit = {}
  internal val presentationMap = mutableMapOf<String, Presentation>()

  @HtmlTagMarker
  fun presentation(block: Presentation.() -> Unit) {
    presentationBlocks += block
  }

  @HtmlTagMarker
  fun globalConfig(block: PresentationConfig.() -> Unit) {
    configBlock = block
  }

  @HtmlTagMarker
  fun output(outputBlock: PresentationOutput.() -> Unit) {
    this.outputBlock = outputBlock
  }

  companion object {
    internal val topLevelPresentations = Presentations()
    internal val staticRoots = listOf("public", "assets", "css", "dist", "js", "plugin")
  }
}

class Presentation(val presentations: Presentations) {
  private val plugins = mutableListOf<String>()
  private val dependencies = mutableListOf<String>()

  internal val jsFiles = mutableListOf(JsFile("dist/reveal.js"))
  internal val cssFiles =
    mutableListOf(
      CssFile("dist/reveal.css"),
      CssFile("dist/reset.css"),
    )
  internal val presentationDefaults = PresentationConfig()
  internal val slides = mutableListOf<Slide>()

  var path = "/"
  var css = ""

  internal fun validatePath() {
    if (path.removePrefix("/") in staticRoots)
      throw IllegalArgumentException("Invalid presentation path: \"${"/${path.removePrefix("/")}"}\"")

    val adjustedPath = if (path.startsWith("/")) path else "/$path"
    if (presentations.presentationMap.containsKey(adjustedPath))
      throw IllegalArgumentException("Presentation path already defined: \"$adjustedPath\"")
    presentations.presentationMap[adjustedPath] = this
  }

  @HtmlTagMarker
  fun presentationConfig(block: PresentationConfig.() -> Unit) = block.invoke(presentationDefaults)

  @HtmlTagMarker
  fun css(block: CssBuilder.() -> Unit) {
    css += CssBuilder().apply(block).toString()
  }

  @HtmlTagMarker
  fun verticalSlides(block: VerticalContext.() -> Unit) =
    VerticalSlide(this) { div, slide ->
      div.apply {
        section {
          (slide as VerticalSlide).vertContext
            .also { vertContext ->
              block.invoke(vertContext)
              vertContext.vertSlides.forEach { vertSlide ->
                vertSlide.content.invoke(div, vertSlide)
                rawHtml("\n")
              }
            }
        }.also { rawHtml("\n") }
      }
    }.also { slides += it }

  @HtmlTagMarker
  fun VerticalContext.htmlSlide(slideContent: VerticalHtmlSlide.() -> String) =
    VerticalHtmlSlide(this@Presentation) { div, slide ->
      div.apply {
        section {
          (slide as VerticalHtmlSlide).apply {
            assignAttribs(this@section, id, hidden, uncounted, autoAnimate)
            slideContent.invoke(this)
            applyConfig(mergedConfig())
            val text =
              htmlBlock()
                .indentInclude(indentToken)
                .let { if (!disableTrimIndent) it.trimIndent() else it }
            rawHtml("\n$text")
          }
        }.also { rawHtml("\n") }
      }
    }.also { vertSlides += it }

  @HtmlTagMarker
  fun htmlSlide(slideContent: HtmlSlide.() -> Unit) =
    HtmlSlide(this) { div, slide ->
      div.apply {
        section {
          (slide as HtmlSlide).apply {
            assignAttribs(this@section, id, hidden, uncounted, autoAnimate)
            slideContent.invoke(this)
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
  fun VerticalContext.dslSlide(slideContent: VerticalDslSlide.() -> Unit) =
    VerticalDslSlide(this@Presentation) { div, slide ->
      div.apply {
        section {
          (slide as VerticalDslSlide).apply {
            assignAttribs(this@section, id, hidden, uncounted, autoAnimate)
            slideContent.invoke(this)
            applyConfig(mergedConfig())
            dslBlock.invoke(this@section, this)
          }
        }.also { rawHtml("\n") }
      }
    }.also { vertSlides += it }

  @HtmlTagMarker
  fun dslSlide(slideContent: DslSlide.() -> Unit) =
    DslSlide(this) { div, slide ->
      div.apply {
        section {
          (slide as DslSlide).apply {
            assignAttribs(this@section, id, hidden, uncounted, autoAnimate)
            slideContent.invoke(this)
            applyConfig(mergedConfig())
            dslBlock.invoke(this@section, this)
          }
        }.also { rawHtml("\n") }
      }
    }.also { slides += it }

  @HtmlTagMarker
  fun VerticalContext.markdownSlide(slideContent: VerticalMarkdownSlide.() -> Unit = { }) =
    VerticalMarkdownSlide(this@Presentation) { div, slide ->
      div.apply {
        section {
          (slide as VerticalMarkdownSlide).apply {
            assignAttribs(this@section, id, hidden, uncounted, autoAnimate)
            slideContent.invoke(this)
            applyConfig(mergedConfig())

            // If this value is == "" it means read content inline
            attributes["data-markdown"] = filename
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
                    rawHtml("\n$text")
                  }
              }
            }
          }
        }.also { rawHtml("\n") }
      }
    }.also { vertSlides += it }

  @HtmlTagMarker
  fun markdownSlide(slideContent: MarkdownSlide.() -> Unit = {}) =
    MarkdownSlide(this) { div, slide ->
      div.apply {
        section {
          (slide as MarkdownSlide).apply {
            assignAttribs(this@section, id, hidden, uncounted, autoAnimate)
            slideContent.invoke(this)
            applyConfig(mergedConfig())

            // If this value is == "" it means read content inline
            attributes["data-markdown"] = filename

            if (separator.isNotEmpty())
              attributes["data-separator"] = separator

            if (verticalSeparator.isNotEmpty())
              attributes["data-separator-vertical"] = verticalSeparator

            // If any of the data-separator values are defined, then plain --- in markdown will not work
            // So do not define data-separator-notes unless using other data-separator values
            if (notesSeparator.isNotEmpty() && separator.isNotEmpty() && verticalSeparator.isNotEmpty())
              attributes["data-separator-notes"] = notesSeparator

            if (filename.isEmpty()) {
              markdownBlock().also { markdown ->
                if (markdown.isNotEmpty())
                  script {
                    type = "text/template"
                    val text =
                      markdown
                        .indentInclude(indentToken)
                        .let { if (!disableTrimIndent) it.trimIndent() else it }
                    rawHtml("\n$text")
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

      config.menuDefaults.unmanagedValues.also { vals ->
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

class PresentationOutput(val presentations: Presentations) {
  var port = 8080
  var dir = "docs"
  var srcPrefix = "revealjs/"

  fun httpServer() {
    val environment = commandLineEnvironment(emptyArray())
    embeddedServer(factory = CIO, environment).start(wait = true)
  }

  fun fileSystem() {
    require(dir.isNotEmpty()) { "dir value must not be empty" }

    File(dir).mkdir()
    presentations.presentationMap.forEach { (key, p) ->
      val (file, prefix) =
        when {
          key == "/" -> File("$dir/index.html") to srcPrefix
          key.endsWith(".html") -> File("$dir/$key") to srcPrefix
          else -> {
            val pathElems = "$dir/$key".split("/").filter { it.isNotEmpty() }
            val path = pathElems.joinToString("/")
            val dotDot = List(pathElems.size - 1) { "../" }.joinToString("")
            File(path).mkdir()
            File("$path/index.html") to "$dotDot$srcPrefix"
          }
        }
      println("Writing presentation $key to $file")
      file.writeText(generatePage(p, prefix))
    }
  }

}

class VerticalContext {
  internal val vertSlides = mutableListOf<VerticalSlide>()
}

class JsFile(val filename: String)

class CssFile(val filename: String, val id: String = "")

