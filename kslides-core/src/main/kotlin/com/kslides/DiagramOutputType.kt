package com.kslides

import io.ktor.http.*

enum class DiagramOutputType(val suffix: String, val contentType: ContentType) {
  PNG("png", ContentType.Image.PNG),
  SVG("svg", ContentType.Image.SVG),
  JPEG("jpeg", ContentType.Image.JPEG),
  ;

  companion object {
    fun outputTypeFromSuffix(suffix: String) =
      values().find { it.suffix == suffix } ?: throw IllegalArgumentException("Invalid Diagram suffix: $suffix")
  }
}