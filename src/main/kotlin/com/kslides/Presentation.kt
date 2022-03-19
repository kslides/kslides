package com.kslides

import com.kslides.Page.generatePage
import com.kslides.Page.rawHtml
import com.kslides.Presentation.Companion.globalConfig
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.css.*
import kotlinx.html.*
import mu.*
import java.io.*

@HtmlTagMarker
fun presentationDefaults(block: PresentationConfig.() -> Unit) = block.invoke(globalConfig)

// Keep this global to make it easier for users to be prompted for completion in it
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

  class VerticalContext {
    val vertSlides = mutableListOf<VerticalSlide>()
  }

  @HtmlTagMarker
  fun verticalSlides(block: VerticalContext.() -> Unit) =
    Slide {
      val vertContext = VerticalContext()
      block.invoke(vertContext)
      section {
        vertContext.vertSlides.forEach {
          section { it.content.invoke(this, it.slideConfig) }
          rawHtml("\n")
        }
      }.also { rawHtml("\n") }
    }.also { slides += it }

  @HtmlTagMarker
  fun VerticalContext.rawHtmlSlide(id: String = "", content: () -> String) =
    VerticalSlide { slideConfig ->
      if (id.isNotEmpty())
        this.id = id
      applyConfig(slideConfig)
      rawHtml(content.invoke())
    }.also { vertSlides += it }

  @HtmlTagMarker
  fun rawHtmlSlide(id: String = "", content: () -> String) =
    Slide { slideConfig ->
      section {
        if (id.isNotEmpty())
          this.id = id
        applyConfig(slideConfig)
        rawHtml("\n" + content.invoke())
      }.also { rawHtml("\n") }
    }.also { slides += it }

  @HtmlTagMarker
  fun Presentation.VerticalContext.htmlSlide(id: String = "", content: SECTION.() -> Unit) =
    VerticalSlide { slideConfig ->
      if (id.isNotEmpty())
        this.id = id
      applyConfig(slideConfig)
      content.invoke(this)
    }.also { vertSlides += it }

  @HtmlTagMarker
  fun htmlSlide(id: String = "", content: SECTION.() -> Unit) =
    Slide { slideConfig ->
      section {
        if (id.isNotEmpty())
          this.id = id
        applyConfig(slideConfig)
        content.invoke(this)
      }
      rawHtml("\n")
    }.also { slides += it }

  @HtmlTagMarker
  fun VerticalContext.markdownSlide(
    id: String = "",
    filename: String = "",
    content: () -> String = { "" },
  ) =
    htmlSlide(id = id) {
      // If this value is == "" it means read content inline
      attributes["data-markdown"] = filename
      attributes["data-separator"] = ""
      attributes["data-separator-vertical"] = ""

      //if (notes.isNotEmpty())
      //    attributes["data-separator-notes"] = notes

      if (filename.isEmpty())
        script {
          type = "text/template"
          rawHtml("\n" + content.invoke().let { if (presentationConfig.trimIndentMarkdown) it.trimIndent() else it })
        }
    }

  @HtmlTagMarker
  fun markdownSlide(
    id: String = "",
    filename: String = "",
    separator: String = "",
    vertical_separator: String = "",
    notes: String = "^Note:",
    content: () -> String = { "" },
  ) =
    htmlSlide(id = id) {
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
          rawHtml(
            "\n" + content.invoke().let { if (presentationConfig.trimIndentMarkdown) it.trimIndent() else it })
        }
    }

//  private fun options(language: PlaygroundLanguage) =
//    """
//      mode="${language.name.toLower()}" theme="idea" indent="4" auto-indent="true" lines="true" highlight-on-fly="true" data-autocomplete="true" match-brackets="true"
//      """.trimIndent()
//
//  fun playgroundFile(filename: String, language: PlaygroundLanguage = PlaygroundLanguage.KOTLIN) =
//    "<div class=\"kotlin-code\" ${options(language)}>\n${includeFile(filename).trimIndent()}\n</div>\n"
//
//  fun playgroundUrl(url: String, language: PlaygroundLanguage = PlaygroundLanguage.KOTLIN) =
//    "<div class=\"kotlin-code\" ${options(language)}>\n${includeUrl(url).trimIndent()}\n</div>\n"

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
      config.revealVals.also { vals ->
        if (vals.isNotEmpty()) {
          vals.forEach { (k, v) ->
            append("\t\t\t${toJsValue(k, v)},\n")
          }
          appendLine()
        }
      }

      config.menuConfig.revealVals.also { vals ->
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
      if (config.copyCode)
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