package com.kslides.config

import com.kslides.CssFile
import com.kslides.JsFile
import com.kslides.StaticRoot

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

  val playgroundSelector = "playground-code"
  val playgroundUrl = "https://unpkg.com/kotlin-playground@1"

  val plotlyUrl = "https://cdn.plot.ly/plotly-1.54.6.min.js"

  val krokiUrl = "https://kroki.io"
}