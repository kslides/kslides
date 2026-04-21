package com.kslides.config

import com.kslides.KSlidesDslMarker

@KSlidesDslMarker
class LetsPlotIframeConfig : AbstractConfig() {
  var width by ConfigProperty<String>(kslidesManagedValues)            // iframe width
  var height by ConfigProperty<String>(kslidesManagedValues)           // iframe height
  var style by ConfigProperty<String>(kslidesManagedValues)            // iframe style
  var title by ConfigProperty<String>(kslidesManagedValues)            // For screen readers
  var staticContent by ConfigProperty<Boolean>(kslidesManagedValues)   // Prevents letsPlot{} recompute

  internal fun assignDefaults() {
    width = "100%"
    height = ""
    style = ""
    title = ""
    staticContent = false
  }

  companion object {
    inline operator fun invoke(
      action: LetsPlotIframeConfig.() -> Unit,
    ): LetsPlotIframeConfig = LetsPlotIframeConfig().apply(action)
  }
}
