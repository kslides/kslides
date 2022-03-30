package com.github.readingbat.examples

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
          ```kotlin [1|3,8|4|5-7]
          ${includeFile("src/test/kotlin/examples/HelloWorldK.kt")}
          ```
          """
        }
      }

      markdownSlide {
        slideConfig {
          backgroundColor = "lightblue"
        }

        content {
          """
          ## Java Code Highlights    
          ```java [1|3,7|4,6|5]
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
    }
  }
}