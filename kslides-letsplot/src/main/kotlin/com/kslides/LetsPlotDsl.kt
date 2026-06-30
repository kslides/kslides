package com.kslides

import com.kslides.LetsPlot.letsPlotContent
import com.kslides.LetsPlot.scriptUrlForVersion
import com.kslides.config.LetsPlotIframeConfig
import com.kslides.slide.DslSlide
import kotlinx.html.SECTION
import kotlinx.html.iframe
import kotlinx.html.style
import kotlinx.html.title
import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.commons.geometry.DoubleVector

/**
 * Embed a [Lets-Plot](https://lets-plot.org/kotlin/) figure inside a [DslSlide] `content{}` block.
 * The figure is exported to self-contained HTML and served from a dedicated iframe so the
 * Lets-Plot JS runtime does not collide with reveal.js's own scripts.
 *
 * The iframe's rendered size (set in [configBlock]) must stay compatible with the plot's own
 * dimensions ([dimensions]): if `width` is `"600px"` the plot should be about that wide, otherwise
 * you will see clipping or extra whitespace. A good tuning workflow is to set
 * `style = "border: 1px solid black;"` in [configBlock] while iterating, then remove it.
 *
 * @param dimensions plot width × height passed to Lets-Plot's HTML exporter. Omit to let
 *   Lets-Plot choose.
 * @param configBlock size/style overrides for the enclosing iframe (symmetric with
 *   `playground{}`/`diagram{}`). Merged with global and presentation defaults.
 * @param block lambda returning the [Figure] to render.
 *
 * The enclosing `<section>` is supplied via the [SECTION] context parameter, so calling this
 * outside a [DslSlide] `content{}` block is a compile-time error.
 */
context(section: SECTION)
fun DslSlide.letsPlot(
  dimensions: Dimensions? = null,
  configBlock: LetsPlotIframeConfig.() -> Unit = {},
  block: () -> Figure,
) = letsPlotImpl(dimensions, LetsPlotIframeConfig().apply(configBlock), block)

/**
 * Pre-built-config overload of [letsPlot]. Prefer the `configBlock` overload, which is symmetric
 * with `playground{}`/`diagram{}`.
 */
@Deprecated("Pass a configBlock lambda instead of a prebuilt LetsPlotIframeConfig")
context(section: SECTION)
fun DslSlide.letsPlot(
  dimensions: Dimensions? = null,
  iframeConfig: LetsPlotIframeConfig,
  block: () -> Figure,
) = letsPlotImpl(dimensions, iframeConfig, block)

context(section: SECTION)
private fun DslSlide.letsPlotImpl(
  dimensions: Dimensions?,
  perCallConfig: LetsPlotIframeConfig,
  block: () -> Figure,
) {
  val filename = newFilename()
  val mergedConfig =
    LetsPlotIframeConfig()
      .also { config ->
        config.merge(globalLetsPlotConfig)
        config.merge(presentationLetsPlotConfig)
        config.merge(perCallConfig)
      }

  val plotSize = resolvePlotSize(dimensions)
  val scriptUrl = scriptUrlForVersion(letsPlotJsVersion)
  warnIfLocalScriptUrl(private_useHttp, scriptUrl, letsPlotJsVersion)

  recordIframeContent(private_useHttp, mergedConfig.staticContent, presentation.kslides, letsPlotPath, filename) {
    renderLetsPlotContent(block(), scriptUrl, plotSize, filename, letsPlotJsVersion)
  }

  section.iframe {
    src = letsPlotFilename(filename)
    mergedConfig.width.also { if (it.isNotBlank()) this.width = it }
    mergedConfig.height.also { if (it.isNotBlank()) this.height = it }
    mergedConfig.style.also { if (it.isNotBlank()) this.style = it }
    mergedConfig.title.also { if (it.isNotBlank()) this.title = it }
  }
}

/** Validate the optional [dimensions] and convert them to a Lets-Plot [DoubleVector] plot size. */
private fun resolvePlotSize(dimensions: Dimensions?): DoubleVector? =
  dimensions?.let {
    require(it.width > 0 && it.height > 0) {
      "letsPlot dimensions must be positive (got ${it.width} by ${it.height})"
    }
    DoubleVector(it.width.toDouble(), it.height.toDouble())
  }

/** Warn when a static page would bake in a dev/localhost Lets-Plot script URL that won't load elsewhere. */
private fun warnIfLocalScriptUrl(
  useHttp: Boolean,
  scriptUrl: String,
  version: String,
) {
  if (!useHttp && LetsPlot.isLocalScriptUrl(scriptUrl)) {
    LetsPlot.logger.warn {
      "letsPlotJsVersion '$version' resolved to a local script URL ($scriptUrl); the generated " +
        "static page will not load Lets-Plot off this machine. Set a published version via " +
        "kslidesConfig { letsPlotJsVersion = \"…\" }."
    }
  }
}

/** Render the figure to iframe HTML, rethrowing any failure with the filename + JS version for context. */
private fun renderLetsPlotContent(
  figure: Figure,
  scriptUrl: String,
  plotSize: DoubleVector?,
  filename: String,
  version: String,
): String =
  runCatching { letsPlotContent(figure, scriptUrl, plotSize) }
    .getOrElse { e ->
      if (e is Error) throw e // never wrap VM errors (OOM, etc.)
      throw IllegalStateException("Failed to render letsPlot iframe '$filename' with Lets-Plot JS $version", e)
    }
