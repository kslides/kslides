package com.kslides.config

class KSlidesConfig {
  val staticRoots = mutableListOf("assets", "css", "dist", "js", "plugin", "revealjs")

  var playgroundUrl = "https://unpkg.com/kotlin-playground@1"
  var playgroundHttpPrefix = "http://0.0.0.0"
  var playgroundEndpoint = "playground-file"
  var playgroundSelector = "playground-code"
}