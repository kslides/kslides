package com.kslides.config

import com.kslides.*
import org.slf4j.event.*

class OutputConfig(val kslides: KSlides) {
  var enableFileSystem = false
  var outputDir = "docs"
  var staticRootDir = "revealjs"
  var playgroundDir = "playground"

  var enableHttp = false
  var httpPort = 8080
  var defaultHttpRoot = "public"
  var logLevel = Level.INFO
}