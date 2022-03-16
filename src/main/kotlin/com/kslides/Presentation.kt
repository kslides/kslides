package com.kslides

import com.kslides.Page.generatePage
import com.kslides.Page.rawHtml
import com.kslides.SlideConfig.Companion.slideConfig
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.css.*
import kotlinx.html.*
import mu.*
import org.apache.commons.lang3.StringEscapeUtils.escapeHtml4
import java.io.*

// Keep this global to make it easier for users to be prompted for completion in it
@HtmlTagMarker
fun presentation(
  path: String = "/",
  title: String = "",
  theme: Theme = Theme.BLACK,
  highlight: Highlight = Highlight.MONOKAI,
  block: Presentation.() -> Unit
) =
  Presentation(path, title, theme.name.toLower(), highlight.name.toLower()).apply { block(this) }

class Presentation internal constructor(path: String, val title: String, val theme: String, val highlight: String) {

  var css = ""

  val jsFiles =
    mutableListOf(
      "dist/reveal.js",
      "plugin/zoom/zoom.js",
      "plugin/notes/notes.js",
      "plugin/search/search.js",
      "plugin/markdown/markdown.js",
      "plugin/highlight/highlight.js",
    )

  val cssFiles =
    mutableListOf(
      "dist/reset.css" to "",
      "dist/reveal.css" to "",
      "dist/theme/$theme.css" to "theme",
      "plugin/highlight/$highlight.css" to "highlight-theme",
    )

  private val baseConfigMap = mutableMapOf<String, Any>()
  private val menuConfigMap = mutableMapOf<String, Any>()
  private val menuConfig = MenuConfig(menuConfigMap)

  val baseConfig = BaseConfig(baseConfigMap, menuConfig)
  val slides = mutableListOf<DIV.() -> Unit>()

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
    val vertSlides = mutableListOf<SECTION.() -> Unit>()
  }

  @HtmlTagMarker
  fun verticalSlides(block: VerticalContext.() -> Unit) {
    val vertContext = VerticalContext()
    block.invoke(vertContext)

    slides += {
      section {
        vertContext.vertSlides.forEach {
          section { it.invoke(this) }
          rawHtml("\n")
        }
      }
      rawHtml("\n")
    }
  }

  @HtmlTagMarker
  fun VerticalContext.rawHtmlSlide(
    slideConfig: SlideConfig = slideConfig {},
    id: String = "",
    content: () -> String
  ) {
    vertSlides += {
      if (id.isNotEmpty())
        this.id = id
      applyConfig(slideConfig)
      rawHtml(content.invoke())
    }
  }

  @HtmlTagMarker
  fun rawHtmlSlide(
    slideConfig: SlideConfig = slideConfig {},
    id: String = "",
    content: () -> String
  ) {
    slides += {
      section {
        if (id.isNotEmpty())
          this.id = id
        applyConfig(slideConfig)
        rawHtml("\n" + content.invoke())
      }
      rawHtml("\n")
    }
  }

  @HtmlTagMarker
  fun VerticalContext.htmlSlide(
    slideConfig: SlideConfig = slideConfig {},
    id: String = "",
    content: SECTION.() -> Unit
  ) {
    vertSlides += {
      if (id.isNotEmpty())
        this.id = id
      applyConfig(slideConfig)
      content.invoke(this)
    }
  }

  @HtmlTagMarker
  fun htmlSlide(
    slideConfig: SlideConfig = slideConfig {},
    id: String = "",
    content: SECTION.() -> Unit
  ) {
    slides += {
      section {
        if (id.isNotEmpty())
          this.id = id
        applyConfig(slideConfig)
        content.invoke(this)
      }
      rawHtml("\n")
    }
  }

  @HtmlTagMarker
  fun VerticalContext.markdownSlide(
    slideConfig: SlideConfig = slideConfig {},
    id: String = "",
    filename: String = "",
    content: () -> String = { "" },
  ) {
    htmlSlide(slideConfig = slideConfig, id = id) {
      // If this value is == "" it means read content inline
      attributes["data-markdown"] = filename
      attributes["data-separator"] = ""
      attributes["data-separator-vertical"] = ""

      //if (notes.isNotEmpty())
      //    attributes["data-separator-notes"] = notes

      if (filename.isEmpty())
        script {
          type = "text/template"
          rawHtml("\n" + content.invoke().let { if (baseConfig.markdownTrimIndent) it.trimIndent() else it })
        }
    }
  }

  @HtmlTagMarker
  fun markdownSlide(
    slideConfig: SlideConfig = slideConfig {},
    id: String = "",
    filename: String = "",
    separator: String = "",
    vertical_separator: String = "",
    notes: String = "^Note:",
    content: () -> String = { "" },
  ) {
    htmlSlide(slideConfig = slideConfig, id = id) {
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
            "\n" + content.invoke().let { escapeHtml4(if (baseConfig.markdownTrimIndent) it.trimIndent() else it) })
        }
    }
  }

  @HtmlTagMarker
  fun config(block: BaseConfig.() -> Unit) = block.invoke(baseConfig)

  private fun options(language: PlaygroundLanguage) =
    """
      mode="${language.name.toLower()}" theme="idea" indent="4" auto-indent="true" lines="true" highlight-on-fly="true" data-autocomplete="true" match-brackets="true" 
      """.trimIndent()

  fun playgroundFile(filename: String, language: PlaygroundLanguage = PlaygroundLanguage.KOTLIN): String {
    return "<div class=\"kotlin-code\" ${options(language)}>\n${includeFile(filename).trimIndent()}\n</div>\n"
  }

  fun playgroundUrl(url: String, language: PlaygroundLanguage = PlaygroundLanguage.KOTLIN): String {
    return "<div class=\"kotlin-code\" ${options(language)}>\n${includeUrl(url).trimIndent()}\n</div>\n"
  }

  private fun toJsValue(key: String, value: Any) =
    when (value) {
      is Boolean, is Number -> "$key: $value"
      is String -> "$key: '$value'"
      is Transition -> "$key: '${value.name.toLower()}'"
      is Speed -> "$key: '${value.name.toLower()}'"
      is List<*> -> "$key: [${value.joinToString(", ") { "'$it'" }}]"
      else -> throw IllegalArgumentException("Invalid value for $key: $value")
    }

  fun toJs(srcPrefix: String) =
    buildString {
      if (baseConfigMap.isNotEmpty()) {
        baseConfigMap.forEach { (k, v) ->
          append("\t\t\t${toJsValue(k, v)},\n")
        }
        appendLine()
      }

      if (menuConfigMap.isNotEmpty()) {
        appendLine(
          buildString {
            appendLine("menu: {")
            appendLine(menuConfigMap.map { (k, v) -> "\t${toJsValue(k, v)}" }.joinToString(",\n"))
            appendLine("},")
          }.prependIndent("\t\t\t")
        )
      }

      // Dependencies
      // if (toolbar)
      //   dependencies += "plugin/toolbar/toolbar.js"

      if (baseConfig.dependencies.isNotEmpty()) {
        appendLine(
          buildString {
            appendLine("dependencies: [")
            appendLine(baseConfig.dependencies.map { "\t{ src: '${if (it.startsWith("http")) it else "$srcPrefix$it"}' }" }
                         .joinToString(",\n"))
            appendLine("],")
          }.prependIndent("\t\t\t")
        )
      }

      // Plugins
      if (baseConfig.copyCode)
        baseConfig.plugins += "CopyCode"

      if (baseConfig.enableMenu)
        baseConfig.plugins += "RevealMenu"

      appendLine("\t\t\tplugins: [ ${baseConfig.plugins.joinToString(", ")} ]")
    }

  companion object : KLogging() {
    internal val presentations = mutableMapOf<String, Presentation>()

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

      if (slideConfig.speed != Speed.DEFAULT)
        attributes["data-transition-speed"] = slideConfig.speed.name.toLower()

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