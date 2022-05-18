package com.kslides.config

import com.github.pambrose.common.util.*
import com.kslides.*
import org.slf4j.event.*

class OutputConfig(val kslides: KSlides) {
  var enableFileSystem = true
  var outputDir = "docs"              // write slide content to html files in the /docs directory
  var staticRootDir = "revealjs"      // directory containing revealjs content
  var playgroundDir = "playground"    // directory where playground content is written
  var plotlyDir = "plotly"            // directory where plotly content is written

  var enableHttp = true
  var httpPort = 8080
  var defaultHttpRoot = "public"
  var callLoggingLogLevel = Level.INFO

  internal val port: Int get() = System.getenv("PORT")?.toInt() ?: httpPort
  internal val playgroundPath: String get() = listOf(outputDir, playgroundDir).toPath(false)
  internal val plotlyPath: String get() = listOf(outputDir, plotlyDir).toPath(false)
}