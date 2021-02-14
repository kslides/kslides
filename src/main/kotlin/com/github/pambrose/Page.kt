package com.github.pambrose

import com.github.pambrose.common.util.isNotNull
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.dom.document
import kotlinx.html.dom.serialize

internal object Page {
    fun generatePage(presentation: Presentation): String {
        val document =
            document {
                append.html {
                    generateHead(presentation)
                    generateBody(presentation)
                }
            }

        val escapedChars =
            listOf(
                "\"" to "_*_quote_*_",
                "<" to "_*_lt_*_",
                ">" to "_*_gt_*_",
                "&" to "_*_amp_*_"
            )

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

        var str = document.serialize()
//        escapedChars.forEach {
//            str = str.replace(it.second, it.first)
//        }
        return str
    }

    fun HTML.generateHead(presentation: Presentation) {
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

            link { rel = "stylesheet"; href = "dist/reset.css" }
            link { rel = "stylesheet"; href = "dist/reveal.css" }
            link { rel = "stylesheet"; href = presentation.theme; id = "theme" }
            link { rel = "stylesheet"; href = "plugin/highlight/monokai.css"; id = "highlight-theme" }

            if (presentation.css.isNotEmpty()) {
                style {
                    type = "text/css"; media = "screen"
                    rawHtml(presentation.css)
                }
            }
        }
    }

    fun HTML.generateBody(presentation: Presentation) {
        body {
            div("reveal") {
                div("slides") {
                    presentation.slides
                        .forEach { slide ->
                            slide(this)
                        }
                }
            }

            script { src = "dist/reveal.js" }
            rawHtml("\n\t")
            script { src = "plugin/zoom/zoom.js" }
            rawHtml("\n\t")
            script { src = "plugin/notes/notes.js" }
            rawHtml("\n\t")
            script { src = "plugin/search/search.js" }
            rawHtml("\n\t")
            script { src = "plugin/markdown/markdown.js" }
            rawHtml("\n\t")
            script { src = "plugin/highlight/highlight.js" }
            rawHtml("\n\t")
            script {
                rawHtml(
                    """
                        Reveal.initialize({
                            hash: true,
                            plugins: [${presentation.plugins.joinToString(", ")}]
                        });
                    """.trimIndent()
                )
            }
        }
    }

    fun HTMLTag.rawHtml(html: String) = unsafe { raw(html) }
}