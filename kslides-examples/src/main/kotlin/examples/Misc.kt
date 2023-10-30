package examples

import com.kslides.*
import kotlinx.css.Color
import kotlinx.css.color
import kotlinx.html.*

fun main() {
  kslides {
    presentationConfig {
      slideNumber = "c/t"
      transition = Transition.FADE
      gaPropertyId = "G-XXXXXXXXX"

      view = ViewType.DEFAULT
      scrollProgress = true

      slideConfig {
        backgroundColor = "green"
        markdownSeparator = "---"
        markdownVerticalSeparator = "--"
      }
    }

    output {
      enableFileSystem = true
      enableHttp = true
    }

    presentation {
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

      presentationConfig {
        title = "markdown Demo"
        theme = PresentationTheme.MOON
        hash = true
        history = true
        transition = Transition.SLIDE
        transitionSpeed = Speed.FAST
        enableMenu = false

        menuConfig {
          numbers = true
          markers = false
          openOnInit = true
        }

        slideConfig {
          backgroundColor = "yellow"
        }
      }

      markdownSlide {
        filename = "markdown.md"

        slideConfig {
          backgroundColor = "red"
        }
      }

      htmlSlide {
        autoAnimate = true

        content {
          """
          <pre data-id="code-animation"><code data-trim="" data-line-numbers="">
          ${include("kslides-examples/src/main/kotlin/content/assign.js", linePattern = "4-9")}
          </code></pre>
          """
        }
      }

      htmlSlide {
        autoAnimate = true

        content {
          """
          <pre data-id="code-animation">
          <code data-trim="" data-line-numbers="">
          ${include("kslides-examples/src/main/kotlin/content/assign.js")}
          </code>
          </pre>
          """
        }
      }

      htmlSlide {
        autoAnimate = true

        content {
          "<h1>Auto-Animate HTML</h1>"
        }
      }

      htmlSlide {
        autoAnimate = true

        content {
          """
          <h1 style="margin-top: 200px; color: red;">Auto-Animate HTML</h1>
          """
        }
      }

      dslSlide {
        autoAnimate = true

        content {
          h1 { +"Auto-Animate DSL" }
        }
      }

      dslSlide {
        autoAnimate = true

        content {
          h1 {
            style = "margin-top: 100px; color: red;"
            +"Auto-Animate DSL"
          }
        }
      }

      markdownSlide {
        slideConfig {
          backgroundColor = "blue"
        }

        content {
          """
          # Java Code  
          ```java [3|4|5]
          ${include("kslides-examples/src/main/kotlin/content/HelloWorldJ.java")}
          ```
          """
        }
      }

      dslSlide {
        slideConfig {
          backgroundIframe = "https://www.readingbat.com"
        }

        content {}
      }

      markdownSlide {
        slideConfig {
          backgroundColor = "#4370A5"
        }

        content {
          """
          # Markdown Example
          ```kotlin [3,7|4,6|5]
          ${include("kslides-examples/src/main/kotlin/content/HelloWorldK.kt")}
          ```
          """
        }
      }

      markdownSlide {
        content {
          """
          # Markdown Example
          ````kotlin [3,7|4,6|5]
          ${include(githubRawUrl("kslides", "kslides", "kslides-examples/src/main/kotlin/content/HelloWorldK.kt"))}
          ````
          """
        }
      }

      markdownSlide {
        slideConfig {
          backgroundColor = "#000433"
          transition = Transition.CONCAVE
          transitionSpeed = Speed.SLOW
        }

        content {
          """
          # Markdown List Items 
          * Item 1
          * Item 2
          * Item 3  
          """
        }
      }

      dslSlide {
        slideConfig {
          backgroundColor = "#4D55A1"
        }

        content {
          h1 { +"DSL Slide" }
          h2 { +"This is an H2" }
          h3 { +"This is an H3" }
          p { +"This is a P" }
        }
      }

      htmlSlide {
        slideConfig {
          backgroundColor = "#4D55A1"
        }

        content {
          """ 
          <h1>HTML Slide</h1>
          <h2>This is an H2</h2> 
          <h3>This is an H3</h3>
          <p>This is a P</p>
          """
        }
      }

      dslSlide {
        id = "home"

        slideConfig {
          backgroundColor = "#3DDFF1"
        }

        content {
          h3 { +"Examples" }
          h4 { a { href = "/demo.html"; +"Demo Deck" } }
        }
      }

      dslSlide {
        slideConfig {
          transition = Transition.ZOOM
          transitionSpeed = Speed.SLOW
          backgroundColor = "#bb00bb"
        }

        content {
          img { src = "https://picsum.photos/512/512" }
        }
      }

      verticalSlides {
        dslSlide {
          slideConfig {
            backgroundColor = "aquamarine"
          }
          content {
            h2 { +"🐟" }
          }
        }

        dslSlide {
          slideConfig {
            transition = Transition.CONCAVE
            transitionSpeed = Speed.SLOW
            backgroundColor = "rgb(70, 70, 255)"
          }

          content {
            h2 { +"🐳" }
          }
        }

        markdownSlide {
          slideConfig {
            backgroundColor = "red"
          }

          content {
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
        }
      }

      dslSlide {
        id = "embed-web-content"

        content {
          h2 { +"Embed Web Content" }

          iframe {
            attributes["data-autoplay"] = "true"
            attributes["frameborder"] = "0"
            width = "700"
            height = "540"
            src = "https://slides.com/news/auto-animate/embed"
          }
        }
      }

      // Slides are separated by three dashes
      markdownSlide {
        content {
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
      }

      // Slides are separated by newline + three dashes + newline, vertical slides identical but two dashes
      markdownSlide {
        slideConfig {
          markdownSeparator = "\r?\\n---\r?\\n"
          markdownVerticalSeparator = "\r?\\n--\r?\\n"
        }

        content {
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
      }

      markdownSlide {
        content {
          """
          A
    
          ---
    
          B
    
          ---
    
          C
          """
        }
      }

      markdownSlide {
        slideConfig {
          backgroundColor = "#FFFF00"
        }

        content {
          """
          ## Slide attributes
          """
        }
      }

      markdownSlide {
        content {
          """
          ## Element attributes
    
          Item 1 ${fragment()}
    
          Item 2 ${fragment()}
    
          Item 3 ${fragment()}
          """
        }
      }

      dslSlide {
        content {
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
      }

      dslSlide {
        content {
          +"Slide 3"
          h4 { a { href = "#/home"; +"Home" } }
        }
      }

      dslSlide {
        slideConfig {
          backgroundIframe = "https://revealjs.com"
        }

        content {
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
      }

      // Images
      markdownSlide {
        content {
          "![Sample image](https://picsum.photos/512/512)"
        }
      }
    }

    presentation {
      path = "demo1"

      dslSlide {
        content {
          +"Demo1 Slide 1"
        }
      }

      dslSlide {
        content {
          +"Demo1 Slide 2"
        }
      }
    }

    presentation {
      path = "demo1/demo2"

      dslSlide {
        content {
          +"Demo2 Slide 1"
        }
      }

      dslSlide {
        content {
          +"Demo2 Slide 2"
        }
      }
    }

    presentation {
      path = "demo3.html"

      dslSlide {
        content {
          +"Demo3 Slide 1"
        }
      }

      dslSlide {
        content {
          +"Demo3 Slide 2"
        }
      }
    }
  }
}
