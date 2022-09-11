package com.kslides

import kotlinx.html.*
import kotlinx.html.dom.*
import mu.KLogging

object Mermaid : KLogging() {
  internal fun mermaidContent(
    kslides: KSlides,
    mermaidText: String,
  ) =
    document {
      val kslidesConfig = kslides.kslidesConfig
      append.html {
        head {
          script { type = "text/javascript"; src = "https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js" }
        }
        body {
          div("mermaid") {
            style = "text-align: center;"
            // Remove the leading and trailing newlines, which the mermaid parser doesn't like
            +mermaidText.lines()
              .filter { it.isNotBlank() }
              .map { it.trimStart() }
              .joinToString("\n")
          }
          script { rawHtml("\n\t\tmermaid.initialize({startOnLoad:true});\n\t\t") }
        }
      }
    }.serialize()
}