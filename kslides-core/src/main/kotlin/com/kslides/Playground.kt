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

  const val playgroundEndpoint = "kotlin-file"
  const val dataSelector = "kotlin-code"
  const val sourceName = "source"
  const val otherNames = "other"

  fun Routing.setupPlayground() {
    get(playgroundEndpoint) {
      respondWith {
        document {
          append.html {
            head {
              script {
                src = "https://unpkg.com/kotlin-playground@1"
                attributes["data-selector"] = ".$dataSelector"
              }
            }
            body {
              val params = call.request.queryParameters
              code(classes = dataSelector) {
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