package com.kslides

import com.kslides.Page.generatePage
import com.kslides.Page.rawHtml
import com.kslides.Presentation.Companion.globalConfig
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.util.logging.*
import kotlinx.css.*
import kotlinx.html.*
import mu.*
import java.io.*

@HtmlTagMarker
fun presentationsDefaults(block: PresentationConfig.() -> Unit) = block.invoke(globalConfig)

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

  val jsFiles =
    mutableListOf(
      JsFile("dist/reveal.js"),
      JsFile("plugin/zoom/zoom.js"),
      JsFile("plugin/notes/notes.js"),
      JsFile("plugin/search/search.js"),
      JsFile("plugin/markdown/markdown.js"),
      JsFile("plugin/highlight/highlight.js"),
    )

  val cssFiles =
    mutableListOf(
      CssFile("dist/reveal.css"),
      CssFile("dist/reset.css"),
    )

  private val plugins = mutableListOf("RevealZoom", "RevealSearch", "RevealMarkdown", "RevealHighlight")
  private val dependencies = mutableListOf<String>()

  val presentationConfig = PresentationConfig()
  val slides = mutableListOf<Slide>()

  init {
    if (path.removePrefix("/") in staticRoots)
      throw IllegalArgumentException("Invalid presentation path: \"${"/${path.removePrefix("/")}"}\"")

    val adjustedPath = if (path.startsWith("/")) path else "/$path"
    if (presentations.containsKey(adjustedPath))
      throw IllegalArgumentException("Presentation path already defined: \"$adjustedPath\"")
    presentations[adjustedPath] = this
  }

  @HtmlTagMarker
  fun css(block: CssBuilder.() -> Unit) {
    css += CssBuilder().apply(block).toString()
  }

  @HtmlTagMarker
  fun verticalSlides(block: VerticalContext.() -> Unit) =
    VerticalSlide(this) { div, slide, config ->
      div.apply {
        section {
          val vertContext = VerticalContext()
          block.invoke(vertContext)
          vertContext.vertSlides.forEach { vertSlide ->
            vertSlide.content.invoke(div, vertSlide, config)
            rawHtml("\n")
          }
        }
        rawHtml("\n")
      }
    }.also { slides += it }

  @HtmlTagMarker
  fun VerticalContext.htmlSlide(id: String = "", content: Slide.() -> String) =
    VerticalHtmlSlide(this@Presentation) { div, slide, config ->
      div.apply {
        section {
          if (id.isNotEmpty())
            this.id = id
          rawHtml(content.invoke(slide))
          applyConfig(slide.mergedConfig())
        }
      }
    }.also { vertSlides += it }

  @HtmlTagMarker
  fun htmlSlide(id: String = "", content: Slide.() -> String) =
    HtmlSlide(this) { div, slide, config ->
      div.apply {
        section {
          if (id.isNotEmpty())
            this.id = id
          rawHtml("\n" + content.invoke(slide))
          applyConfig(slide.mergedConfig())
        }
        rawHtml("\n")
      }
    }.also { slides += it }

  @HtmlTagMarker
  fun VerticalContext.dslSlide(id: String = "", content: VerticalDslSlide.() -> Unit) =
    VerticalDslSlide(this@Presentation) { div, slide, config ->
      div.apply {
        section {
          if (id.isNotEmpty())
            this.id = id
          content.invoke(slide as VerticalDslSlide)
          slide.dslBlock?.dslContent?.invoke(this, slide)
          applyConfig(slide.mergedConfig())
        }
      }
    }.also { vertSlides += it }

  @HtmlTagMarker
  fun dslSlide(id: String = "", content: DslSlide.() -> Unit) =
    DslSlide(this) { div, slide, config ->
      div.apply {
        section {
          if (id.isNotEmpty())
            this.id = id
          content.invoke(slide as DslSlide)
          slide.dslBlock?.dslContent?.invoke(this, slide)
          applyConfig(slide.mergedConfig())
        }
        rawHtml("\n")
      }
    }.also { slides += it }

  @HtmlTagMarker
  fun VerticalContext.markdownSlide(
    id: String = "",
    filename: String = "",
    disableTrimIndent: Boolean = false,
    content: Slide.() -> String = { "" },
  ) =
    dslSlide(id = id) {
      content { dslSlide ->

        // If this value is == "" it means read content inline
        attributes["data-markdown"] = filename
        attributes["data-separator"] = ""
        attributes["data-separator-vertical"] = ""

        //if (notes.isNotEmpty())
        //    attributes["data-separator-notes"] = notes

        if (filename.isEmpty())
          script {
            type = "text/template"
            rawHtml("\n" + content.invoke(dslSlide).let { if (!disableTrimIndent) it.trimIndent() else it })
          }
      }
    }

  @HtmlTagMarker
  fun markdownSlide(
    id: String = "",
    filename: String = "",
    disableTrimIndent: Boolean = false,
    separator: String = "",
    vertical_separator: String = "",
    notes: String = "^Note:",
    content: Slide.() -> String = { "" },
  ) =
    dslSlide(id = id) {
      content { dslSlide ->

        // If this value is == "" it means read content inline
        attributes["data-markdown"] = filename

        if (separator.isNotEmpty())
          attributes["data-separator"] = separator

        if (vertical_separator.isNotEmpty())
          attributes["data-separator-vertical"] = vertical_separator

        // If any of the data-separator values are defined, then plain --- in markdown will not work
        // So do not define data-separator-notes unless using other data-separator values
        if (notes.isNotEmpty() && separator.isNotEmpty() && vertical_separator.isNotEmpty())
          attributes["data-separator-notes"] = notes

        if (filename.isEmpty())
          script {
            type = "text/template"
            rawHtml("\n" + content.invoke(dslSlide).let { if (!disableTrimIndent) it.trimIndent() else it })
          }
      }
    }

  @HtmlTagMarker
  fun config(block: PresentationConfig.() -> Unit) = block.invoke(presentationConfig)

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

      config.menuConfig.primaryValues.also { vals ->
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
            appendLine(dependencies.map { "\t{ src: '${if (it.startsWith("http")) it else "$srcPrefix$it"}' }" }
                         .joinToString(",\n"))
            appendLine("],")
          }.prependIndent("\t\t\t")
        )
      }

      // Plugins
      if (config.enableCodeCopy)
        plugins += "CopyCode"

      if (config.enableMenu)
        plugins += "RevealMenu"

      appendLine("\t\t\tplugins: [ ${plugins.joinToString(", ")} ]")
    }

  companion object : KLogging() {
    internal val presentations = mutableMapOf<String, Presentation>()
    internal val globalConfig = PresentationConfig(true)

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
            key == "/" -> {
              File("$dir/index.html") to srcPrefix
            }
            key.endsWith(".html") -> {
              File("$dir/$key") to srcPrefix
            }
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

    private fun SECTION.applyConfig(slideConfig: SlideConfig) {
      if (slideConfig.transition != Transition.SLIDE) {
        attributes["data-transition"] = slideConfig.transition.asInOut()
      } else {
        if (slideConfig.transitionIn != Transition.SLIDE || slideConfig.transitionOut != Transition.SLIDE)
          attributes["data-transition"] = "${slideConfig.transitionIn.asIn()} ${slideConfig.transitionOut.asOut()}"
      }

      if (slideConfig.transitionSpeed != Speed.DEFAULT)
        attributes["data-transition-speed"] = slideConfig.transitionSpeed.name.toLower()

      if (slideConfig.backgroundColor.isNotEmpty())
        attributes["data-background"] = slideConfig.backgroundColor

      if (slideConfig.backgroundIframe.isNotEmpty()) {
        attributes["data-background-iframe"] = slideConfig.backgroundIframe

        if (slideConfig.backgroundInteractive)
          attributes["data-background-interactive"] = ""
      }

      if (slideConfig.backgroundVideo.isNotEmpty())
        attributes["data-background-video"] = slideConfig.backgroundVideo
    }
  }
}