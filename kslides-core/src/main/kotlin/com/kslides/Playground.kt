package com.kslides

import com.github.pambrose.common.response.*
import com.kslides.config.*
import com.kslides.config.PlaygroundConfig.Companion.playgroundAttributes
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.html.*
import kotlinx.html.dom.*
import mu.*
import java.io.*
import kotlin.collections.set

object Playground : KLogging() {

  const val sourceName = "source"
  const val otherNames = "other"

  internal fun writePlaygroundFiles(config: OutputConfig, urls: List<Pair<DslSlide, String>>) {
    // Create directory if missing
    File(config.playgroundPath).mkdir()

    urls.forEach { (slide, url) ->
      val prefix = config.kslides.kslidesConfig.playgroundHttpPrefix
      val port = config.port
      val fullname = "${config.playgroundPath}${slide._slideFilename}"
      val content = include("$prefix:$port/$url", indentToken = "", escapeHtml = false)
      logger.info { "Writing playground content to: $fullname" }
      File(fullname).writeText(content)
    }
  }

  fun Routing.setupPlayground(kslides: KSlides) {
    get(kslides.kslidesConfig.playgroundEndpoint) {
      respondWith {
        document {
          append.html {
            head {
              script {
                src = kslides.kslidesConfig.playgroundUrl
                attributes["data-selector"] = ".${kslides.kslidesConfig.playgroundSelector}"
              }
            }
            body {
              val params = call.request.queryParameters
              code(kslides.kslidesConfig.playgroundSelector) {
                playgroundAttributes
                  .map { attrib -> attrib to (params[attrib] ?: "") }
                  .filter { it.second.isNotBlank() }
                  .forEach { attributes[it.first] = it.second }

                val path = params[sourceName] ?: throw IllegalArgumentException("Missing playground filename")
                logger.info { "Including file: $path" }
                +include(path)

                // other names are comma separated
                (params[otherNames] ?: "")
                  .also { files ->
                    files
                      .split(",")
                      .forEach { filename ->
                        logger.info { "Including additional file: $filename" }
                        textArea(classes = "hidden-dependency") {
                          +this@code.include(filename)
                        }
                      }
                  }
              }
            }
          }
        }.serialize()
      }
    }
  }
}