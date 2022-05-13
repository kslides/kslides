package com.kslides.config

import com.kslides.*

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

  var playgroundUrl = "https://unpkg.com/kotlin-playground@1"
  var playgroundHttpPrefix = "http://0.0.0.0"
  var playgroundEndpoint = "playground-file"
  var playgroundSelector = "playground-code"
}