package examples

import com.kslides.*
import com.kslides.Presentation.Companion.servePresentations
import com.kslides.SlideConfig.Companion.slideConfig
import com.kslides.Speed.SLOW
import com.kslides.Transition.SLIDE
import com.kslides.Transition.ZOOM
import kotlinx.html.*

fun main() {
  presentation {

    config {
      history = true
      transition = SLIDE
      transitionSpeed = SLOW
    }

    htmlSlide {
      h1 { +"HTML Slide 🐦" }
      p { +"Use the arrow keys to navigate" }
    }

    rawHtmlSlide {
      """
      <h1>Raw HTML Slide 🐦</h1>
      <h2>HTML Slide 🐦</h2>
      <h3>HTML Slide 🐦</h3>
      <p>This is a test</p>
      """
    }

    markdownSlide(slideConfig { transition(ZOOM, SLOW) }) {
      """
      # Markdown Slide 🍒 
      
      Press ESC to see presentation overview.
      """
    }

    verticalSlides {
      htmlSlide(slideConfig { backgroundVideo = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4" }) {
        h1 {
          style = "color: red;"
          +"Vertical HTML Slide 👇"
        }
      }

      markdownSlide {
        """
        # Vertical Markdown Slide 🦊 
        
        [Go back to the 1st slide](#/0) ${fragmentIndex(1)}
     
        [Go back to the 2nd slide](#/1) ${fragmentIndex(2)}
        """
      }
    }
  }

  // Run the web server
  servePresentations()
}