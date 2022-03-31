package com.kslides

import org.slf4j.event.*

class PresentationOutput(val kslides: KSlides) {
  var enableHttp = false
  var httpPort = 8080
  var defaultHttpRoot = "public"
  var logLevel = Level.INFO

  var enableFileSystem = false
  var outputDir = "docs"
  var staticRootDir = "revealjs"
}