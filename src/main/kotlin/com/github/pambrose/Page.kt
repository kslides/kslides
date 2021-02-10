package com.github.pambrose

import kotlinx.html.*
import kotlinx.html.stream.createHTML

object Page {
    fun generatePage(slidedeck: SlideDeck) =
        createHTML()
            .html {
                generateHead()
                generateBody(slidedeck)
            }.replace("data-markdown=\"true\"", "data-markdown")

    fun HTML.generateHead() {
        head {
            meta { charset = "utf-8" }
            meta {
                name = "apple-mobile-web-app-capable"
                content = "yes"
            }
            meta {
                name = "apple-mobile-web-app-status-bar-style"
                content = "black-translucent"
            }
            meta {
                name = "viewport"
                content = "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"
            }

            title { +"kslides" }

            link { rel = "stylesheet"; href = "dist/reset.css" }
            link { rel = "stylesheet"; href = "dist/reveal.css" }
            link { rel = "stylesheet"; href = "dist/theme/black.css"; id = "theme" }
            link { rel = "stylesheet"; href = "plugin/highlight/monokai.css"; id = "highlight-theme" }
        }
    }

    fun HTML.generateBody(slidedeck: SlideDeck) {
        body {
            div("reveal") {
                div("slides") {
                    slidedeck.slides
                        .onEach { slide ->
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