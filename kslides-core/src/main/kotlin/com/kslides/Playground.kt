package com.kslides

import com.github.pambrose.common.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.html.*
import kotlinx.html.dom.*
import kotlin.collections.set

object Playground {

  fun Routing.playgroundFiles() {

    get("kotlin-code") {
      fun String.toFileName() =
        replace("---___---", "/")

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
                  .map { attrib -> attrib to (params[attrib] ?: "") }
                  .filter { it.second.isNotBlank() }
                  .forEach { attributes[it.first] = it.second }

                val path = params["source"] ?: throw IllegalArgumentException("Missing playground filename")
                +includeFile("kslides-examples/src/main/kotlin/${path.toFileName()}")

                (params["supp"] ?: "")
                  .also { suppCode ->
                    if (suppCode.isNotBlank())
                      textArea(classes = "hidden-dependency") {
                        +this@code.includeFile("kslides-examples/src/main/kotlin/${suppCode.toFileName()}")
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