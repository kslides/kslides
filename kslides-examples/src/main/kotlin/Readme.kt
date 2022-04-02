import com.kslides.*
import kotlinx.html.*

fun main() {

  kslides {

    output {
      enableFileSystem = true
      enableHttp = true
    }

    presentation {

      css += """
        #intro h1 { color: #FF5533; }
        #mdslide p { color: black; }
      """

      presentationConfig {
        githubCornerHref = githubSourceUrl("pambrose", "kslides", "kslides-examples/src/main/kotlin/Readme.kt")
        githubCornerTitle = "View presentation source on Github"

        slideNumber = "c/t"
        history = true
        transition = Transition.SLIDE
        transitionSpeed = Speed.SLOW
        gaPropertyId = "G-TRY2Q243XC"

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
          
          Use the arrow keys to navigate ${fragmentIndex(1)}
          
          Press ESC to see presentation overview ${fragmentIndex(2)}
          
          Press S to see speaker notes ${fragmentIndex(3)}

          """
        }
      }

      htmlSlide {
        content {
          """
          <h1>HTML Slide</h1>
          <h2>🐦</h2>
          <p>Use the arrow keys to navigate</p>
          <aside class="notes">
            These are notes for the htmlSlide 📝
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
          p { +"Use the arrow keys to navigate" }
          aside("notes") {
            +"These are notes for the dslSlide ⚾"
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
          """
        }
      }

      verticalSlides {

        htmlSlide {
          autoAnimate = true

          content {
            """
            <h2>Animated Code 👇</h2>  
            <pre data-id="code-animation" data-cc="false"> 
              <code data-trim="" data-line-numbers="">
                ${includeFile("kslides-examples/src/main/kotlin/examples/assign.js", "[5-6,9]")}
              </code>
            </pre>
            """
          }
        }

        htmlSlide {
          autoAnimate = true

          content {
            """
            <h2>Animated Code 👇</h2>  
            <pre data-id="code-animation" data-cc="false"> 
              <code data-trim="" data-line-numbers="">
                ${includeFile("kslides-examples/src/main/kotlin/examples/assign.js", "[5-9]")}
              </code>
            </pre>
            """
          }
        }

        dslSlide {
          autoAnimate = true

          content {
            h2 { +"Animated Code 👇" }
            pre {
              attributes["data-id"] = "code-animation"
              attributes["data-cc"] = "false"
              code {
                attributes["data-trim"] = ""
                attributes["data-line-numbers"] = ""
                +includeFile("kslides-examples/src/main/kotlin/examples/assign.js", indentToken="")
              }
            }
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
            
            [Go back to the 1st slide](#/) ${fragmentIndex(1)}
         
            [Go back to the 2nd slide](#/1) ${fragmentIndex(2)}
            
            """
          }
        }
      }

      markdownSlide {
        content {
          """
          ## Presentation Definition    
          ```kotlin []
          ${includeUrl(githubRawUrl("pambrose", "kslides", "kslides-examples/src/main/kotlin/Readme.kt"))}
          ```
          """
        }
      }
    }
  }
}