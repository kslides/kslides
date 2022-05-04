package com.kslides

import com.github.pambrose.common.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.html.*
import kotlinx.html.dom.*
import mu.*
import kotlin.collections.set

object Playground : KLogging() {

  const val playgroundEndpoint = "kotlin-file"

  private val playgroundAttributes =
    listOf(
      "args",
      "data-target-platform",
      "data-highlight-only",
      "folded-button",
      "data-js-libs",
      "auto-indent",
      "theme",
      "mode",
      "data-min-compiler-version",
      "data-autocomplete",
      "highlight-on-fly",
      "indent",
      "lines",
      "from",
      "to",
      "data-output-height",
      "match-brackets",
      "data-crosslink",
      "data-shorter-height",
      "data-scrollbar-style",
    )

  fun Routing.playgroundFiles() {

    get(playgroundEndpoint) {
      respondWith {
        document {
          append.html {
            head {
              script {
                src = "https://unpkg.com/kotlin-playground@1"
                attributes["data-selector"] = "code"
              }
            }
            body {
              val params = call.request.queryParameters
              code {
                playgroundAttributes
                  .map { attrib -> attrib to (params[attrib] ?: "") }
                  .filter { it.second.isNotBlank() }
                  .forEach { attributes[it.first] = it.second }

                val path = params["source"] ?: throw IllegalArgumentException("Missing playground filename")
                +includeFile(path)

                (params["supp"] ?: "").also { suppFile ->
                  if (suppFile.isNotBlank())
                    textArea(classes = "hidden-dependency") {
                      +this@code.includeFile(suppFile)
                    }
                }
              }
            }
          }
        }.serialize().also { logger.info { "\n$it" } }
      }
    }
  }
}