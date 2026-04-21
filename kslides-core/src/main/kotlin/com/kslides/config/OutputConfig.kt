package com.kslides.config

import com.kslides.KSlides
import com.kslides.KSlidesDslMarker
import com.pambrose.common.util.toPath
import org.slf4j.event.Level

@KSlidesDslMarker
class OutputConfig(
  val kslides: KSlides,
) {
  var enableFileSystem = true
  var outputDir = "docs"              // write slide content to html files in the /docs directory
  var staticRootDir = "revealjs"      // directory containing revealjs content
  var playgroundDir = "playground"    // directory where playground content is written
  var letsPlotDir = "letsPlot"        // directory where Lets-Plot content is written
  var krokiDir = "kroki"              // directory where kroki content is written

  var enableHttp = true
  var httpPort = 8080
  var defaultHttpRoot = "public"
  var callLoggingLogLevel = Level.INFO

  internal val port: Int get() = System.getenv("PORT")?.toInt() ?: httpPort

  internal val playgroundPath: String get() = listOf(outputDir, playgroundDir).toPath(false)
  internal val letsPlotPath: String get() = listOf(outputDir, letsPlotDir).toPath(false)
  internal val krokiPath: String get() = listOf(outputDir, krokiDir).toPath(false)
}
