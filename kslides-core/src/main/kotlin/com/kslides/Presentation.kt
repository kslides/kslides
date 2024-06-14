package com.kslides

import com.github.pambrose.common.util.nullIfBlank
import com.kslides.InternalUtils.indentInclude
import com.kslides.config.PresentationConfig
import com.kslides.config.SlideConfig
import com.kslides.slide.*
import com.pambrose.srcref.Api.srcrefUrl
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.css.CssBuilder
import kotlinx.html.*

class Presentation(val kslides: KSlides) {
  internal val plugins = mutableListOf<String>()
  internal val dependencies = mutableListOf<String>()
  internal val presentationConfig = PresentationConfig()
  internal val slides = mutableListOf<Slide>()
  internal lateinit var finalConfig: PresentationConfig

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
  fun presentationConfig(block: PresentationConfig.() -> Unit) = presentationConfig.block()

  @KSlidesDslMarker
  fun verticalSlides(block: VerticalSlidesContext.() -> Unit) =
    VerticalSlide(this) { div, slide, useHttp ->
      div.apply {
        (slide as VerticalSlide).verticalContext
          .also { vcontext ->
            // Calling resetContext() is a bit of a hack. It is required because the vertical slide lambdas are executed
            // for both http and the filesystem. Without resetting the slide context, you will end up with double the slides
            vcontext.resetContext()
            vcontext.block()

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
  fun markdownSlide(slideContent: HorizontalMarkdownSlide.() -> Unit = {}) =
    HorizontalMarkdownSlide(this) { div, slide, _ ->
      div.apply {
        (slide as HorizontalMarkdownSlide).also { s ->
          s.slideContent()
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
          s.slideContent()
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
      s.private_section = this // TODO This is a hack that will go away when context receivers work
      s.private_dslBlock(this)
      require(s.private_dslAssigned) { "dslSlide missing content{} block" }
    }.also { rawHtml("\n") }
  }

  @KSlidesDslMarker
  fun dslSlide(slideContent: HorizontalDslSlide.() -> Unit) =
    HorizontalDslSlide(this) { div, slide, useHttp ->
      div.apply {
        (slide as HorizontalDslSlide)
          .also { s ->
            s.private_iframeCount = 1
            s.private_useHttp = useHttp
            s.slideContent()
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
            s.private_iframeCount = 1
            s.private_useHttp = useHttp
            s.slideContent()
            processDsl(s)
          }
      }
    }.also { verticalSlides += it }

  private fun DIV.processHtml(
    s: HtmlSlide,
    config: SlideConfig,
  ) {
    section(s.classes.nullIfBlank()) {
      s.processSlide(this)
      require(s.private_htmlAssigned) { "htmlSlide missing content{} block" }
      s.private_htmlBlock()
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
            s.slideContent()
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
            s.slideContent()
            processHtml(s, s.mergedSlideConfig)
          }
      }
    }.also { verticalSlides += it }

  private fun srcref(token: String) =
    srcrefUrl(
      account = "kslides",
      repo = "kslides",
      path = "kslides-examples/src/main/kotlin/Slides.kt",
      beginRegex = "//\\s*$token\\s+begin",
      beginOffset = 1,
      endRegex = "//\\s*$token\\s+end",
      endOffset = -1,
      escapeHtml4 = true,
    )

  private fun githubLink(href: String) = """<a id="ghsrc" href="$href" target="_blank">GitHub Source</a>"""

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
        ${githubLink(srcref(token))}
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
        ${githubLink(srcref(token))}
        """
      }
    }
  }

  internal fun validatePath() {
    require(path.removePrefix("/") !in kslides.kslidesConfig.httpStaticRoots.map { it.dirname }) {
      "Invalid presentation path: \"${"/${path.removePrefix("/")}"}\""
    }

    (if (path.startsWith("/")) path else "/$path").also { adjustedPath ->
      require(!kslides.presentationMap.containsKey(adjustedPath)) {
        "Presentation with path already defined: \"$adjustedPath\""
      }
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

  private fun SECTION.processMarkdown(
    s: MarkdownSlide,
    config: SlideConfig,
  ) {
    if (s.filename.isBlank()) {
      s.private_markdownBlock()
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

  private fun toJsValue(
    key: String,
    value: Any,
  ) = when (value) {
    is Boolean, is Number -> "$key: $value"
    is String -> "$key: '$value'"
    is Transition -> "$key: '${value.name.lowercase()}'"
    is ViewType -> "$key: '${value.name.lowercase()}'"
    is ScrollProgress -> "$key: '${value.name.lowercase()}'"
    is ScrollLayout -> "$key: '${value.name.lowercase()}'"
    is ScrollSnap -> "$key: '${value.name.lowercase()}'"
    is Speed -> "$key: '${value.name.lowercase()}'"
    is List<*> -> "$key: [${value.joinToString(", ") { "'$it'" }}]"
    else -> throw IllegalArgumentException("Invalid value for $key: $value")
  }

  internal fun toJs(
    config: PresentationConfig,
    srcPrefix: String,
  ) = buildString {
    config.revealjsManagedValues.also { vals ->
      if (vals.isNotEmpty()) {
        vals.forEach { (k, v) ->
          append("$INDENT${toJsValue(k, v)},\n")
        }
        appendLine()
      }
    }

    config.autoSlide
      .also { autoSlide ->
        when {
          autoSlide is Boolean && !autoSlide -> {
            append("$INDENT${toJsValue("autoSlide", autoSlide)},\n")
            appendLine()
          }

          autoSlide is Int -> {
            if (autoSlide > 0) {
              append("$INDENT${toJsValue("autoSlide", autoSlide)},\n")
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
              append("$INDENT${toJsValue("slideNumber", slideNumber)},\n")
              appendLine()
            }
          }

          is String -> {
            append("$INDENT${toJsValue("slideNumber", slideNumber)},\n")
            appendLine()
          }

          else -> error("Invalid value for slideNumber: $slideNumber")
        }
      }

    config.jumpToSlide
      .also { jumpToSlide ->
        if (!jumpToSlide) {
          append("$INDENT${toJsValue("jumpToSlide", jumpToSlide)},\n")
          appendLine()
        }
      }

    config.view
      .also { view ->
        if (view == ViewType.SCROLL) {
          append("$INDENT${toJsValue("view", view)},\n")
          appendLine()
        }
      }

    config.scrollLayout
      .also { scrollLayout ->
        append("$INDENT${toJsValue("scrollLayout", scrollLayout)},\n")
        appendLine()
      }

    config.scrollProgress
      .also { scrollProgress ->
        when (scrollProgress) {
          is Boolean,
          is ScrollProgress,
            -> {
            append("$INDENT${toJsValue("scrollProgress", scrollProgress)},\n")
            appendLine()
          }

          else -> error("Invalid value for scrollProgress: $scrollProgress")
        }
      }

    config.scrollActivationWidth
      .also { scrollActivationWidth ->
        if (scrollActivationWidth != 0) {
          append("$INDENT${toJsValue("scrollActivationWidth", scrollActivationWidth)},\n")
          appendLine()
        }
      }

    config.scrollSnap
      .also { scrollSnap ->
        when (scrollSnap) {
          is Boolean,
          is ScrollSnap,
            -> {
            append("$INDENT${toJsValue("scrollSnap", scrollSnap)},\n")
            appendLine()
          }

          else -> error("Invalid value for scrollSnap: $scrollSnap")
        }
      }

    config.menuConfig.revealjsManagedValues.also { valMap ->
      if (valMap.isNotEmpty()) {
        appendLine(
          buildString {
            appendLine("menu: {")
            appendLine(valMap.map { (k, v) -> "\t${toJsValue(k, v)}" }.joinToString(",\n"))
            appendLine("},")
          }.prependIndent(INDENT),
        )
      }
    }

    config.copyCodeConfig.revealjsManagedValues.also { valMap ->
      if (valMap.isNotEmpty()) {
        appendLine(
          buildString {
            appendLine("copycode: {")
            appendLine(valMap.map { (k, v) -> "\t${toJsValue(k, v)}" }.joinToString(",\n"))
            appendLine("},")
          }.prependIndent(INDENT),
        )
      }
    }

    if (dependencies.isNotEmpty()) {
      appendLine(
        buildString {
          appendLine("dependencies: [")
          appendLine(
            dependencies.joinToString(",\n") {
              "\t{ src: '${if (it.startsWith("http")) it else "$srcPrefix$it"}' }"
            },
          )
          appendLine("],")
        }.prependIndent(INDENT),
      )
    }

    appendLine("${INDENT}plugins: [ ${plugins.joinToString(", ")} ]")
  }

  companion object {
    private val logger = KotlinLogging.logger {}
    private const val INDENT = "\t\t\t"
  }
}

data class StaticRoot(val dirname: String)

data class JsFile(val filename: String)

data class CssFile(val filename: String, val id: String = "")
