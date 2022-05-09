import com.kslides.*
import kotlinx.css.*
import kotlinx.html.*

fun main() {

  kslides {

    output {
      // Write the presentations to a file
      enableFileSystem = true
      // Serve up the presentations via HTTP
      enableHttp = true
    }

    // Default values for all presentations in this file
    presentationConfig {
    }

    presentation {
      // Make this presentation available at helloworld.html
      path = "helloworld.html"

      // Specify css styles  as a string
      css +=
        """
        .htmlslide h2 {
          color: yellow;
        }
        """
      // or with the kotlin css DSL
      css {
        rule("#mdslide h2") {
          color = Color.green
        }
      }

      // Default values for all slides in this presentation
      presentationConfig {
        transition = Transition.FADE
        topLeftHref = ""
        topRightHref = ""

        slideConfig {
          backgroundColor = "#2A9EEE"
        }
      }

      // Slide that uses Markdown for content
      markdownSlide {
        id = "mdslide"

        content {
          """
          # Markdown
          ## Hello World
          """
        }
      }

      // Vertical section with two slides
      verticalSlides {
        // Slide that uses HTML for content
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

        // Slide that uses the Kotlin HTML DSL for content
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