package com.kslides

import com.kslides.Plotly.plotlyContent
import com.kslides.config.PlotlyIframeConfig
import com.kslides.slide.DslSlide
import kotlinx.html.*
import space.kscience.plotly.Plot
import space.kscience.plotly.PlotlyConfig
import space.kscience.plotly.layout
import space.kscience.plotly.plot

@KSlidesDslMarker
fun DslSlide.plotly(
  dimensions: Dimensions? = null,
  iframeConfig: PlotlyIframeConfig = PlotlyIframeConfig(),
  plotlyConfig: PlotlyConfig = PlotlyConfig(),
  block: Plot.() -> Unit,
) {
  val iframeId = _iframeCount++
  val kslides = presentation.kslides

  val mergedConfig =
    PlotlyIframeConfig()
      .also { config ->
        config.merge(globalPlotlyConfig)
        config.merge(presentationPlotlyConfig)
        config.merge(iframeConfig)
      }

  recordContent(kslides, mergedConfig.staticContent, filename(iframeId), plotlyPath) {
    plotlyContent(kslides) {
      plot(config = plotlyConfig) {
        block()
        // Override the layout dimensions with those supplied in the args
        layout {
          dimensions?.also { d ->
            d.width.let { this@layout.width = d.width }
            d.height.let { this@layout.height = d.height }
          }
        }
      }
    }
  }

  _section?.iframe {
    src = plotlyFilename(iframeId)
    mergedConfig.width.also { if (it.isNotBlank()) this.width = it }
    mergedConfig.height.also { if (it.isNotBlank()) this.height = it }
    mergedConfig.style.also { if (it.isNotBlank()) this.style = it }
    mergedConfig.title.also { if (it.isNotBlank()) this.title = it }
  } ?: error("plotly{} must be called from within a content{} block")
}