package com.github.pambrose

import kotlinx.html.*
import kotlinx.html.stream.createHTML

object Page {
    fun generatePage(presentation: Presentation) =
        createHTML()
            .html {
                generateHead(presentation)
                generateBody(presentation)
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
}