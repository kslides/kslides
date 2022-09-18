package com.kslides.config

import com.kslides.DiagramOutputType

open class DiagramConfig : AbstractConfig() {
  var outputType by ConfigProperty<DiagramOutputType>(kslidesManagedValues)
  var width by ConfigProperty<String>(kslidesManagedValues)  // img style
  var height by ConfigProperty<String>(kslidesManagedValues)  // img style
  var style by ConfigProperty<String>(kslidesManagedValues)  // img style
  var title by ConfigProperty<String>(kslidesManagedValues)  // For screen readers
  var options by ConfigProperty<Map<String, Any>>(kslidesManagedValues)  // For screen readers

  internal fun assignDefaults() {
    outputType = DiagramOutputType.SVG
    width = ""
    height = ""
    style = ""
    title = ""
    options = emptyMap()
  }
}