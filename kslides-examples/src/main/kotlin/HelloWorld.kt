import com.kslides.*
import kotlinx.css.*
import kotlinx.html.*

fun main() {

  kslides {

    output {
      // Writes the presentation to a file
      enableFileSystem = true
      // Serves up the presentation via HTTP
      enableHttp = true
    }

    presentation {
      // Make this presentation available at helloworld.html
      path = "helloworld.html"

      // css styles can be specified as a string or with the kotlin css DSL
      css +=
        """
        .htmlslide h2 {
          color: yellow;
        }
        """

      css {
        rule("#mdslide h2") {
          color = Color.green
        }
      }

      // presentationConfig values are the default values for all slides in a presentation
      presentationConfig {
        transition = Transition.FADE

        // slideConfig values override the presentationDefault slideConfig values
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

      // Two vertical slides
      verticalSlides {
        // Slide that uses HTML for content
        htmlSlide {
          classes = "htmlslide"

          // slideConfig values override the presentationConfig slideConfig values
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