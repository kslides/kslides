package com.kslides

import com.kslides.LetsPlot.letsPlotContent
import com.kslides.LetsPlot.scriptUrlForVersion
import com.kslides.config.LetsPlotIframeConfig
import com.kslides.slide.DslSlide
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
 * The iframe's rendered size (controlled by [iframeConfig]) must stay compatible with the plot's
 * own dimensions (controlled by [dimensions]): if `iframeConfig.width` is `"600px"` the plot
 * should be about that wide, otherwise you will see clipping or extra whitespace. A good tuning
 * workflow is to set `iframeConfig.style = "border: 1px solid black;"` while iterating and
 * remove it once the sizes line up.
 *
 * @param dimensions plot width × height passed to Lets-Plot's HTML exporter. Omit to let
 *   Lets-Plot choose.
 * @param iframeConfig size/style overrides for the enclosing iframe. Merged with global and
 *   presentation defaults.
 * @param block lambda returning the [Figure] to render.
 * @throws IllegalStateException if called outside a [DslSlide] `content{}` block.
 */
fun DslSlide.letsPlot(
  dimensions: Dimensions? = null,
  iframeConfig: LetsPlotIframeConfig = LetsPlotIframeConfig(),
  block: () -> Figure,
) {
  val filename = newFilename()
  val mergedConfig =
    LetsPlotIframeConfig()
      .also { config ->
        config.merge(globalLetsPlotConfig)
        config.merge(presentationLetsPlotConfig)
        config.merge(iframeConfig)
      }

  val plotSize = dimensions?.let { DoubleVector(it.width.toDouble(), it.height.toDouble()) }
  val scriptUrl = scriptUrlForVersion(letsPlotJsVersion)

  recordIframeContent(private_useHttp, mergedConfig.staticContent, presentation.kslides, letsPlotPath, filename) {
    letsPlotContent(block(), scriptUrl, plotSize)
  }

  private_section?.iframe {
    src = letsPlotFilename(filename)
    mergedConfig.width.also { if (it.isNotBlank()) this.width = it }
    mergedConfig.height.also { if (it.isNotBlank()) this.height = it }
    mergedConfig.style.also { if (it.isNotBlank()) this.style = it }
    mergedConfig.title.also { if (it.isNotBlank()) this.title = it }
  } ?: error("letsPlot() must be called from within a content{} block")
}
