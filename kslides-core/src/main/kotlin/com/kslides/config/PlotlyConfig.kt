package com.kslides.config

class PlotlyConfig : AbstractConfig() {

  var width by ConfigProperty<String>(kslidesManagedValues)
  var height by ConfigProperty<String>(kslidesManagedValues)
  var style by ConfigProperty<String>(kslidesManagedValues)
  var title by ConfigProperty<String>(kslidesManagedValues) // For screen readers

  fun assignDefaults() {
    width = ""
    height = ""
    style = ""
    title = ""
  }

}