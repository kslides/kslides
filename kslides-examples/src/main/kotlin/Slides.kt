import com.kslides.*
import kotlinx.css.*
import kotlinx.html.*

fun main() {

  kslides {

    output {
      enableFileSystem = true
      enableHttp = true
    }

    // readme begin
    presentation {
      css += """
        #intro h1 { color: #FF5533; }
        #mdslide p { color: #FF6836; }
      """

      presentationConfig {
        githubCornerHref = githubSourceUrl("pambrose", "kslides", "kslides-examples/src/main/kotlin/Slides.kt")
        githubCornerTitle = "View presentation source on Github"
        slideNumber = "c/t"
        history = true
        transition = Transition.SLIDE
        transitionSpeed = Speed.SLOW
        gaPropertyId = "G-TRY2Q243XC"
        enableSpeakerNotes = true
        enableMenu = true

        slideConfig {
          backgroundColor = "#4370A5"
        }
      }

      markdownSlide {
        id = "intro"

        slideConfig {
          transition = Transition.ZOOM
        }

        content {
          """
          # kslides
          ### A Kotlin DSL wrapper for [reveal.js](https://revealjs.com)
          Notes: This is a note for the opening slide 📝
          """
        }
      }

      markdownSlide {
        id = "mdslide"

        slideConfig {
          transition = Transition.ZOOM
        }

        content {
          """
          # Markdown Slide
          ## 🍒
          
          Use the arrow keys to navigate ${fragment(Effect.FADE_LEFT)}
          
          Press ESC to see the presentation overview ${fragment(Effect.FADE_LEFT)}
                    
          Notes: This is a note for the Markdown slide 📝
          """
        }
      }

      htmlSlide {
        content {
          """
          <h1>HTML Slide</h1>
          <h2>🐦</h2>
          <p class="fragment fade-up">Press S to see the speaker notes</p> 
          <p class="fragment fade-up">Press M to see the menu</p> 
          <aside class="notes">
            This is a note for the HTML slide 📝
          </aside>
          """
        }
      }

      dslSlide {
        slideConfig {
          transition = Transition.ZOOM
        }

        content {
          h1 { +"DSL Slide" }
          h2 { +"👀" }
          p("fragment fade-right") { +"Press CTRL+Shift+F to search the slide content" }
          p("fragment fade-right") { +"Press Alt+click to zoom in on elements" }
          aside("notes") {
            +"This is a note for the DSL slide 📝"
          }
        }
      }

      markdownSlide {
        content {
          """
          ## Code Highlights    
          ```kotlin [3,7|4,6|5]
          ${includeFile("kslides-examples/src/main/kotlin/examples/HelloWorldK.kt")}
          ```
          Note: This slide shows simple code highlights. You can specify the language and the lines 
          you want to highlight
          """
        }
      }

      // We use a for loop here to generate a series of slides, each with a different set of lines
      // We use the same syntax used by revealjs: https://revealjs.com/code/
      for (lines in lineNumbers("[5,6,9|5-9|]")) {
        htmlSlide {
          autoAnimate = true
          content {
            """
            <h2>Animated Code</h2>  
            <pre data-id="code-animation" data-cc="false"> 
              <code data-trim="" data-line-numbers="">
                ${includeFile("kslides-examples/src/main/kotlin/examples/assign.js", lines)}
              </code>
            </pre>
            Notes: This slide shows animated code highlights. 
            Indicate the lines you want to highlight by using a for loop
            """
          }
        }
      }

      verticalSlides {
        dslSlide {
          slideConfig {
            backgroundVideo = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
          }

          content {
            h1 {
              style = "color: red;"
              +"Vertical DSL Slide 👇"
            }
          }
        }

        markdownSlide {
          content {
            """
            # Vertical Markdown Slide 🦊 
            
            [Go back to the 1st slide](#/) ${fragment()}
         
            [Go back to the 2nd slide](#/1) ${fragment()}
            
            """
          }
        }
      }

      markdownSlide {
        content {
          """
          ## Presentation Definition    
          ```kotlin []
          ${
            includeFile(
              "kslides-examples/src/main/kotlin/Slides.kt",
              beginToken = "readme begin",
              endToken = "readme end"
            )
          }
          ```
          """
        }
      }
    }
    // readme end

    // helloworld begin
    presentation {
      // Makes this presentation available at helloworld.html
      path = "helloworld.html"

      // css styles can be specified as a string or with the kotlin css DSL
      css += """
        .htmlslide h2 {
          color: yellow;
        }
      """

      css {
        rule("#mdslide h2") {
          color = Color.green
        }
      }

      presentationConfig {
        transition = Transition.FADE

        slideConfig {
          backgroundColor = "#2A9EEE"
        }
      }

      markdownSlide {
        id = "mdslide"

        content {
          """
        # Markdown
        ## Hello World
        """
        }
      }

      htmlSlide {
        classes = "htmlslide"

        slideConfig {
          backgroundColor = "red"
        }

        content {
          """
        <h1>HTML</h1>
        <h2>Hello World</h2>
        """
        }
      }

      dslSlide {
        content {
          h1 { +"DSL" }
          h2 { +"Hello World" }
        }
      }

      markdownSlide {
        content {
          """
          ## Presentation Definition    
          ```kotlin []
          ${
            includeFile(
              "kslides-examples/src/main/kotlin/Slides.kt",
              beginToken = "helloworld begin",
              endToken = "helloworld end"
            )
          }
          ```
          """
        }
      }
    }
    // helloworld end
  }
}