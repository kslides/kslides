package com.kslides.config

class PlotlyConfig : AbstractConfig() {

  var width by ConfigProperty<String>(kslidesManagedValues)
  var height by ConfigProperty<String>(kslidesManagedValues)
  var style by ConfigProperty<String>(kslidesManagedValues)
  var title by ConfigProperty<String>(kslidesManagedValues) // For screen readers
  var staticContent by ConfigProperty<Boolean>(kslidesManagedValues) // Prevents plotly{} recompute

  fun assignDefaults() {
    width = ""
    height = ""
    style = ""
    title = ""
    staticContent = false
  }
}