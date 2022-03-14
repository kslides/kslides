package com.kslides

import com.kslides.Page.generatePage
import com.kslides.Page.rawHtml
import com.kslides.SlideConfig.Companion.slideConfig
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.css.*
import kotlinx.html.*
import mu.*
import java.io.*

// Keep this global to make it easier for users to be prompted for completion in it
@HtmlTagMarker
fun presentation(
  path: String = "/",
  title: String = "",
  theme: Theme = Theme.Black,
  highlight: Highlight = Highlight.Monokai,
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

  val config = RevealJsConfig()

  internal val slides = mutableListOf<DIV.() -> Unit>()

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
          rawHtml("\n" + content.invoke().let { if (config.markdownTrimIndent) it.trimIndent() else it })
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
          rawHtml("\n" + content.invoke().let { if (config.markdownTrimIndent) it.trimIndent() else it })
        }
    }
  }

  @HtmlTagMarker
  fun config(block: RevealJsConfig.() -> Unit) = block.invoke(config)

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
      if (slideConfig.transition != Transition.Slide) {
        attributes["data-transition"] = slideConfig.transition.asInOut()
      } else {
        if (slideConfig.transitionIn != Transition.Slide || slideConfig.transitionOut != Transition.Slide)
          attributes["data-transition"] = "${slideConfig.transitionIn.asIn()} ${slideConfig.transitionOut.asOut()}"
      }

      if (slideConfig.speed != Speed.Default)
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