package com.kslides

import com.github.pambrose.common.util.*
import kotlinx.html.*
import kotlinx.html.dom.*

internal object Page {

  fun generatePage(p: Presentation, config: PresentationConfig, prefix: String = "/"): String {
    //p.slides.removeAll { true }
    val document =
      document {
        append.html {
          generateHead(p, config, prefix.ensureSuffix("/"))
          generateBody(p, config, prefix.ensureSuffix("/"))
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

  fun HTML.generateHead(p: Presentation, config: PresentationConfig, srcPrefix: String) =
    head {
      meta { charset = "utf-8" }
      meta { name = "apple-mobile-web-app-capable"; content = "yes" }
      meta { name = "apple-mobile-web-app-status-bar-style"; content = "black-translucent" }
      meta {
        name = "viewport"
        content = "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"
      }

      if (config.title.isNotBlank())
        title { +config.title }

      if (config.gaPropertyId.isNotBlank()) {
        script { async = true; src = "https://www.googletagmanager.com/gtag/js?id=G-Z6YBNZS12K" }
        rawHtml("\n\t")
        script {
          rawHtml(
            """
              window.dataLayer = window.dataLayer || [];
              function gtag(){dataLayer.push(arguments);}
              gtag('js', new Date()); 
              gtag('config', '${config.gaPropertyId}');
            """
          )
        }
      }

      p.cssFiles.forEach {
        link(rel = "stylesheet") {
          href = if (it.filename.startsWith("http")) it.filename else "$srcPrefix${it.filename}"
          if (it.id.isNotBlank())
            id = it.id
        }
      }

      if (p.css.isNotBlank()) {
        style("text/css") {
          media = "screen"
          rawHtml(p.css.prependIndent("\t\t"))
        }
      }
    }

  fun HTML.generateBody(p: Presentation, config: PresentationConfig, srcPrefix: String) =
    body {
      div("reveal") {
        if (config.githubCornerHref.isNotBlank()) {
          a(config.githubCornerHref, "blank", classes = "github-corner") {
            title = config.githubCornerTitle
            rawHtml(config.githubCornerSvg)
          }
        }

        div("slides") {
          p.slides.forEach { slide -> slide.content(this, slide) }
        }
      }

      rawHtml("\n\t\n")
      p.jsFiles.forEach { jsFile ->
        rawHtml("\t")
        script { src = if (jsFile.filename.startsWith("http")) jsFile.filename else "$srcPrefix${jsFile.filename}" }
        rawHtml("\n")
      }

      rawHtml("\n\t")
      script {
        rawHtml("\n\t\tReveal.initialize({\n${p.toJs(config, srcPrefix)}\t\t});\n\n")
      }
    }
}