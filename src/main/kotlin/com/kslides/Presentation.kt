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
fun presentation(path: String = "/", title: String = "", theme: Theme = Theme.Black, block: Presentation.() -> Unit) =
  Presentation(path, title, "dist/theme/${theme.name.toLower()}.css").apply { block(this) }

class Presentation internal constructor(path: String, val title: String, val theme: String) {

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

  val config = ConfigOptions()

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
    config: SlideConfig = slideConfig {},
    id: String = "",
    content: () -> String
  ) {
    vertSlides += {
      if (id.isNotEmpty())
        this.id = id
      applyConfig(config)
      rawHtml(content.invoke())
    }
  }

  @HtmlTagMarker
  fun rawHtmlSlide(
    config: SlideConfig = slideConfig {},
    id: String = "",
    content: () -> String
  ) {
    slides += {
      section {
        if (id.isNotEmpty())
          this.id = id
        applyConfig(config)
        rawHtml("\n" + content.invoke())
      }
      rawHtml("\n")
    }
  }

  @HtmlTagMarker
  fun VerticalContext.htmlSlide(
    config: SlideConfig = slideConfig {},
    id: String = "",
    content: SECTION.() -> Unit
  ) {
    vertSlides += {
      if (id.isNotEmpty())
        this.id = id
      applyConfig(config)
      content.invoke(this)
    }
  }

  @HtmlTagMarker
  fun htmlSlide(
    config: SlideConfig = slideConfig {},
    id: String = "",
    content: SECTION.() -> Unit
  ) {
    slides += {
      section {
        if (id.isNotEmpty())
          this.id = id
        applyConfig(config)
        content.invoke(this)
      }
      rawHtml("\n")
    }
  }

  @HtmlTagMarker
  fun VerticalContext.markdownSlide(
    config: SlideConfig = slideConfig {},
    id: String = "",
    filename: String = "",
    content: () -> String = { "" },
  ) {
    htmlSlide(config = config, id = id) {
      // If this value is == "" it means read content inline
      attributes["data-markdown"] = filename
      attributes["data-separator"] = ""
      attributes["data-separator-vertical"] = ""

      //if (notes.isNotEmpty())
      //    attributes["data-separator-notes"] = notes

      if (filename.isEmpty())
        script {
          type = "text/template"
          rawHtml("\n" + content.invoke())
        }
    }
  }

  @HtmlTagMarker
  fun markdownSlide(
    config: SlideConfig = slideConfig {},
    id: String = "",
    filename: String = "",
    separator: String = "",
    vertical_separator: String = "",
    notes: String = "^Note:",
    content: () -> String = { "" },
  ) {
    htmlSlide(config = config, id = id) {
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
          rawHtml("\n" + content.invoke())
        }
    }
  }

  @HtmlTagMarker
  fun config(block: ConfigOptions.() -> Unit) = block.invoke(config)

  companion object : KLogging() {
    internal val presentations = mutableMapOf<String, Presentation>()

    fun present() {
      val environment = commandLineEnvironment(emptyArray())
      embeddedServer(CIO, environment).start(wait = true)
    }

    fun print() {
      presentations.forEach { presentation ->
        println(presentation.key)
        println(generatePage(presentation.value, ""))
      }
    }

    fun output(dir: String = "site", srcPrefix: String = "/../revealjs/") {
      require(dir.isNotEmpty()) { "dir must not be empty" }

      File(dir).mkdir()
      presentations.forEach { (key, value) ->
        val file =
          when {
            key == "/" -> {
              File("$dir/index.html")
            }
            key.endsWith(".html") -> {
              File("$dir/$key")
            }
            else -> {
              File("$dir/$key").mkdir()
              File("$dir$key/index.html")
            }
          }
        file.writeText(generatePage(value, srcPrefix))
      }
    }

    private fun SECTION.applyConfig(config: SlideConfig) {
      if (config.transition != Transition.Slide) {
        attributes["data-transition"] = config.transition.asInOut()
      } else {
        if (config.transitionIn != Transition.Slide || config.transitionOut != Transition.Slide)
          attributes["data-transition"] = "${config.transitionIn.asIn()} ${config.transitionOut.asOut()}"
      }

      if (config.speed != Speed.Default)
        attributes["data-transition-speed"] = config.speed.name.toLower()

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