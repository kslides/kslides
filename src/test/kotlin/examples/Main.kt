package examples

import com.kslides.*
import com.kslides.Presentation.Companion.outputPresentations
import kotlinx.css.Color
import kotlinx.css.color
import kotlinx.html.*

fun main() {

  defaultConfig {
    transition = Transition.FADE
  }

  presentation(title = "markdown Demo", theme = Theme.MOON) {

    css += """
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
      rule(".slides section h4 a") {
        color = Color.red
      }
    }

    markdownSlide {
      """
      # Java Code  
      ```java [3|4|5]
      ${includeFile("src/test/kotlin/examples/HelloWorldJ.java")}
      ```
      """.trimIndentWithInclude()
    }.config {
      backgroundColor = "#4DD0A5"
    }

    htmlSlide {
    }.config {
      backgroundIframe = "https://www.readingbat.com"
    }

    markdownSlide {
      """
      # Code Highlights    
      ```kotlin [1,6|2,5|3-4]
      fun main() {
        repeat(10) {
          println("Hello")
          println("World")
        }
      }
      ```
      """
    }.config {
      backgroundColor = "#4370A5"
    }

    markdownSlide {
      """
      # Markdown Example
      ```kotlin [3|4|5]
      ${includeFile("src/test/kotlin/examples/HelloWorldK.kt")}
      ```
      """.trimIndentWithInclude()
    }

    markdownSlide {
      """
      # Markdown Example
      ````kotlin [1|3-4|20,24-25]
      ${includeUrl("https://raw.githubusercontent.com/pambrose/kslides/master/src/test/kotlin/examples/HelloWorldK.kt")}
      ````
      """.trimIndentWithInclude()
    }

    markdownSlide {
      """
        # Markdown List Items 
        * Item 1
        * Item 2
        * Item 3
      """
    }.config {
      backgroundColor = "#000433"
      transition = Transition.CONCAVE
      transitionSpeed = Speed.SLOW
    }


    rawHtmlSlide {
      """
      <h1>Raw Slide</h1>
      <h2>This is an H2</h2> 
      <h3>This is an H3</h3>
      <p>This is a P</p>
      """
    }

    htmlSlide(id = "home") {
      h3 { +"Examples" }
      h4 { a { href = "/demo.html"; +"Demo Deck" } }
    }.config {
      backgroundColor = "#4DDFA1"
    }

    htmlSlide {
      img { src = "https://picsum.photos/512/512" }
    }.config {
      transition = Transition.ZOOM
      transitionSpeed = Speed.SLOW
      backgroundColor = "#bb00bb"
    }

    verticalSlides {
      htmlSlide {
        h2 { +"🐟" }
      }.config {
        backgroundColor = "aquamarine"
      }

      htmlSlide {
        h2 { +"🐳" }
      }.config {
        transition = Transition.CONCAVE
        transitionSpeed = Speed.SLOW
        backgroundColor = "rgb(70, 70, 255)"
      }

      markdownSlide {
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
      }.config {
        backgroundColor = "red"
      }

      //markdownSlide(filename = "public/markdown.md")
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

    //markdownSlide(filename = "/public/markdown.md", separator = "^---", vertical_separator = "^--")

    // Slides are separated by three dashes
    markdownSlide(separator = "---") {
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
    }

    // Slides are separated by newline + three dashes + newline, vertical slides identical but two dashes
    markdownSlide(separator = "\r?\\n---\r?\\n", vertical_separator = "\r?\\n--\r?\\n") {
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
    }

    markdownSlide {
      """
      A

      ---

      B

      ---

      C
      """
    }

    // Slide attributes
    markdownSlide {
      """
      ## Slide attributes
      """
    }.config {
      backgroundColor = "#FFFF00"
    }

    markdownSlide {
      """
      ## Element attributes

      Item 1 ${fragmentIndex(1)}

      Item 2 ${fragmentIndex(2)}

      Item 3 ${fragmentIndex(3)}
      """
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
      h4 { a { href = "#/home"; +"Home" } }
    }

    htmlSlide {
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
    }.config {
      backgroundIframe = "https://revealjs.com"
    }

    // Images
    markdownSlide {
      "![Sample image](https://picsum.photos/512/512)"
    }
  }.config {
    hash = true
    history = true
    transition = Transition.SLIDE
    transitionSpeed = Speed.FAST
    slideNumber = "c/t"
    enableMenu = false

    menu {
      numbers = true
      markers = false
      openOnInit = true
    }
  }

  presentation("demo1") {
    htmlSlide {
      +"Demo1 Slide 1"
    }

    htmlSlide {
      +"Demo1 Slide 2"
    }
  }

  presentation("demo1/demo2") {
    htmlSlide {
      +"Demo2 Slide 1"
    }

    htmlSlide {
      +"Demo2 Slide 2"
    }
  }

  presentation("demo3.html") {
    htmlSlide {
      +"Demo3 Slide 1"
    }

    htmlSlide {
      +"Demo3 Slide 2"
    }
  }

  //servePresentations()
  outputPresentations()
}