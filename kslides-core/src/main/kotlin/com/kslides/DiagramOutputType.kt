package com.kslides

import io.ktor.http.*

/**
 * Image format produced by a Kroki [com.kslides.diagram] call. Not every Kroki diagram type
 * supports every output format — consult the [Kroki docs](https://docs.kroki.io) for the
 * specific diagram you are rendering.
 *
 * @property suffix file extension used for the generated file (e.g. `"svg"`).
 * @property contentType MIME `Content-Type` sent when serving the diagram over HTTP.
 */
enum class DiagramOutputType(
  val suffix: String,
  val contentType: ContentType,
) {
  PNG("png", ContentType.Image.PNG),
  SVG("svg", ContentType.Image.SVG),
  JPEG("jpeg", ContentType.Image.JPEG),
  ;

  companion object {
    /**
     * Look up the [DiagramOutputType] matching [suffix] (e.g. `"png"`).
     *
     * @throws IllegalArgumentException if the suffix does not match any known type.
     */
    fun outputTypeFromSuffix(
      suffix: String,
    ) = entries.find { it.suffix == suffix } ?: throw IllegalArgumentException("Invalid Diagram suffix: $suffix")
  }
}
