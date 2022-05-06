package com.kslides.config

import com.kslides.*
import org.slf4j.event.*

class OutputConfig(val kslides: KSlides) {
  var enableFileSystem = true
  var outputDir = "docs"
  var staticRootDir = "revealjs"
  var playgroundDir = "playground"

  var enableHttp = true
  var httpPort = 8080
  var defaultHttpRoot = "public"
  var logLevel = Level.INFO

  internal val port: Int get() = System.getenv("PORT")?.toInt() ?: httpPort
}