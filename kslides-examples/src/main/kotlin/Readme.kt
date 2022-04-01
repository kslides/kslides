import com.kslides.*
import kotlinx.html.*

fun main() {

  kslides {

    output {
      enableFileSystem = true
      enableHttp = true
    }

    presentation {

      presentationConfig {
        slideNumber = "c/t"
        history = true
        transition = Transition.SLIDE
        transitionSpeed = Speed.SLOW

        slideConfig {
          backgroundColor = "#4370A5"
        }
      }

      markdownSlide {
        slideConfig {
          transition = Transition.ZOOM
          transitionSpeed = Speed.FAST
        }

        content {
          """
          # Markdown Slide
          ## 🍒
          Press ESC to see presentation overview.
          """
        }
      }

      htmlSlide {
        content {
          """
          <h1>HTML Slide</h1>
          <h2>🐦</h2>
          <p>Use the arrow keys to navigate</p>
          """
        }
      }

      dslSlide {
        slideConfig {
          transition = Transition.ZOOM
          transitionSpeed = Speed.FAST
        }

        content {
          h1 { +"DSL Slide" }
          h2 { +"👀" }
          p { +"Use the arrow keys to navigate" }
        }
      }

      markdownSlide {
        content {
          """
          ## Kotlin Code Highlights    
          ```kotlin [3,7|4,6|5]
          ${includeFile("kslides-examples/src/main/kotlin/examples/HelloWorldK.kt")}
          ```
          """
        }
      }


      markdownSlide {
        slideConfig {
          backgroundColor = "#BB4400"
        }

        content {
          """
          ## Java Code Highlights    
          ```java [1|3,7|4,6|5]
          ${includeFile("kslides-examples/src/main/kotlin/examples/HelloWorldJ.java")}
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

        htmlSlide {
          autoAnimate = true

          content {
            """
          <h2>Animated Code 👇</h2>  
          <pre data-id="code-animation" data-cc="false">
            <code data-trim="" data-line-numbers="">
              ${includeFile("kslides-examples/src/main/kotlin/examples/assign.js")}
            </code>
          </pre>
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
            
            [Go back to the 1st slide](#/) ${fragmentIndex(1)}
         
            [Go back to the 2nd slide](#/1) ${fragmentIndex(2)}
            
            """
          }
        }
      }

      markdownSlide {
        content {
          """
          ## Presentation Description    
          ```kotlin []
          ${includeUrl("${githubPrefix("pambrose", "kslides", "kslides-examples")}/src/main/kotlin/Readme.kt")}
          ```
          """
        }
      }
    }
  }
}