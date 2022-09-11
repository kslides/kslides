package com.kslides.config

class MermaidIframeConfig : AbstractConfig() {
  var width by ConfigProperty<String>(kslidesManagedValues)  // iframe width
  var height by ConfigProperty<String>(kslidesManagedValues) // iframe height
  var style by ConfigProperty<String>(kslidesManagedValues)  // iframe style
  var title by ConfigProperty<String>(kslidesManagedValues)  // For screen readers
  var staticContent by ConfigProperty<Boolean>(kslidesManagedValues) // Prevents recompute of mermaid content

  internal fun assignDefaults() {
    width = "100%"
    height = "500px"
    style = ""
    title = ""
    staticContent = true
  }

  companion object {
    inline operator fun invoke(action: MermaidIframeConfig.() -> Unit): MermaidIframeConfig = MermaidIframeConfig().apply(action)
  }
}