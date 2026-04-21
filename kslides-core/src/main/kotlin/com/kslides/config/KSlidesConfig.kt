package com.kslides.config

import com.kslides.CssFile
import com.kslides.JsFile
import com.kslides.KSlidesDslMarker
import com.kslides.StaticRoot
import kotlin.time.Duration.Companion.seconds

@KSlidesDslMarker
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

  // Lets-Plot JS runtime version — must be compatible with the lets-plot-kotlin-jvm
  // artifact on the classpath. Bumping this here avoids a library release.
  // lets-plot-kotlin-jvm:4.13.0 → lets-plot core 4.9.0.
  var letsPlotJsVersion = "4.9.0"

  var krokiUrl = "https://kroki.io"

  var clientHttpTimeout = 30.seconds
}