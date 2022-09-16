package com.kslides

import com.kslides.slide.DslSlide
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.html.*

@KSlidesDslMarker
fun DslSlide.kroki(
  type: String,
  krokiText: () -> String,
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
    runBlocking {
      val response: HttpResponse = presentation.kslides.client.post("https://kroki.io/${type.lowercase()}/svg") {
        expectSuccess = true
        contentType(ContentType.Text.Plain)
        setBody(krokiText())
        onDownload { bytesSentTotal, contentLength ->
          println("Received $bytesSentTotal bytes from $contentLength")
        }
      }
      val bytes: ByteArray = response.body()
      String(bytes) //.lines().drop(3).joinToString("\n")
    }
  }

  _section?.img {
    //style = "zoom: 2.0"
    src = krokiFilename(filename)
  } ?: error("kroki() must be called from within a content{} block")
}