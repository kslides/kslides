package com.github.readingbat.examples

import com.kslides.*
import kotlinx.html.*

fun main() {
  kslides {
    output {
      enableHttp = true
    }

    presentation {

      presentationConfig {
        history = true
        transition = Transition.SLIDE
        transitionSpeed = Speed.SLOW
      }

      markdownSlide {
        id = "start"

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
        content {
          h1 { +"DSL Slide" }
          h2 { +"🐦" }
          p { +"Use the arrow keys to navigate" }
        }
      }


      markdownSlide {
        slideConfig {
          backgroundColor = "#4370A5"
        }

        content {
          """
          # Code Highlights    
          ```kotlin [1|2,5|3-4]
          ${includeFile("src/test/kotlin/examples/HelloWorldK.kt")}
          ```
          """
        }
      }

      markdownSlide {
        slideConfig {
          backgroundColor = "#DD70A5"
        }

        content {
          """
          # Code Highlights    
          ```java [1|2|3]
          ${includeFile("src/test/kotlin/examples/HelloWorldJ.java")}
          ```
          """
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
              +"Vertical HTML Slide 👇"
            }
          }
        }

        markdownSlide {
          content {
            """
            # Vertical Markdown Slide 🦊 
            
            [Go back to the 1st slide](#/start) ${fragmentIndex(1)}
         
            [Go back to the 2nd slide](#/1) ${fragmentIndex(2)}
            
            """
          }
        }
      }
    }
  }
}