package com.github.readingbat.examples

import com.github.pambrose.*
import com.github.pambrose.Presentation.Companion.present
import com.github.pambrose.SlideConfig.Companion.slideConfig
import com.github.pambrose.Speed.Slow
import com.github.pambrose.Theme.Moon
import com.github.pambrose.Transition.Concave
import kotlinx.css.Color
import kotlinx.css.color
import kotlinx.html.*

fun main() {
    presentation(title = "markdown Demo", theme = Moon) {
        css = """
			.slides section h3 {
				color: green;
			}
			.slides section h4 a {
				color: red;
			}
        """

        css {
            rule(".slides section h3") {
                color = Color.green
            }
        }

        markdownSlide(
            """
                # Markdown Example
                ````kotlin [1|3-4|20,24-25]
                ${includeFile(path = "src/main/kotlin/Simple.kt")}
                ````
             """
        )

        markdownSlide(
            """
                # Markdown Example
                ```kotlin [1|3-4|20,24-25]
                ${includeUrl(source = "https://raw.githubusercontent.com/pambrose/kslides/master/src/main/kotlin/Simple.kt")}
                ```
             """
        )

        rawHtmlSlide {
            """
            <h1>Raw Slide</h1>
            <h2>This is a raw slide</h2>
            <h3>This is a raw slide</h3>
            <p>This is a raw slide</p>
            """
        }

        htmlSlide(id = "home") {
            h3 { +"Examples" }
            h4 { a { href = "/demo.html"; +"Demo Deck" } }
        }

        htmlSlide(slideConfig { transition(Transition.Zoom, Slow); backgroundColor = "#bb00bb" }) {
            img { src = "https://picsum.photos/512/512" }
        }

        verticalSlides {
            htmlSlide(slideConfig { backgroundColor = "aquamarine" }) {
                h2 { +"🐟" }
            }

            htmlSlide(slideConfig { transition(Concave, speed = Slow); backgroundColor = "rgb(70, 70, 255)" }) {
                h2 { +"🐳" }
            }

            markdownSlide(
                """
                # Markdown Slide
                """
            )

            markdownSlide(
                config = slideConfig { backgroundColor = "red" },
                content =
                """
                ## Demo 1
                Slide 1
                
                ---

                ## Demo 1
                Slide 2
                
                ---
                
                ## Demo 1
                Slide 3
                """
            )

            markdownSlide(filename = "public/markdown2.md")
        }

        htmlSlide(id = "embed-web-content") {
            h2 { +"Embed Web Content" }

            iframe {
                attributes["data-autoplay"] = "true"
                attributes["frameborder"] = "0"
                width = "700"
                height = "540"
                src = "https://slides.com/news/auto-animate/embed"
            }
        }

        markdownSlide(filename = "/public/markdown.md", separator = "^---", vertical_separator = "^--")

        // Slides are separated by three dashes
        markdownSlide(
            separator = "---", content =
            """
            ## Demo 1
            Slide 1
            ---
            ## Demo 1
            Slide 2
            ---
            ## Demo 1
            Slide 3
            """
        )

        // Slides are separated by newline + three dashes + newline, vertical slides identical but two dashes
        markdownSlide(
            separator = "\r?\\n---\r?\\n", vertical_separator = "\r?\\n--\r?\\n", content =
            """
            ## Demo 2
            Slide 1.1

            --

            ## Demo 2
            Slide 1.2

            ---

            ## Demo 2
            Slide 2
            """
        )

        markdownSlide(
            """
            A

            ---

            B

            ---

            C
            """
        )

        // Slide attributes
        markdownSlide(
            config = slideConfig { backgroundColor = "#FFFF00" },
            content =
            """
                ## Slide attributes
             """
        )

        markdownSlide(
            """
                ## Element attributes
                
                Item 1 ${fragmentIndex(1)}
                
                Item 2 ${fragmentIndex(2)}
                
                Item 3 ${fragmentIndex(3)}
                """
        )

        markdownSlide(
            id = "markdown-example",
            content =
            """
                # Markdown Example
                ```kotlin [1|3-4]
                    fun main() {
                        println("Hello")
                        println("World")
                    }
                ```
                """
        )

        htmlSlide {
            section {
                +"Slide 2"
            }
            section {
                +"Sub 1"
            }
            section {
                +"Sub 2"
            }
        }

        htmlSlide {
            +"Slide 3"
            h4 { a { href = "#/home"; +"Home" } }
        }

        htmlSlide(slideConfig { backgroundIframe = "https://revealjs.com" }) {
            //attributes["data-background-interactive"] = "false"
            div {
                style =
                    "position: absolute; width: 40%; right: 0; box-shadow: 0 1px 4px rgba(0,0,0,0.5), 0 5px 25px rgba(0,0,0,0.2); background-color: rgba(0, 0, 0, 0.9); color: #fff; padding: 20px; font-size: 20px; text-align: left;"
                h2 { +"Iframe Backgrounds" }
                p {
                    +"""Since reveal.js runs on the web, you can easily embed other web content. Try interacting with the
                    page in the background."""
                }
            }
        }

        // Images
        markdownSlide(
            "![Sample image](https://picsum.photos/512/512)"
        )

        config {
            hash = true
        }
    }

    presentation("/demo.html") {
        htmlSlide {
            +"Demo Slide 1"
        }

        htmlSlide {
            +"Demo Slide 2"
        }
    }

    present()
}