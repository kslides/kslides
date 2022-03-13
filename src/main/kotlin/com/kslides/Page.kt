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

      if (p.config.copyCode)
        p.cssFiles += "plugin/copycode/copycode.css" to ""

      p.cssFiles.forEach {
        link {
          rel = "stylesheet";
          href = if (it.first.startsWith("http")) it.first else "$srcPrefix${it.first}"
          if (it.second.isNotEmpty())
            id = it.second
        }
      }

      if (p.css.isNotEmpty()) {
        style {
          type = "text/css"; media = "screen"
          rawHtml(p.css)
        }
      }
    }
  }

  fun HTML.generateBody(p: Presentation, srcPrefix: String) {
    body {
      div("reveal") {
        div("slides") {
          p.slides
            .forEach { slide ->
              slide(this)
            }
        }
      }

      rawHtml("\n")

      if (p.config.copyCode) {
        p.jsFiles += "plugin/copycode/copycode.js"
        // Required for copycode.js
        p.jsFiles += "https://cdnjs.cloudflare.com/ajax/libs/clipboard.js/2.0.6/clipboard.min.js"
      }

      p.jsFiles.forEach {
        script { src = if (it.startsWith("http")) it else "$srcPrefix$it" }
        rawHtml("\n")
      }

      rawHtml("\n")

      script {
        rawHtml(p.config.toJS())
      }
    }
  }

  fun HTMLTag.rawHtml(html: String) = unsafe { raw(html) }
}