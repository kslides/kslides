package com.kslides

import com.github.pambrose.common.util.*
import kotlinx.html.*
import kotlinx.html.dom.*

internal object Page {

    fun generatePage(presentation: Presentation, srcPrefix: String): String {
        val document =
            document {
                append.html {
                    generateHead(presentation, srcPrefix)
                    generateBody(presentation, srcPrefix)
                }
            }

//        val escapedChars =
//            listOf(
//                "\"" to "_*_quote_*_",
//                "<" to "_*_lt_*_",
//                ">" to "_*_gt_*_",
//                "&" to "_*_amp_*_"
//            )

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

//                if (node.nodeName == "script") {
//                    if (node.attributes.getNamedItem("type")?.nodeValue == "text/template") {
//                        if (node.firstChild.isNotNull())
//                            escapedChars.forEach {
//                                node.firstChild.nodeValue = node.firstChild.nodeValue.replace(it.first, it.second)
//                            }
//                    }
//                }
            }
        }

        val str = document.serialize()
//        escapedChars.forEach {
//            str = str.replace(it.second, it.first)
//        }
        return str
    }

    fun HTML.generateHead(presentation: Presentation, srcPrefix: String) {
        head {
            meta { charset = "utf-8" }
            meta { name = "apple-mobile-web-app-capable"; content = "yes" }
            meta { name = "apple-mobile-web-app-status-bar-style"; content = "black-translucent" }
            meta {
                name = "viewport"
                content = "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"
            }

            if (presentation.title.isNotEmpty())
                title { +presentation.title }

            link { rel = "stylesheet"; href = "${srcPrefix}dist/reset.css" }
            link { rel = "stylesheet"; href = "${srcPrefix}dist/reveal.css" }
            link { rel = "stylesheet"; href = "${srcPrefix}${presentation.theme}"; id = "theme" }
            link { rel = "stylesheet"; href = "${srcPrefix}plugin/highlight/monokai.css"; id = "highlight-theme" }

            if (presentation.config.copyCode)
                link { rel = "stylesheet"; href = "${srcPrefix}plugin/copycode/copycode.css" }

            if (presentation.css.isNotEmpty()) {
                style {
                    type = "text/css"; media = "screen"
                    rawHtml(presentation.css)
                }
            }
        }
    }

    fun HTML.generateBody(presentation: Presentation, srcPrefix: String) {
        body {
            div("reveal") {
                div("slides") {
                    presentation.slides
                        .forEach { slide ->
                            slide(this)
                        }
                }
            }

            rawHtml("\n")

            if (presentation.config.copyCode) {
                presentation.jsFiles += "plugin/copycode/copycode.js"
                presentation.jsFiles += "https://cdnjs.cloudflare.com/ajax/libs/clipboard.js/2.0.6/clipboard.min.js"
            }

            presentation.jsFiles.forEach {
                script { src = if (it.startsWith("http")) it else "$srcPrefix$it" }
                rawHtml("\n")
            }
            rawHtml("\n")
            script {
                rawHtml(presentation.config.toJS())
            }
        }
    }

    fun HTMLTag.rawHtml(html: String) = unsafe { raw(html) }
}