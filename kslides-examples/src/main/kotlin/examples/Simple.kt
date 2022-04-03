package examples

import com.kslides.*
import com.kslides.Speed.SLOW
import com.kslides.Transition.SLIDE
import com.kslides.Transition.ZOOM
import kotlinx.html.*

fun main() {
  kslides {

    output {
      // Run the web server
      enableHttp = true
    }

    presentation {

      presentationConfig {
        history = true
        transition = SLIDE
        transitionSpeed = SLOW
      }

      dslSlide {
        content {
          h1 { +"HTML Slide 🐦" }
          p { +"Use the arrow keys to navigate" }
        }
      }

      htmlSlide {

        content {
          """
          <h1>Raw HTML Slide 🐦</h1>
          <h2>HTML Slide 🐦</h2>
          <h3>HTML Slide 🐦</h3>
          <p>This is a test</p>
          """
        }
      }

      markdownSlide {

        slideConfig {
          transition = ZOOM
          transitionSpeed = SLOW
        }
        content {
          """
          # Markdown Slide 🍒 
          
          Press ESC to see presentation overview.
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
          """
          # Vertical Markdown Slide 🦊 
          
          [Go back to the 1st slide](#/0) ${fragment()}
       
          [Go back to the 2nd slide](#/1) ${fragment()}
          """
        }
      }
    }
  }
}