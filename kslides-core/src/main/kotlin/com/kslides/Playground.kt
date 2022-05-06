package com.kslides

import com.github.pambrose.common.response.*
import com.kslides.config.PlaygroundConfig.Companion.playgroundAttributes
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.html.*
import kotlinx.html.dom.*
import mu.*
import kotlin.collections.set

object Playground : KLogging() {

  const val sourceName = "source"
  const val otherNames = "other"

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
              code(classes = kslides.kslidesConfig.playgroundSelector) {
                playgroundAttributes
                  .map { attrib -> attrib to (params[attrib] ?: "") }
                  .filter { it.second.isNotBlank() }
                  .forEach { attributes[it.first] = it.second }

                val path = params[sourceName] ?: throw IllegalArgumentException("Missing playground filename")
                logger.info { "Including file: $path" }
                +includeFile(path)

                // other names are comma separated
                (params[otherNames] ?: "")
                  .also { files ->
                    if (files.isNotBlank())
                      files
                        .split(",")
                        .forEach { filename ->
                          logger.info { "Including additional file: $filename" }
                          textArea(classes = "hidden-dependency") {
                            +this@code.includeFile(filename)
                          }
                        }
                  }
              }
            }
          }
        }.serialize() //.also { logger.info { "\n$it" } }
      }
    }
  }
}