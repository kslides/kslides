package com.kslides

import com.github.pambrose.common.util.*
import com.kslides.config.*
import kotlinx.html.*
import kotlinx.html.dom.*
import java.io.*

internal object Page {

  val preRegex = Regex("\\s*<pre.*>\\s*")
  val codeRegex = Regex("\\s*<code.*>\\s*")

  fun generatePage(p: Presentation, useHttp: Boolean = true, prefix: String = "/"): String {
    val htmldoc =
      document {
        val config = p.finalConfig
        append.html {
          generateHead(p, config, prefix.ensureSuffix("/"))
          generateBody(p, config, prefix.ensureSuffix("/"), useHttp)
        }
      }

    // Protect characters inside markdown blocks that get escaped by HTMLStreamBuilder
    val nodeList = htmldoc.getElementsByTagName("*")
    (0..nodeList.length)
      .forEach { i ->
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

    /*
    This is a hack to fix a copycode issue: the <pre> and <code> tags must be on the same line
    <pre>
       <code>
       </code>
    </pre>

    has to be transformed into:
    <pre><code>
       </code>
    </pre>
     */
    return buildString {
      var preFound = false
      htmldoc
        .serialize()
        .lines()
        .forEach {
          when {
            it.matches(preRegex) -> {
              preFound = true
              append(it)
            }
            preFound && it.matches(codeRegex) -> {
              preFound = false
              append("${it.trimStart()}\n")
            }
            else -> {
              append("$it\n")
            }
          }
        }
    }
  }

  private fun HTML.generateHead(p: Presentation, config: PresentationConfig, srcPrefix: String) =
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

      rawHtml("\n")
      p.cssFiles.forEach {
        link(rel = "stylesheet") {
          href = if (it.filename.startsWith("http")) it.filename else "$srcPrefix${it.filename}"
          if (it.id.isNotBlank())
            id = it.id
        }
      }

      rawHtml("\n")
      style("text/css") {
        media = "screen"
        rawHtml(
          Page::class.java.classLoader.getResource("slides.css")
            ?.readText()
            ?.lines()
            ?.joinToString("\n") { "\t\t$it" }
            ?: throw FileNotFoundException("File not found: src/main/resources/slides.css"))
      }

      if (p.css.isNotBlank()) {
        rawHtml("\n")
        style("text/css") {
          media = "screen"
          rawHtml("\n\t\t")
          rawHtml(p.css.prependIndent("\t\t"))
        }
      }
    }

  private fun HTML.generateBody(p: Presentation, config: PresentationConfig, srcPrefix: String, useHttp: Boolean) =
    body {
      div("reveal") {
        if (config.topLeftHref.isNotBlank()) {
          a(href = config.topLeftHref, target = config.topLeftTarget.htmlVal, classes = "top-left") {
            if (config.topLeftTitle.isNotBlank())
              title = config.topLeftTitle
            if (config.topLeftSvg.isNotBlank())
              rawHtml(config.topLeftSvg)
            if (config.topLeftText.isNotBlank())
              +config.topLeftText
          }
        }

        if (config.topRightHref.isNotBlank()) {
          rawHtml("\n\t\t\t")
          a(href = config.topRightHref, target = config.topRightTarget.htmlVal, classes = "top-right") {
            if (config.topRightTitle.isNotBlank())
              title = config.topRightTitle
            if (config.topRightSvg.isNotBlank())
              rawHtml(config.topRightSvg)
            if (config.topRightText.isNotBlank())
              +config.topRightText
          }
        }

        rawHtml("\n")
        div("slides") {
          p.slides.forEach { slide -> slide.content(this, slide, useHttp) }
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