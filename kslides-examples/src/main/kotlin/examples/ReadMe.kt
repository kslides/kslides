package examples

import com.kslides.*
import kotlinx.css.*
import kotlinx.html.*

fun main() {

  kslides {

    output {
      // Write the presentations to a file
      enableFileSystem = true
      // Also serve up the presentations via HTTP
      enableHttp = true
    }

    // Default values for all presentations
    presentationConfig {
    }

    presentation {
      // Make this presentation available at helloworld.html
      path = "helloworld.html"

      // Specify css styles as a string
      css +=
        """
        .htmlslide h2 {
          color: yellow;
        }
        """
      // or use the Kotlin CSS DSL
      css {
        rule("#mdslide h2") {
          color = Color.green
        }
      }

      // Config values for this presentation
      presentationConfig {
        transition = Transition.FADE
        topLeftHref = ""  // Turn off top left href
        topRightHref = "" // Turn off top right href

        // Default values for all slides in this presentation
        slideConfig {
          backgroundColor = "#2A9EEE"
        }
      }

      // Slide that uses Markdown content
      markdownSlide {
        id = "mdslide"

        content {
          """
          # Markdown
          ## Hello World
          """
        }
      }

      // Two vertical slides slides
      verticalSlides {
        // Slide that uses HTML content
        htmlSlide {
          classes = "htmlslide"

          // Slide-specific config values
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

        // Slide that uses Kotlin HTML DSL content
        dslSlide {
          content {
            h1 { +"DSL" }
            h2 { +"Hello World" }
          }
        }
      }
    }
  }
}