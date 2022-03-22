package com.kslides

import com.github.pambrose.common.util.*
import com.kslides.Presentation.Companion.globalDefaults
import kotlinx.html.*
import kotlinx.html.dom.*

internal object Page {

  fun generatePage(p: Presentation, srcPrefix: String = "/"): String {
    val document =
      document {
        PresentationConfig()
          .apply {
            merge(globalDefaults)
            merge(p.presentationDefaults)
          }.also { config ->
            append.html {
              generateHead(p, config, srcPrefix.ensureSuffix("/"))
              generateBody(p, config, srcPrefix.ensureSuffix("/"))
            }
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

  fun HTML.generateHead(p: Presentation, config: PresentationConfig, srcPrefix: String) {
    head {
      meta { charset = "utf-8" }
      meta { name = "apple-mobile-web-app-capable"; content = "yes" }
      meta { name = "apple-mobile-web-app-status-bar-style"; content = "black-translucent" }
      meta {
        name = "viewport"
        content = "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"
      }

      if (config.title.isNotEmpty())
        title { +config.title }

      if (config.gaPropertyId.isNotEmpty()) {
        script { async = true; src = "https://www.googletagmanager.com/gtag/js?id=G-Z6YBNZS12K" }
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

      // Css Files
      p.cssFiles += CssFile("dist/theme/${config.theme.name.toLower()}.css", "theme")
      p.cssFiles += CssFile("plugin/highlight/${config.highlight.name.toLower()}.css", "highlight-theme")

      if (config.enableCodeCopy)
        p.cssFiles += CssFile("plugin/copycode/copycode.css")

      p.cssFiles.forEach {
        link {
          rel = "stylesheet"
          href = if (it.filename.startsWith("http")) it.filename else "$srcPrefix${it.filename}"
          if (it.id.isNotEmpty())
            id = it.id
        }
      }

      if (p.css.isNotEmpty()) {
        style {
          type = "text/css"
          media = "screen"
          rawHtml(p.css.prependIndent("\t\t"))
        }
      }
    }
  }

  fun HTML.generateBody(p: Presentation, config: PresentationConfig, srcPrefix: String) {
    body {
      div("reveal") {
//        a {
//          id = "github-corner"
//          href = "https://github.com/Martinomagnifico/reveal.js-appearance"
//          target = "blank"
//          title = "View source on Github"
//          svg {
//            attributes["viewBox"] = "0 0 55 55"
//          }
//          rawHtml(
//            """
//                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 55 55">
//            <path fill="currentColor"
//                  d="M27.5 11.2a16.3 16.3 0 0 0-5.1 31.7c.8.2 1.1-.3 1.1-.7v-2.8c-4.5 1-5.5-2.2-5.5-2.2-.7-1.9-1.8-2.4-1.8-2.4-1.5-1 .1-1 .1-1 1.6.1 2.5 1.7 2.5 1.7 1.5 2.5 3.8 1.8 4.7 1.4.2-1 .6-1.8 1-2.2-3.5-.4-7.3-1.8-7.3-8 0-1.8.6-3.3 1.6-4.4-.1-.5-.7-2.1.2-4.4 0 0 1.4-.4 4.5 1.7a15.6 15.6 0 0 1 8.1 0c3.1-2 4.5-1.7 4.5-1.7.9 2.3.3 4 .2 4.4 1 1 1.6 2.6 1.6 4.3 0 6.3-3.8 7.7-7.4 8 .6.6 1.1 1.6 1.1 3v4.6c0 .4.3.9 1.1.7a16.3 16.3 0 0 0-5.2-31.7"></path>
//        </svg>
//          """.trimIndent()
//          )
//        }

        div("slides") {
          p.slides.forEach { slide -> slide.content(this, slide) }
        }
      }

      if (config.enableCodeCopy) {
        p.jsFiles += JsFile("plugin/copycode/copycode.js")
        // Required for copycode.js
        p.jsFiles += JsFile("https://cdnjs.cloudflare.com/ajax/libs/clipboard.js/2.0.6/clipboard.min.js")
      }

      if (config.enableMenu) {
        p.jsFiles += JsFile("plugin/menu/menu.js")
      }

//      if (config.toolbar) {
//        p.jsFiles += "plugin/toolbar/toolbar.js"
//      }

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

  fun HTMLTag.rawHtml(html: String) = unsafe { raw(html) }
}