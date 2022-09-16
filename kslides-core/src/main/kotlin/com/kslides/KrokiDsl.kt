package com.kslides

import com.kslides.slide.DslSlide
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.html.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KLogging

object KrokiDsl : KLogging()

@KSlidesDslMarker
fun DslSlide.kroki(
  type: String,
  krokiBlock: () -> String,
) {
  val filename = newFilename("svg")

//  val mergedConfig =
//    MermaidIframeConfig()
//      .also { config ->
//        config.merge(globalMermaidConfig)
//        config.merge(presentationMermaidConfig)
//        config.merge(iframeConfig)
//      }
//
//  recordContent(presentation.kslides, mergedConfig.staticContent, filename, mermaidPath, _useHttp) {
//    mermaidContent(presentation.kslides, mermaidText())
//  }
//
//  _section?.iframe {
//    src = mermaidFilename(filename)
//    mergedConfig.width.also { if (it.isNotBlank()) width = it }
//    mergedConfig.height.also { if (it.isNotBlank()) height = it }
//    mergedConfig.style.also { if (it.isNotBlank()) style = it }
//    mergedConfig.title.also { if (it.isNotBlank()) title = it }
//  } ?: error("mermaid() must be called from within a content{} block")

  recordKrokiContent(_useHttp, presentation.kslides, krokiPath, filename) {
    fetchKrokiContent(
      mapOf(
        "diagram_source" to krokiBlock(),
        "diagram_type" to type.lowercase(),
        "output_format" to "svg",
      )
    )
  }

  _section?.img {
    //style = "zoom: 2.0"
    src = krokiFilename(filename)
  } ?: error("kroki() must be called from within a content{} block")
}

private fun DslSlide.fetchKrokiContent(desc: Map<String, String>) =
  runBlocking {
    val response = presentation.kslides.client.post(presentation.kslides.kslidesConfig.krokiUrl) {
      expectSuccess = true
      contentType(ContentType.Application.Json)
      setBody(Json.encodeToString(desc))
      onDownload { bytesSentTotal, contentLength ->
        KrokiDsl.logger.info { "Received $bytesSentTotal bytes of $contentLength" }
      }
    }
    val bytes: ByteArray = response.body()
    String(bytes)
  }