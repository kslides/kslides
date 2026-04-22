package com.kslides.slide

import com.kslides.KSlidesDslMarker
import com.kslides.Presentation
import com.pambrose.common.util.toPath
import kotlinx.html.*

/**
 * Shared contract for slides whose content is authored with the kotlinx.html DSL. Implemented by
 * [HorizontalDslSlide] and [VerticalDslSlide]. This is the only slide type that can host the
 * extension DSLs ([com.kslides.playground], [com.kslides.diagram], [com.kslides.codeSnippet],
 * `letsPlot{}`).
 *
 * Members prefixed with `private_` are implementation details (crossing the DSL/renderer
 * boundary) and should not be called by user code. The exposed `global*Config` /
 * `presentation*Config` / `*Path` accessors are intended for extension DSLs that need to look
 * up merged configuration and output paths when generating iframe content.
 */
@KSlidesDslMarker
interface DslSlide {
  /** The [Presentation] that owns this slide. */
  val presentation: Presentation

  /** Implementation detail — do not use. */
  var private_section: SECTION? // TODO This is a hack that will go away when context receivers work

  /** Implementation detail — unique slide id used to generate iframe filenames. */
  val private_slideId: Int

  /** Implementation detail — do not use. */
  var private_useHttp: Boolean

  /** Implementation detail — do not use. */
  var private_dslAssigned: Boolean

  /** Implementation detail — do not use. */
  var private_dslBlock: SECTION.() -> Unit

  /** Implementation detail — counter for per-slide iframe filenames. */
  var private_iframeCount: Int

  /** CSS class list applied to the `<section>`. */
  var classes: String

  /** HTML `id` applied to the `<section>`; blank omits the attribute. */
  var id: String

  /** Inline CSS applied to the `<section>`; blank omits the attribute. */
  var style: String

  /** Global default [com.kslides.config.PlaygroundConfig] for [com.kslides.playground] calls. */
  val globalPlaygroundConfig get() = presentation.kslides.globalPresentationConfig.playgroundConfig

  /** Per-presentation [com.kslides.config.PlaygroundConfig] overrides. */
  val presentationPlaygroundConfig get() = presentation.presentationConfig.playgroundConfig

  /** Filesystem directory where Playground iframe HTML files are written. */
  val playgroundPath get() = presentation.kslides.outputConfig.playgroundPath

  /** Global default [com.kslides.config.LetsPlotIframeConfig] for `letsPlot{}` calls. */
  val globalLetsPlotConfig get() = presentation.kslides.globalPresentationConfig.letsPlotIframeConfig

  /** Per-presentation [com.kslides.config.LetsPlotIframeConfig] overrides. */
  val presentationLetsPlotConfig get() = presentation.presentationConfig.letsPlotIframeConfig

  /** Filesystem directory where Lets-Plot iframe HTML files are written. */
  val letsPlotPath get() = presentation.kslides.outputConfig.letsPlotPath

  /** Version of the Lets-Plot JS runtime loaded by generated Lets-Plot iframes. */
  val letsPlotJsVersion get() = presentation.kslides.kslidesConfig.letsPlotJsVersion

  /** Global default [com.kslides.config.DiagramConfig] for [com.kslides.diagram] calls. */
  val globalDiagramConfig get() = presentation.kslides.globalPresentationConfig.diagramConfig

  /** Per-presentation [com.kslides.config.DiagramConfig] overrides. */
  val presentationDiagramConfig get() = presentation.presentationConfig.diagramConfig

  /** Filesystem directory where Kroki diagram files are written. */
  val krokiPath get() = presentation.kslides.outputConfig.krokiPath

  /** Apply the slide's attributes to the enclosing `<section>`. Invoked by the renderer. */
  fun processSlide(section: SECTION)

  /**
   * Generate a unique per-slide filename for iframe or image content. Each call increments the
   * internal counter so multiple playgrounds / diagrams / plots on the same slide get distinct
   * filenames.
   *
   * @param suffix file extension without a leading dot.
   */
  fun newFilename(suffix: String = "html") = "slide-$private_slideId-${private_iframeCount++}.$suffix"

  /** Resolve [filename] relative to the configured Playground output directory. */
  fun playgroundFilename(
    filename: String,
  ) = listOf(presentation.kslides.outputConfig.playgroundDir, filename).toPath(addPrefix = false, addTrailing = false)

  /** Resolve [filename] relative to the configured Lets-Plot output directory. */
  fun letsPlotFilename(
    filename: String,
  ) = listOf(presentation.kslides.outputConfig.letsPlotDir, filename).toPath(addPrefix = false, addTrailing = false)

  /** Resolve [filename] relative to the configured Kroki output directory. */
  fun krokiFilename(
    filename: String,
  ) = listOf(presentation.kslides.outputConfig.krokiDir, filename).toPath(addPrefix = false, addTrailing = false)
}

/**
 * A top-level DSL slide. Add via [com.kslides.Presentation.dslSlide]; populate the slide body
 * inside [content].
 */
class HorizontalDslSlide(
  override val presentation: Presentation,
  content: SlideArgs,
) : HorizontalSlide(presentation, content),
  DslSlide {
  override var private_section: SECTION? = null
  override var private_dslBlock: SECTION.() -> Unit = { }
  override var private_iframeCount = 1
  override var private_useHttp: Boolean = false
  override var private_dslAssigned = false

  /**
   * Supply the slide body using the kotlinx.html DSL. Required — blank throws at render time.
   * Inside the block you can call extension DSLs such as [com.kslides.playground],
   * [com.kslides.diagram], [com.kslides.codeSnippet], and `letsPlot{}` (from `kslides-letsplot`).
   */
  fun content(dslBlock: SECTION.() -> Unit) {
    private_dslBlock = dslBlock
    private_dslAssigned = true
  }

  /**
   * Emit a reveal.js speaker-notes `<aside class="notes">` block. Visible only in the speaker
   * view (when the Notes plugin is enabled on the presentation).
   */
  inline fun SectioningOrFlowContent.notes(
    crossinline block: ASIDE.() -> Unit = {},
  ) = ASIDE(attributesMapOf("class", "notes"), consumer).visit(block)
}

/** A DSL slide inside a `verticalSlides{}` block. Same semantics as [HorizontalDslSlide]. */
class VerticalDslSlide(
  override val presentation: Presentation,
  content: SlideArgs,
) : VerticalSlide(presentation, content),
  DslSlide {
  override var private_section: SECTION? = null
  override var private_dslBlock: SECTION.() -> Unit = { }
  override var private_iframeCount = 1
  override var private_useHttp: Boolean = false
  override var private_dslAssigned = false

  /** See [HorizontalDslSlide.content]. */
  fun content(dslBlock: SECTION.() -> Unit) {
    private_dslBlock = dslBlock
    private_dslAssigned = true
  }

  /** See [HorizontalDslSlide.notes]. */
  inline fun SectioningOrFlowContent.notes(
    crossinline block: ASIDE.() -> Unit = {},
  ) = ASIDE(attributesMapOf("class", "notes"), consumer).visit(block)
}
