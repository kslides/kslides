package com.kslides

import com.github.pambrose.common.util.*
import kotlinx.css.*
import kotlinx.html.*
import mu.*

class Presentation(val kslides: KSlides) {
  internal val plugins = mutableListOf<String>()
  internal val dependencies = mutableListOf<String>()
  internal val presentationConfig = PresentationConfig()
  internal lateinit var finalConfig: PresentationConfig
  internal val slides = mutableListOf<Slide>()

  internal val jsFiles = mutableListOf(JsFile("dist/reveal.js"))
  internal val cssFiles =
    mutableListOf(
      CssFile("dist/reveal.css"),
      CssFile("dist/reset.css"),
    )

  // Initialize css with the global css value
  val css by lazy { AppendableString(kslides.css.let { if (it.isNotBlank()) "$it\n" else "" }) }
  var path = "/"

  internal fun validatePath() {
    require(path.removePrefix("/") !in kslides.staticRoots) { "Invalid presentation path: \"${"/${path.removePrefix("/")}"}\"" }

    (if (path.startsWith("/")) path else "/$path").also { adjustedPath ->
      require(!kslides.presentationMap.containsKey(adjustedPath)) { "Presentation path already defined: \"$adjustedPath\"" }
      kslides.presentationMap[adjustedPath] = this
    }
  }

  internal fun assignCssFiles() {
    cssFiles += CssFile("dist/theme/${finalConfig.theme.name.toLower()}.css", "theme")
    cssFiles += CssFile("plugin/highlight/${finalConfig.highlight.name.toLower()}.css", "highlight-theme")

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

  @KSlidesDslMarker
  fun css(block: CssBuilder.() -> Unit) {
    css += "${CssBuilder().apply(block)}\n"
  }

  @KSlidesDslMarker
  fun presentationConfig(block: PresentationConfig.() -> Unit) = block(presentationConfig)

  @KSlidesDslMarker
  fun verticalSlides(block: VerticalSlideContext.() -> Unit) =
    VerticalSlide(this) { div, slide ->
      div.apply {
        (slide as VerticalSlide).verticalContext
          .also { vcontext ->
            // Calling resetContext() is a bit of a hack. It is required because the vertical slide lambdas are executed
            // for both http and the filesystem. Without resetting the slide context, you will end up with double the slides
            vcontext.resetContext()
            block(vcontext)
            section(classes = vcontext.classes.nullIfBlank()) {
              vcontext.id.also { if (it.isNotEmpty()) id = it }

              // Apply config items for all the slides in the vertical slide
              vcontext.slideConfig.applyConfig(this)
              vcontext.slideConfig.applyMarkdownItems(this)

              vcontext.verticalSlides
                .forEach { verticalSlide ->
                  verticalSlide.content(div, verticalSlide)
                  rawHtml("\n")
                }
            }.also { rawHtml("\n") }
          }
      }
    }.also { slides += it }

  @KSlidesDslMarker
  fun markdownSlide(slideContent: MarkdownSlide.() -> Unit = {}) =
    MarkdownSlide(this) { div, slide ->
      div.apply {
        (slide as MarkdownSlide).also { s ->
          slideContent(s)
          section(classes = s.classes.nullIfBlank()) {
            s.processSlide(this)
            require(s.filename.isNotBlank() || s.markdownAssigned) { "markdownSlide missing content { } section" }

            // If this value is == "" it means read content inline
            attributes["data-markdown"] = s.filename

            if (s.charset.isNotBlank())
              attributes["data-charset"] = s.charset

            s.mergedConfig.applyMarkdownItems(this)

            if (s.filename.isEmpty()) {
              s.markdownBlock().also { markdown ->
                if (markdown.isNotBlank())
                  script("text/template") {
                    markdown
                      .indentInclude(s.indentToken)
                      .let { if (!s.disableTrimIndent) it.trimIndent() else it }
                      .also { rawHtml("\n$it\n") }
                  }
              }
            }
          }.also { rawHtml("\n") }
        }
      }
    }.also { slides += it }

  @KSlidesDslMarker
  fun VerticalSlideContext.markdownSlide(slideContent: VerticalMarkdownSlide.() -> Unit = { }) =
    VerticalMarkdownSlide(this@Presentation) { div, slide ->
      div.apply {
        (slide as VerticalMarkdownSlide).also { s ->
          slideContent(s)
          section(classes = s.classes.nullIfBlank()) {
            s.processSlide(this)
            require(s.filename.isNotBlank() || s.markdownAssigned) { "markdownSlide missing content { } section" }
            // If this value is == "" it means read content inline
            attributes["data-markdown"] = s.filename

            if (s.charset.isNotBlank())
              attributes["data-charset"] = s.charset

            // These are not applicable for vertical markdown slides
            attributes["data-separator"] = ""
            attributes["data-separator-vertical"] = ""

            s.mergedConfig.apply {
              attributes["data-separator-notes"] = markdownNotesSeparator
            }

            //if (notes.isNotBlank())
            //    attributes["data-separator-notes"] = notes

            if (s.filename.isEmpty()) {
              s.markdownBlock().also { markdown ->
                if (markdown.isNotBlank())
                  script("text/template") {
                    markdown
                      .indentInclude(s.indentToken)
                      .let { if (!s.disableTrimIndent) it.trimIndent() else it }
                      .also { rawHtml("\n$it\n") }
                  }
              }
            }
          }.also { rawHtml("\n") }
        }
      }
    }.also { verticalSlides += it }

  @KSlidesDslMarker
  fun dslSlide(slideContent: DslSlide.() -> Unit) =
    DslSlide(this) { div, slide ->
      div.apply {
        (slide as DslSlide).also { s ->
          slideContent(s)
          section(classes = s.classes.nullIfBlank()) {
            if (s.style.isNotBlank())
              style = s.style
            s.processSlide(this)
            require(s.dslAssigned) { "dslSlide missing content { } section" }
            s.dslBlock(this, s)
          }.also { rawHtml("\n") }
        }
      }
    }.also { slides += it }

  @KSlidesDslMarker
  fun VerticalSlideContext.dslSlide(slideContent: VerticalDslSlide.() -> Unit) =
    VerticalDslSlide(this@Presentation) { div, slide ->
      div.apply {
        (slide as VerticalDslSlide).also { s ->
          slideContent(s)
          section(classes = s.classes.nullIfBlank()) {
            if (s.style.isNotBlank())
              style = s.style
            s.processSlide(this)
            require(s.dslAssigned) { "dslSlide missing content { } section" }
            s.dslBlock(this, s)
          }.also { rawHtml("\n") }
        }
      }
    }.also { verticalSlides += it }

  @KSlidesDslMarker
  fun htmlSlide(slideContent: HtmlSlide.() -> Unit) =
    HtmlSlide(this) { div, slide ->
      div.apply {
        (slide as HtmlSlide).also { s ->
          slideContent(s)
          section(classes = s.classes.nullIfBlank()) {
            if (s.style.isNotBlank())
              style = s.style
            s.processSlide(this)
            require(s.htmlAssigned) { "htmlSlide missing content { } section" }
            s.htmlBlock()
              .indentInclude(s.indentToken)
              .let { if (!s.disableTrimIndent) it.trimIndent() else it }
              .also { rawHtml("\n$it") }
          }.also { rawHtml("\n") }
        }
      }
    }.also {
      slides += it
    }

  @KSlidesDslMarker
  fun VerticalSlideContext.htmlSlide(slideContent: VerticalHtmlSlide.() -> Unit) =
    VerticalHtmlSlide(this@Presentation) { div, slide ->
      div.apply {
        (slide as VerticalHtmlSlide).also { s ->
          slideContent(s)
          section(classes = s.classes.nullIfBlank()) {
            if (s.style.isNotBlank())
              style = s.style
            s.processSlide(this)
            require(s.htmlAssigned) { "htmlSlide missing content { } section" }
            s.htmlBlock()
              .indentInclude(s.indentToken)
              .let { if (!s.disableTrimIndent) it.trimIndent() else it }
              .also { rawHtml("\n$it") }
          }.also { rawHtml("\n") }
        }
      }
    }.also { verticalSlides += it }

  @KSlidesDslMarker
  // slideDefinition begin
  fun slideDefinition(
    source: String,
    token: String,
    title: String = "Slide Definition",
    lines: String = "",
    id: String = "",
    language: String = "kotlin",
  ) {
    markdownSlide {
      if (id.isNotBlank()) this.id = id
      slideConfig { markdownNotesSeparator = "^^" }
      content {
        """
        ## $title    
        ```$language $lines
        ${includeFile(source, beginToken = "$token begin", endToken = "$token end")}
        ```
        """
      }
    }
  }
  // slideDefinition end

  @KSlidesDslMarker
  fun VerticalSlideContext.slideDefinition(
    source: String,
    token: String,
    title: String = "Slide Definition",
    lines: String = "",
    id: String = "",
    language: String = "kotlin",
  ) {
    markdownSlide {
      if (id.isNotBlank()) this.id = id
      slideConfig { markdownNotesSeparator = "^^" }
      content {
        """
        ## $title    
        ```$language $lines
        ${includeFile(source, beginToken = "$token begin", endToken = "$token end")}
        ```
        """
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
  internal val slideConfig = SlideConfig().apply { init() }
  internal val verticalSlides = mutableListOf<VerticalSlide>()
  var id = ""
  var classes = ""

  @KSlidesDslMarker
  fun slideConfig(block: SlideConfig.() -> Unit) = block(slideConfig)

  internal fun resetContext() {
    verticalSlides.clear()
  }
}

class JsFile(val filename: String)

class CssFile(val filename: String, val id: String = "")