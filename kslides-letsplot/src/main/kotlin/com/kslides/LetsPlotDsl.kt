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
