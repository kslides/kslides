package com.kslides

import com.kslides.InternalUtils.indentInclude
import com.kslides.config.PresentationConfig
import com.kslides.config.SlideConfig
import com.kslides.slide.*
import com.pambrose.common.util.nullIfBlank
import com.pambrose.srcref.Api.srcrefUrl
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.css.CssBuilder
import kotlinx.html.*

/**
 * A single presentation — the unit that reveal.js renders as one HTML page. Created via
 * [KSlides.presentation] and populated with slide definitions, per-presentation configuration,
 * and scoped CSS.
 *
 * Not intended to be constructed directly by user code.
 *
 * @property kslides the owning [KSlides] instance; provides access to global config and
 *   shared caches (iframe content, Kroki content).
 */
@KSlidesDslMarker
class Presentation(
  val kslides: KSlides,
) {
  internal val plugins = mutableListOf<String>()
  internal val dependencies = mutableListOf<String>()
  internal val presentationConfig = PresentationConfig()
  internal val slides = mutableListOf<Slide>()
  internal lateinit var finalConfig: PresentationConfig

  /**
   * The URL path (HTTP mode) or output filename/directory (filesystem mode) for this
   * presentation. Defaults to `"/"`, which writes to `docs/index.html` or serves at the server
   * root. Must be unique across presentations and must not collide with a registered static
   * asset directory ([com.kslides.config.KSlidesConfig.httpStaticRoots]).
   */
  var path = "/"

  /**
   * CSS scoped to this presentation, seeded from [KSlides.css]. Appended via the [css] DSL block
   * or `css += "..."` / `css += { ... }`.
   */
  val css by lazy { CssValue(kslides.css) }

  /**
   * Per-presentation list of CSS `<link>` files injected into the generated `<head>`. Seeded
   * from [com.kslides.config.KSlidesConfig.cssFiles]; theme and highlight stylesheets are appended automatically
   * during page rendering.
   */
  val cssFiles by lazy { mutableListOf<CssFile>().apply { addAll(kslides.kslidesConfig.cssFiles) } }

  /**
   * Per-presentation list of JavaScript `<script>` files injected into the generated page.
   * Seeded from [com.kslides.config.KSlidesConfig.jsFiles]; reveal.js plugins are appended automatically when the
   * corresponding [PresentationConfig] flags are enabled.
   */
  val jsFiles by lazy { mutableListOf<JsFile>().apply { addAll(kslides.kslidesConfig.jsFiles) } }

  /**
   * Append CSS (declared via Kotlin's CSS DSL) to this presentation's stylesheet. Can be placed
   * anywhere inside the `presentation{}` block — unlike raw HTML, CSS does not have to appear
   * at the top.
   */
  fun css(block: CssBuilder.() -> Unit) {
    css += block
  }

  /**
   * Configure reveal.js / kslides options scoped to this presentation. Values set here override
   * the global [KSlides.presentationConfig] for this presentation only.
   */
  fun presentationConfig(block: PresentationConfig.() -> Unit) = presentationConfig.block()

  /**
   * Group one or more slides vertically (reveal.js "vertical stacks"). Inside the block, use
   * [markdownSlide], [htmlSlide], or [dslSlide] to add slides; they are stacked top-to-bottom
   * in the order declared and share a single `<section>` wrapper so the whole stack can share a
   * background, CSS class, or [VerticalSlidesContext.slideConfig].
   *
   * @throws IllegalArgumentException if the block adds zero slides.
   */
  fun verticalSlides(block: VerticalSlidesContext.() -> Unit) =
    VerticalSlide(this) { div, slide, useHttp ->
      div.apply {
        (slide as VerticalSlide)
          .verticalContext
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

  /**
   * Add a horizontal Markdown slide. Content comes from either a `content {}` block (inline
   * Markdown) or a `filename` pointing at an external `.md` file.
   *
   * @param slideContent DSL block to configure the slide (content, id, classes,
   *   [HorizontalMarkdownSlide.filename], etc.).
   * @throws IllegalArgumentException at render time if neither `content {}` nor `filename` is set.
   */
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

  /**
   * Add a Markdown slide inside a [verticalSlides] block. Same semantics as the top-level
   * [markdownSlide] but registered with the enclosing [VerticalSlidesContext].
   */
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

  /**
   * Add a horizontal slide whose content is authored with the kotlinx.html DSL. This is the only
   * slide type that supports the extension DSLs [com.kslides.playground], [com.kslides.diagram],
   * [com.kslides.codeSnippet], and (from the `kslides-letsplot` module) `letsPlot{}`.
   *
   * @throws IllegalArgumentException at render time if the slide has no `content{}` block.
   */
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

  /**
   * Add a kotlinx.html DSL slide inside a [verticalSlides] block. Same semantics as the
   * top-level [dslSlide] but registered with the enclosing [VerticalSlidesContext].
   */
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

  /**
   * Add a horizontal slide whose content is a raw HTML string supplied via `content {}`.
   *
   * @throws IllegalArgumentException at render time if the slide has no `content{}` block.
   */
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

  /**
   * Add a raw-HTML slide inside a [verticalSlides] block. Same semantics as the top-level
   * [htmlSlide] but registered with the enclosing [VerticalSlidesContext].
   */
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

  /**
   * Generate a Markdown "meta" slide that embeds a highlighted code excerpt from [source]
   * between `// <token> begin` and `// <token> end` markers, and appends a "GitHub Source"
   * link pointing at the same region on `master`. Primarily used by the kslides example deck
   * to explain its own DSL.
   *
   * @param source path (relative to the repo root) of the source file to excerpt.
   * @param token the begin/end token bracketing the excerpt.
   * @param title heading shown above the code block.
   * @param highlightPattern reveal.js `data-line-numbers` pattern (e.g. `"1-3|5"`).
   * @param id optional slide id.
   * @param language syntax-highlighting language for the code fence.
   */
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
        ${this@Presentation.githubLink(this@Presentation.srcref(token))}
        """
      }
    }
  }
  // slideDefinition end

  /**
   * Vertical-stack variant of [slideDefinition]. Identical semantics but registers the generated
   * Markdown slide inside the enclosing [VerticalSlidesContext].
   */
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
        ${this@Presentation.githubLink(this@Presentation.srcref(token))}
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
        when (autoSlide) {
          is Boolean if !autoSlide -> {
            append("$INDENT${toJsValue("autoSlide", autoSlide)},\n")
            appendLine()
          }

          is Int -> {
            if (autoSlide > 0) {
              append("$INDENT${toJsValue("autoSlide", autoSlide)},\n")
              appendLine()
            }
          }

          else -> {
            error("Invalid value for autoSlide: $autoSlide")
          }
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

          else -> {
            error("Invalid value for slideNumber: $slideNumber")
          }
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

          else -> {
            error("Invalid value for scrollProgress: $scrollProgress")
          }
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

          else -> {
            error("Invalid value for scrollSnap: $scrollSnap")
          }
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

/**
 * A directory under reveal.js's static asset root that kslides should expose when serving over
 * HTTP. Registered on [com.kslides.config.KSlidesConfig.httpStaticRoots].
 *
 * @property dirname directory name relative to `src/main/resources/revealjs/`.
 */
data class StaticRoot(
  val dirname: String,
)

/**
 * A JavaScript file referenced from the generated `<body>`. The [filename] may be a relative
 * path (resolved against the reveal.js static root) or an absolute `http(s)://` URL.
 */
data class JsFile(
  val filename: String,
)

/**
 * A CSS file referenced from the generated `<head>`. The [filename] may be a relative path
 * (resolved against the reveal.js static root) or an absolute `http(s)://` URL. The optional
 * [id] is emitted as the `<link>`'s `id` attribute — used internally to tag theme / highlight
 * stylesheets.
 */
data class CssFile(
  val filename: String,
  val id: String = "",
)

private fun SECTION.processMarkdown(
  s: MarkdownSlide,
  config: SlideConfig,
) {
  if (s.filename.isBlank()) {
    s
      .private_markdownBlock()
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

private fun DIV.processDsl(s: DslSlide) {
  section(s.classes.nullIfBlank()) {
    s.processSlide(this)
    s.private_section = this // TODO This is a hack that will go away when context receivers work
    s.private_dslBlock(this)
    require(s.private_dslAssigned) { "dslSlide missing content{} block" }
  }.also { rawHtml("\n") }
}

private fun DIV.processHtml(
  s: HtmlSlide,
  config: SlideConfig,
) {
  section(s.classes.nullIfBlank()) {
    s.processSlide(this)
    require(s.private_htmlAssigned) { "htmlSlide missing content{} block" }
    s
      .private_htmlBlock()
      .indentInclude(config.indentToken)
      .let { if (!config.disableTrimIndent) it.trimIndent() else it }
      .also { rawHtml("\n$it") }
  }.also { rawHtml("\n") }
}
