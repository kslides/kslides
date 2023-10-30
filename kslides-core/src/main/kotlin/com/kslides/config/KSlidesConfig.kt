package com.kslides.config

import com.kslides.CssFile
import com.kslides.JsFile
import com.kslides.StaticRoot
import kotlin.time.Duration.Companion.seconds

class KSlidesConfig {
  val httpStaticRoots =
    mutableListOf(
      StaticRoot("assets"),
      StaticRoot("css"),
      StaticRoot("dist"),
      StaticRoot("js"),
      StaticRoot("plugin"),
    )
  val cssFiles =
    mutableListOf(
      CssFile("dist/reveal.css"),
      CssFile("dist/reset.css"),
    )
  val jsFiles =
    mutableListOf(
      JsFile("dist/reveal.js"),
    )

  var playgroundSelector = "playground-code"
  var playgroundUrl = "https://unpkg.com/kotlin-playground@1"

  //var plotlyUrl = "https://cdn.plot.ly/plotly-1.54.6.min.js"

  var krokiUrl = "https://kroki.io"

  var clientHttpTimeout = 30.seconds
}