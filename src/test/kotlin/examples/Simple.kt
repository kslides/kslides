package examples

import com.github.pambrose.*
import com.github.pambrose.Presentation.Companion.present
import com.github.pambrose.SlideConfig.Companion.slideConfig
import com.github.pambrose.Speed.Slow
import com.github.pambrose.Transition.Slide
import com.github.pambrose.Transition.Zoom
import kotlinx.html.*

fun main() {
  presentation {
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

    markdownSlide(slideConfig { transition(Zoom, Slow) }) {
      """
      # Markdown Slide 🍒 
      
      Press ESC to see presentation overview.
      """.trimIndent()
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

    config {
      history = true
      transition = Slide
      transitionSpeed = Slow
    }
  }

  // Run the web server
  present()
}