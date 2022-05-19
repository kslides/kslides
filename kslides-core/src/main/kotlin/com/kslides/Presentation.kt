package com.kslides

import com.github.pambrose.common.util.*
import com.kslides.InternalUtils.indentInclude
import com.kslides.config.*
import com.kslides.slide.*
import kotlinx.css.*
import kotlinx.html.*
import mu.*

class Presentation(val kslides: KSlides) {
  internal val plugins = mutableListOf<String>()
  internal val dependencies = mutableListOf<String>()
  internal val presentationConfig = PresentationConfig()
  internal lateinit var finalConfig: PresentationConfig
  internal val slides = mutableListOf<Slide>()

  // User variables
  var path = "/"
  val css by lazy { CssValue(kslides.css) }
  val cssFiles by lazy { mutableListOf<CssFile>().apply { addAll(kslides.kslidesConfig.cssFiles) } }
  val jsFiles by lazy { mutableListOf<JsFile>().apply { addAll(kslides.kslidesConfig.jsFiles) } }

  @KSlidesDslMarker
  fun css(block: CssBuilder.() -> Unit) {
    css += block
  }

  @KSlidesDslMarker
  fun presentationConfig(block: PresentationConfig.() -> Unit) = block(presentationConfig)

  @KSlidesDslMarker
  fun verticalSlides(block: VerticalSlidesContext.() -> Unit) =
    VerticalSlide(this) { div, slide, useHttp ->
      div.apply {
        (slide as VerticalSlide).verticalContext
          .also { vcontext ->
            // Calling resetContext() is a bit of a hack. It is required because the vertical slide lambdas are executed
            // for both http and the filesystem. Without resetting the slide context, you will end up with double the slides
            vcontext.resetContext()
            block(vcontext)

            require(vcontext.verticalSlides.isNotEmpty()) {
              throw IllegalArgumentException("A verticalSlides{} block requires one or more slides")
            }
            section(vcontext.classes.nullIfBlank()) {
              vcontext.id.also { if (it.isNotBlank()) id = it }
              vcontext.style.also { if (it.isNotBlank()) style = it }

              // Apply config items for all the slides in the vertical slide
              vcontext.slideConfig.applyConfig(this)
              vcontext.slideConfig.applyMarkdownItems(this)
              vcontext.verticalSlides
                .forEach { verticalSlide ->
                  verticalSlide.content(div, verticalSlide, useHttp)
                  rawHtml("\n")
                }
            }.also { rawHtml("\n") }
          }
      }
    }.also { slides += it }

  @KSlidesDslMarker
  fun markdownSlide(slideContent: HortizontalMarkdownSlide.() -> Unit = {}) =
    HortizontalMarkdownSlide(this) { div, slide, _ ->
      div.apply {
        (slide as HortizontalMarkdownSlide).also { s ->
          slideContent(s)
          section(s.classes.nullIfBlank()) {
            s.processSlide(this)
            require(s.filename.isNotBlank() || s.markdownAssigned) { "markdownSlide missing content{} block" }

            // If this value is == "" it means read content inline
            attributes["data-markdown"] = s.filename

            val config = s.mergedSlideConfig
            config.markdownCharset.also { charset ->
              if (charset.isNotBlank()) attributes["data-charset"] = charset
            }

            config.applyMarkdownItems(this)

            processMarkdown(s, config)
          }.also { rawHtml("\n") }
        }
      }
    }.also { slides += it }

  @KSlidesDslMarker
  fun VerticalSlidesContext.markdownSlide(slideContent: VerticalMarkdownSlide.() -> Unit = { }) =
    VerticalMarkdownSlide(this@Presentation) { div, slide, _ ->
      div.apply {
        (slide as VerticalMarkdownSlide).also { s ->
          slideContent(s)
          section(s.classes.nullIfBlank()) {
            s.processSlide(this)
            require(s.filename.isNotBlank() || s.markdownAssigned) { "markdownSlide missing content{} block" }

            // If this value is == "" it means read content inline
            attributes["data-markdown"] = s.filename

            val config = s.mergedSlideConfig
            config.markdownCharset.also { charset ->
              if (charset.isNotBlank()) attributes["data-charset"] = charset
            }

            attributes["data-separator-notes"] = config.markdownNotesSeparator

            // These are not applicable for vertical markdown slides
            attributes["data-separator"] = ""
            attributes["data-separator-vertical"] = ""

            processMarkdown(s, config)
          }.also { rawHtml("\n") }
        }
      }
    }.also { verticalSlides += it }

  private fun DIV.processDsl(s: DslSlide) {
    section(s.classes.nullIfBlank()) {
      s.processSlide(this)
      s._section = this // TODO This is a hack that will go away when context receivers work
      s._dslBlock(this)
      require(s._dslAssigned) { "dslSlide missing content{} block" }
    }.also { rawHtml("\n") }
  }

  @KSlidesDslMarker
  fun dslSlide(slideContent: HorizontalDslSlide.() -> Unit) =
    HorizontalDslSlide(this) { div, slide, useHttp ->
      div.apply {
        (slide as HorizontalDslSlide)
          .also { s ->
            s._iframeCount = 1
            s._useHttp = useHttp
            slideContent(s)
            processDsl(s)
          }
      }
    }.also { slides += it }

  @KSlidesDslMarker
  fun VerticalSlidesContext.dslSlide(slideContent: VerticalDslSlide.() -> Unit) =
    VerticalDslSlide(this@Presentation) { div, slide, useHttp ->
      div.apply {
        (slide as VerticalDslSlide)
          .also { s ->
            s._iframeCount = 1
            s._useHttp = useHttp
            slideContent(s)
            processDsl(s)
          }
      }
    }.also { verticalSlides += it }

  private fun DIV.processHtml(s: HtmlSlide, config: SlideConfig) {
    section(s.classes.nullIfBlank()) {
      s.processSlide(this)
      require(s._htmlAssigned) { "htmlSlide missing content{} block" }
      s._htmlBlock()
        .indentInclude(config.indentToken)
        .let { if (!config.disableTrimIndent) it.trimIndent() else it }
        .also { rawHtml("\n$it") }
    }.also { rawHtml("\n") }
  }

  @KSlidesDslMarker
  fun htmlSlide(slideContent: HorizontalHtmlSlide.() -> Unit) =
    HorizontalHtmlSlide(this) { div, slide, _ ->
      div.apply {
        (slide as HorizontalHtmlSlide)
          .also { s ->
            slideContent(s)
            processHtml(s, s.mergedSlideConfig)
          }
      }
    }.also { slides += it }

  @KSlidesDslMarker
  fun VerticalSlidesContext.htmlSlide(slideContent: VerticalHtmlSlide.() -> Unit) =
    VerticalHtmlSlide(this@Presentation) { div, slide, _ ->
      div.apply {
        (slide as VerticalHtmlSlide)
          .also { s ->
            slideContent(s)
            processHtml(s, s.mergedSlideConfig)
          }
      }
    }.also { verticalSlides += it }

  @KSlidesDslMarker
  // slideDefinition begin
  fun slideDefinition(
    source: String,
    token: String,
    title: String = "Slide Definition",
    highlightPattern: String = "[]",
    id: String = "",
    language: String = "kotlin",
  ) {
    markdownSlide {
      if (id.isNotBlank()) this.id = id
      content {
        """
        ## $title    
        ```$language $highlightPattern
        ${include(source, beginToken = "$token begin", endToken = "$token end")}
        ```
        """
      }
    }
  }
  // slideDefinition end

  @KSlidesDslMarker
  fun VerticalSlidesContext.slideDefinition(
    source: String,
    token: String,
    title: String = "Slide Definition",
    highlightPattern: String = "[]",
    id: String = "",
    language: String = "kotlin",
  ) {
    markdownSlide {
      if (id.isNotBlank()) this.id = id
      slideConfig {
        markdownNotesSeparator = "^^"
      }
      content {
        """
        ## $title    
        ```$language $highlightPattern
        ${include(source, beginToken = "$token begin", endToken = "$token end")}
        ```
        """
      }
    }
  }

  internal fun validatePath() {
    require(path.removePrefix("/") !in kslides.kslidesConfig.httpStaticRoots.map { it.dirname }) {
      "Invalid presentation path: \"${"/${path.removePrefix("/")}"}\""
    }

    (if (path.startsWith("/")) path else "/$path").also { adjustedPath ->
      require(!kslides.presentationMap.containsKey(adjustedPath)) { "Presentation with path already defined: \"$adjustedPath\"" }
      kslides.presentationMap[adjustedPath] = this
    }
  }

  internal fun assignCssFiles() {
    cssFiles += CssFile(finalConfig.theme.cssSrc, "theme")
    cssFiles += CssFile(finalConfig.highlight.cssSrc, "highlight-theme")

    if (finalConfig.enableCodeCopy)
      cssFiles += CssFile("plugin/copycode/copycode.css")
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
      // Required for copycode.js
      jsFiles += JsFile("https://cdnjs.cloudflare.com/ajax/libs/clipboard.js/2.0.11/clipboard.min.js")
      jsFiles += JsFile("plugin/copycode/copycode.js")
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

  private fun SECTION.processMarkdown(s: MarkdownSlide, config: SlideConfig) {
    if (s.filename.isBlank()) {
      s._markdownBlock()
        .also { markdown ->
          if (markdown.isNotBlank())
            script("text/template") {
              markdown
                .indentInclude(config.indentToken)
                .let { if (!config.disableTrimIndent) it.trimIndent() else it }
                .also { rawHtml("\n$it\n") }
            }
        }
    }
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

  internal fun toJs(config: PresentationConfig, srcPrefix: String) =
    buildString {
      config.revealjsManagedValues.also { vals ->
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

      config.menuConfig.revealjsManagedValues.also { vals ->
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

      config.copyCodeConfig.revealjsManagedValues.also { vals ->
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

data class StaticRoot(val dirname: String)

data class JsFile(val filename: String)

data class CssFile(val filename: String, val id: String = "")