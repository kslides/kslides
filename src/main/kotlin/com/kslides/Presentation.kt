package com.kslides

import com.kslides.Page.generatePage
import com.kslides.Page.rawHtml
import com.kslides.Presentation.Companion.globalDefaults
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.util.logging.*
import kotlinx.css.*
import kotlinx.html.*
import mu.*
import java.io.*

@HtmlTagMarker
fun globalConfig(block: PresentationConfig.() -> Unit) = block.invoke(globalDefaults)

class VerticalContext {
  val vertSlides = mutableListOf<VerticalSlide>()
}

// Keep this global to make it easier for users to be prompted for completion with it
@HtmlTagMarker
fun presentation(
  path: String = "/",
  block: Presentation.() -> Unit
) =
  Presentation(path).apply { block(this) }

class JsFile(val filename: String)

class CssFile(val filename: String, val id: String = "")

class Presentation internal constructor(path: String) {

  var css = ""

  val jsFiles = mutableListOf(JsFile("dist/reveal.js"))

  val cssFiles =
    mutableListOf(
      CssFile("dist/reveal.css"),
      CssFile("dist/reset.css"),
    )

  private val plugins = mutableListOf<String>()
  private val dependencies = mutableListOf<String>()

  internal val presentationDefaults = PresentationConfig()
  internal val slides = mutableListOf<Slide>()

  init {
    if (path.removePrefix("/") in staticRoots)
      throw IllegalArgumentException("Invalid presentation path: \"${"/${path.removePrefix("/")}"}\"")

    val adjustedPath = if (path.startsWith("/")) path else "/$path"
    if (presentations.containsKey(adjustedPath))
      throw IllegalArgumentException("Presentation path already defined: \"$adjustedPath\"")
    presentations[adjustedPath] = this
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
  fun VerticalContext.htmlSlide(
    id: String = "",
    hidden: Boolean = false,
    slideContent: VerticalHtmlSlide.() -> String
  ) =
    VerticalHtmlSlide(this@Presentation) { div, slide ->
      div.apply {
        section {
          slide.assignAttribs(this, id, hidden)
          slideContent.invoke(slide as VerticalHtmlSlide)
          applyConfig(slide.mergedConfig())
          rawHtml("\n${slide.htmlBlock()}")
        }.also { rawHtml("\n") }
      }
    }.also { vertSlides += it }

  @HtmlTagMarker
  fun htmlSlide(id: String = "", hidden: Boolean = false, slideContent: HtmlSlide.() -> Unit) =
    HtmlSlide(this) { div, slide ->
      div.apply {
        section {
          slide.assignAttribs(this, id, hidden)
          slideContent.invoke(slide as HtmlSlide)
          applyConfig(slide.mergedConfig())
          rawHtml("\n${slide.htmlBlock()}")
        }.also { rawHtml("\n") }
      }
    }.also { slides += it }

  @HtmlTagMarker
  fun VerticalContext.dslSlide(id: String = "", hidden: Boolean = false, slideContent: VerticalDslSlide.() -> Unit) =
    VerticalDslSlide(this@Presentation) { div, slide ->
      div.apply {
        section {
          slide.assignAttribs(this, id, hidden)
          slideContent.invoke(slide as VerticalDslSlide)
          applyConfig(slide.mergedConfig())
          slide.dslBlock.invoke(this, slide)
        }.also { rawHtml("\n") }
      }
    }.also { vertSlides += it }

  @HtmlTagMarker
  fun dslSlide(id: String = "", hidden: Boolean = false, slideContent: DslSlide.() -> Unit) =
    DslSlide(this) { div, slide ->
      div.apply {
        section {
          slide.assignAttribs(this, id, hidden)
          slideContent.invoke(slide as DslSlide)
          applyConfig(slide.mergedConfig())
          slide.dslBlock.invoke(this, slide)
        }.also { rawHtml("\n") }
      }
    }.also { slides += it }

  @HtmlTagMarker
  fun VerticalContext.markdownSlide(
    id: String = "",
    hidden: Boolean = false,
    slideContent: VerticalMarkdownSlide.() -> Unit = { }
  ) =
    VerticalMarkdownSlide(this@Presentation) { div, slide ->
      div.apply {
        section {
          slide.assignAttribs(this, id, hidden)
          slideContent.invoke(slide as VerticalMarkdownSlide)
          applyConfig(slide.mergedConfig())

          // If this value is == "" it means read content inline
          attributes["data-markdown"] = slide.filename
          attributes["data-separator"] = ""
          attributes["data-separator-vertical"] = ""

          //if (notes.isNotEmpty())
          //    attributes["data-separator-notes"] = notes

          if (slide.filename.isEmpty()) {
            slide.markdownBlock().also { markdown ->
              if (markdown.isNotEmpty())
                script {
                  type = "text/template"
                  rawHtml("\n${markdown.let { if (!slide.disableTrimIndent) it.trimIndent() else it }}")
                }
            }
          }
        }.also { rawHtml("\n") }
      }
    }.also { vertSlides += it }

  @HtmlTagMarker
  fun markdownSlide(id: String = "", hidden: Boolean = false, slideContent: MarkdownSlide.() -> Unit = {}) =
    MarkdownSlide(this) { div, slide ->
      div.apply {
        section {
          slide.assignAttribs(this, id, hidden)
          slideContent.invoke(slide as MarkdownSlide)
          applyConfig(slide.mergedConfig())

          // If this value is == "" it means read content inline
          attributes["data-markdown"] = slide.filename

          if (slide.separator.isNotEmpty())
            attributes["data-separator"] = slide.separator

          if (slide.verticalSeparator.isNotEmpty())
            attributes["data-separator-vertical"] = slide.verticalSeparator

          // If any of the data-separator values are defined, then plain --- in markdown will not work
          // So do not define data-separator-notes unless using other data-separator values
          if (slide.notesSeparator.isNotEmpty() && slide.separator.isNotEmpty() && slide.verticalSeparator.isNotEmpty())
            attributes["data-separator-notes"] = slide.notesSeparator

          if (slide.filename.isEmpty()) {
            slide.markdownBlock().also { markdown ->
              if (markdown.isNotEmpty())
                script {
                  type = "text/template"
                  rawHtml("\n${markdown.let { if (!slide.disableTrimIndent) it.trimIndent() else it }}")
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
      config.primaryValues.also { vals ->
        if (vals.isNotEmpty()) {
          vals.forEach { (k, v) ->
            append("\t\t\t${toJsValue(k, v)},\n")
          }
          appendLine()
        }
      }

      config.menuDefaults.primaryValues.also { vals ->
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
    internal val presentations = mutableMapOf<String, Presentation>()
    internal val globalDefaults = PresentationConfig(true)

    fun servePresentations() {
      val environment = commandLineEnvironment(emptyArray())
      val environment2 = applicationEngineEnvironment {
        this.log = KtorSimpleLogger("ktor.application")
        watchPaths = listOf("classes")
      }
      embeddedServer(CIO, environment).start(wait = true)
    }

    fun outputPresentations(dir: String = "docs", srcPrefix: String = "revealjs/") {
      require(dir.isNotEmpty()) { "dir value must not be empty" }

      File(dir).mkdir()
      presentations.forEach { (key, p) ->
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
        attributes["data-background"] = config.backgroundColor

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