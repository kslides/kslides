package com.kslides

import com.github.pambrose.common.util.*
import kotlinx.html.*
import kotlinx.html.dom.*

internal object Page {

  fun generatePage(p: Presentation, srcPrefix: String = "/"): String {
    val document =
      document {
        append.html {
          generateHead(p, srcPrefix.ensureSuffix("/"))
          generateBody(p, srcPrefix.ensureSuffix("/"))
        }
      }

    // Protect characters inside markdown blocks that get escaped by HTMLStreamBuilder
    val nodeList = document.getElementsByTagName("*")
    (0..nodeList.length).forEach { i ->
      val node = nodeList.item(i)
      if (node.isNotNull()) {
        if (node.nodeName == "section") {
          node.attributes.getNamedItem("data-separator")?.apply {
            nodeValue = nodeValue.replace("\n", "\\n")
            nodeValue = nodeValue.replace("\r", "\\r")
          }

          node.attributes.getNamedItem("data-separator-vertical")?.apply {
            nodeValue = nodeValue.replace("\n", "\\n")
            nodeValue = nodeValue.replace("\r", "\\r")
          }
        }
      }
    }

    return document.serialize()
  }

  fun HTML.generateHead(p: Presentation, srcPrefix: String) {
    head {
      meta { charset = "utf-8" }
      meta { name = "apple-mobile-web-app-capable"; content = "yes" }
      meta { name = "apple-mobile-web-app-status-bar-style"; content = "black-translucent" }
      meta {
        name = "viewport"
        content = "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"
      }

      if (p.title.isNotEmpty())
        title { +p.title }

      if (p.baseConfig.copyCode)
        p.cssFiles += "plugin/copycode/copycode.css" to ""

      p.cssFiles.forEach {
        link {
          rel = "stylesheet"
          href = if (it.first.startsWith("http")) it.first else "$srcPrefix${it.first}"
          if (it.second.isNotEmpty())
            id = it.second
        }
      }

      if (p.baseConfig.enablePlayground)
        p.css += """
          .code-output {
          text-align: left;  
          }
        """.trimIndent()

      if (p.css.isNotEmpty()) {
        style {
          type = "text/css"
          media = "screen"
          rawHtml(p.css.prependIndent("\t\t"))
        }
      }
    }
  }

  fun HTML.generateBody(p: Presentation, srcPrefix: String) {
    body {
      div("reveal") {
        a {
          id = "github-corner"
          href = "https://github.com/Martinomagnifico/reveal.js-appearance"
          target = "blank"
          title = "View source on Github"

          rawHtml(
            """
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 55 55">
            <path fill="currentColor"
                  d="M27.5 11.2a16.3 16.3 0 0 0-5.1 31.7c.8.2 1.1-.3 1.1-.7v-2.8c-4.5 1-5.5-2.2-5.5-2.2-.7-1.9-1.8-2.4-1.8-2.4-1.5-1 .1-1 .1-1 1.6.1 2.5 1.7 2.5 1.7 1.5 2.5 3.8 1.8 4.7 1.4.2-1 .6-1.8 1-2.2-3.5-.4-7.3-1.8-7.3-8 0-1.8.6-3.3 1.6-4.4-.1-.5-.7-2.1.2-4.4 0 0 1.4-.4 4.5 1.7a15.6 15.6 0 0 1 8.1 0c3.1-2 4.5-1.7 4.5-1.7.9 2.3.3 4 .2 4.4 1 1 1.6 2.6 1.6 4.3 0 6.3-3.8 7.7-7.4 8 .6.6 1.1 1.6 1.1 3v4.6c0 .4.3.9 1.1.7a16.3 16.3 0 0 0-5.2-31.7"></path>
        </svg>

          """.trimIndent()
          )
        }

        div("slides") {
          p.slides
            .forEach { slide ->
              slide(this)
            }
        }
      }

      if (p.baseConfig.copyCode) {
        p.jsFiles += "plugin/copycode/copycode.js"
        // Required for copycode.js
        p.jsFiles += "https://cdnjs.cloudflare.com/ajax/libs/clipboard.js/2.0.6/clipboard.min.js"
      }

      if (p.baseConfig.enableMenu) {
        p.jsFiles += "plugin/menu/menu.js"
      }

//      if (p.config.toolbar) {
//        p.jsFiles += "plugin/toolbar/toolbar.js"
//      }

      script { src = "https://unpkg.com/kotlin-playground@1"; attributes["data-selector"] = ".kotlin-code" }

      rawHtml("\n\t\n")
      p.jsFiles.forEach {
        rawHtml("\t")
        script { src = if (it.startsWith("http")) it else "$srcPrefix$it" }
        rawHtml("\n")
      }

      rawHtml("\n\t")
      script {
        rawHtml("\n\t\tReveal.initialize({\n${p.toJs(srcPrefix)}\t\t});\n\n")
      }
    }
  }

  fun HTMLTag.rawHtml(html: String) = unsafe { raw(html) }
}