import com.kslides.*
import com.kslides.Include.includeFile
import com.kslides.Include.includeUrl
import kotlinx.html.*

fun main() {

  kslides {

    output {
      enableFileSystem = true
      enableHttp = true
    }

    presentation {

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
        slideConfig {
          transition = Transition.ZOOM
          transitionSpeed = Speed.FAST
        }

        content {
          """
          # kslides
          A Kotlin DSL wrapper for [reveal.js](https://revealjs.com)
          """
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