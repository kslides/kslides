package com.kslides

import com.github.pambrose.common.util.nullIfBlank
import com.kslides.config.DiagramConfig
import com.kslides.slide.DslSlide
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.html.div
import kotlinx.html.img
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonObject
import mu.two.KLogging

class DiagramDescription : DiagramConfig() {
  var source = ""

  companion object : KLogging()
}

@KSlidesDslMarker
fun DslSlide.diagram(
  diagramType: String,
  diagramBlock: DiagramDescription.() -> Unit,
) {
  val diagram = DiagramDescription().apply(diagramBlock)
  val mergedConfig =
    DiagramConfig()
      .also { config ->
        config.merge(globalDiagramConfig)
        config.merge(presentationDiagramConfig)
        config.merge(diagram)
      }

  val filename = newFilename(mergedConfig.outputType.suffix)

  recordKrokiContent(_useHttp, presentation.kslides, mergedConfig.outputType, krokiPath, filename) {
    val configMap: MutableMap<String, Any> =
      mutableMapOf(
        "diagram_source" to diagram.source,
        "diagram_type" to diagramType.lowercase(),
        "output_format" to mergedConfig.outputType.suffix,
      )

    mergedConfig.options.also { options ->
      if (options.isNotEmpty())
        configMap["diagram_options"] = options
    }

    fetchKrokiContent(filename, configMap)
  }

  _section?.div(classes.nullIfBlank()) {
    img {
      src = krokiFilename(filename)
      mergedConfig.width.also { if (it.isNotBlank()) width = it }
      mergedConfig.height.also { if (it.isNotBlank()) height = it }
      mergedConfig.style.also { if (it.isNotBlank()) style = it }
      mergedConfig.title.also { if (it.isNotBlank()) title = it }
    }
  } ?: error("diagram() must be called from within a content{} block")
}

private fun DslSlide.fetchKrokiContent(filename: String, desc: Map<String, Any>): ByteArray =
  runBlocking {
    DiagramDescription.logger.info { "Fetching kroki content for $filename" }
    val json =
      buildJsonObject {
        desc.forEach { (k, v) ->
          when (v) {
            is String -> put(k, JsonPrimitive(v))
            is Map<*, *> -> {
              putJsonObject(k) {
                v.forEach { k, v ->
                  when {
                    k !is String -> error("Invalid key type: $k")
                    v is Boolean -> put(k, JsonPrimitive(v))
                    v is String -> put(k, JsonPrimitive(v))
                    v is Number -> put(k, JsonPrimitive(v))
                    else -> error("Invalid value type: $v")
                  }
                }
              }
            }

            else -> error("Unexpected value type: ${v::class}")
          }
        }
      }.toString()

    val kslidesConfig = presentation.kslides.kslidesConfig
    val response =
      presentation.kslides.client.post(kslidesConfig.krokiUrl) {
        expectSuccess = true
        timeout { requestTimeoutMillis = kslidesConfig.clientHttpTimeout.inWholeMilliseconds }
        contentType(ContentType.Application.Json)
        setBody(json)
        onDownload { bytesSentTotal, contentLength ->
          DiagramDescription.logger.info { "Received $bytesSentTotal bytes of $contentLength for $filename" }
        }
      }
    response.body()
  }