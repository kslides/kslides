package examples

import com.kslides.*
import com.kslides.Presentation.Companion.outputPresentations
import com.kslides.SlideConfig.Companion.slideConfig
import com.kslides.Transition.Concave
import kotlinx.css.Color
import kotlinx.css.color
import kotlinx.html.*

fun main() {
  presentation(title = "markdown Demo", theme = Theme.Moon) {

    config {
      hash = true
      history = true
      transition = Transition.Slide
      transitionSpeed = Speed.Slow
      slideNumber = "c/t"
      enableMenu = false

      menu {
        numbers = true
        markers = false
        openOnInit = true
      }

    }

    css += """
			.slides section h3 {
				color: green;
			}
			.slides section h4 a {
				color: red;
			}

      .code-output {
        text-align: left;  
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

    markdownSlide(slideConfig { backgroundColor = "#4DD0A5" }) {
      """
      # Kotlin Playground   
      ${playground(includeFile("src/test/kotlin/examples/HelloWorldK.kt"))}
      """.trimIndentWithPlayground()
    }

    markdownSlide(slideConfig { backgroundColor = "#4DD0A5" }) {
      """
      # Java Code   
      ${playground(includeFile("src/test/kotlin/examples/HelloWorldJ.java"), PlaygroundLanguage.Java)}
      """.trimIndentWithPlayground()
    }

    markdownSlide(slideConfig { backgroundColor = "#4DD0A5" }) {
      """
      # Java Code  
      ```java [1|3]
      ${includeFile("src/test/kotlin/examples/HelloWorldJ.java")}
      ```
      """.trimIndentWithInclude()
    }

    markdownSlide(slideConfig { backgroundColor = "#4370A5" }) {
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
    }

    markdownSlide {
      """
      # Markdown Example
      ````kotlin [1|3-4|20,24-25]
      ${includeFile("src/test/kotlin/examples/Simple.kt")}
      ````
      """.trimIndentWithInclude()
    }

    markdownSlide {
      """
      # Markdown Example
      ````kotlin [1|3-4|20,24-25]
      ${includeUrl("https://raw.githubusercontent.com/pambrose/kslides/master/src/test/kotlin/examples/Simple.kt")}
      ````
      """.trimIndentWithInclude()
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
    }

    htmlSlide(slideConfig { transition(Transition.Zoom, Speed.Slow); backgroundColor = "#bb00bb" }) {
      img { src = "https://picsum.photos/512/512" }
    }

    verticalSlides {
      htmlSlide(slideConfig { backgroundColor = "aquamarine" }) {
        h2 { +"🐟" }
      }

      htmlSlide(slideConfig { transition(Concave, speed = Speed.Slow); backgroundColor = "rgb(70, 70, 255)" }) {
        h2 { +"🐳" }
      }

      markdownSlide {
        """
        # Markdown Slide
        """
      }

      markdownSlide(slideConfig { backgroundColor = "red" }) {
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
    markdownSlide(slideConfig { backgroundColor = "#FFFF00" }) {
      """
      ## Slide attributes
      """
    }

    markdownSlide {
      """
      ## Element attributes

      Item 1 ${fragmentIndex(1)}

      Item 2 ${fragmentIndex(2)}

      Item 3 ${fragmentIndex(3)}
      """
    }

    markdownSlide(id = "markdown-example") {
      """
      # Markdown Example
      ```kotlin [1|3-4]
          fun main() {
              println("Hello")
              println("World")
          }
      ```
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
    markdownSlide {
      "![Sample image](https://picsum.photos/512/512)"
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