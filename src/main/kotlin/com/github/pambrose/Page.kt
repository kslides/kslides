package com.github.pambrose

import kotlinx.html.*
import kotlinx.html.stream.createHTML

object Page {
    fun generatePage(slidedeck: SlideDeck) =
        createHTML()
            .html {
                generateHead()
                generateBody(slidedeck)
            }

    fun HTML.generateHead() {
        head {
            meta { charset = "utf-8" }
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

                    for (slide in slidedeck.slides)
                        slide.content(this)

                    section {
                        attributes["data-markdown"] = "true"
                        id = "page1"
                        script {
                            type = "text/template"
                            +"""
                                            ## Page 1 of My Slides
                                            This is a story with multiple paths:
                                            * [Page1](#/page1)
                                            * [Page2](#/page2)
                                        """.trimIndent()
                        }
                    }
                }
            }

            script { src = "dist/reveal.js" }
            script { src = "plugin/notes/notes.js" }
            script { src = "plugin/markdown/markdown.js" }
            script { src = "plugin/highlight/highlight.js" }
            script {
                rawHtml(
                    """
                                Reveal.initialize({
                                    hash: true,
                                    plugins: [RevealMarkdown, RevealHighlight, RevealNotes]
                                });
                            """.trimIndent()
                )
            }
        }
    }
}