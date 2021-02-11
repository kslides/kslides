package com.github.pambrose

import kotlinx.html.*
import kotlinx.html.consumers.filter
import kotlinx.html.dom.append
import kotlinx.html.dom.document
import kotlinx.html.dom.serialize

object Page {
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
            if (node?.nodeName == "script") {
                if (node.attributes.getNamedItem("type")?.nodeValue == "text/template") {
                    escapedChars.forEach {
                        node.firstChild.nodeValue = node.firstChild.nodeValue.replace(it.first, it.second)
                    }
                }
            }
        }

        var str = document.serialize()
        escapedChars.forEach {
            str = str.replace(it.second, it.first)
        }
        return str
    }

    fun test() {
        println(document {
            append.filter { if (it.tagName == "div") SKIP else PASS }.html {
                body {
                    div {
                        a { +"link1" }
                    }
                    a { +"link2" }
                }
            }
        }.serialize())
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
        }
    }

    fun HTML.generateBody(presentation: Presentation) {
        body {
            div("reveal") {
                div("slides") {
                    presentation.slides
                        .forEach { slide ->
                            slide.content(this)
                        }
                }
            }

            script { src = "dist/reveal.js" }
            script { src = "plugin/zoom/zoom.js" }
            script { src = "plugin/notes/notes.js" }
            script { src = "plugin/search/search.js" }
            script { src = "plugin/markdown/markdown.js" }
            script { src = "plugin/highlight/highlight.js" }

            script {
                rawHtml(
                    """
                        Reveal.initialize({
                            hash: true,
                            plugins: [RevealZoom, RevealSearch, RevealMarkdown, RevealHighlight]
                        });
                    """.trimIndent()
                )
            }
        }
    }

    fun HTMLTag.rawHtml(html: String) = unsafe { raw(html) }
}