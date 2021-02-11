package com.github.pambrose

import com.github.pambrose.Presentation.Companion.present
import kotlinx.html.*

fun main() {

    presentation(title = "My Presentation", theme = Theme.Moon) {

        htmlSlide("home") {
            h3 { +"Examples" }
            h4 { a { href = "/demo.html"; +"Demo Deck" } }
        }

        markdownSlide {
            markdown { +"![Sample image](https://s3.amazonaws.com/static.slid.es/logo/v2/slides-symbol-512x512.png)" }
        }

        htmlSlide {
            img { src = "https://s3.amazonaws.com/static.slid.es/logo/v2/slides-symbol-512x512.png" }
        }

        section("embed-web-content") {
            h2 { +"Embed Web Content" }
            h4 { a { href = "#/home"; +"Home" } }

            iframe {
                attributes["data-autoplay"] = "true"
                attributes["frameborder"] = "0"
                width = "700"
                height = "540"
                src = "https://slides.com/news/auto-animate/embed"
                //frameborder = "0"
            }
        }

        markdownSlide("markdown-example") {
            markdown {
                +"""
                    # Markdown Example
                    ```kotlin [1|3-5]
                        fun main() {
                            println("Hello")
                        }
                    ```
                    
                    ```php [1|3-5]
                        public function foo()
                        {
                            '$'foo = array('bar' => 'bar')
                        }
                     ```

                """.trimIndent()
            }
        }

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
        }

        htmlSlide {
            attributes["data-background-iframe"] = "https://athenian.org"
            attributes["data-background-interactive"] = "true"
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