import com.kslides.Presentation
import com.kslides.PresentationTheme
import com.kslides.VerticalSlidesContext
import com.kslides.kslides
import kotlinx.html.*

fun main() {
  kslides {
    output {
      enableFileSystem = false
      enableHttp = true
    }

    presentation {
      presentationConfig {
        theme = PresentationTheme.SKY
      }

      dslSlide {
        content { h1 { +"Horizontal 1" } }
      }

      nestedHorizontalContent()

      verticalSlides {
        dslSlide {
          content { h1 { +"Vertical 1" } }
        }

        nestedVerticalContent(this)
      }
    }
  }
}

fun Presentation.nestedHorizontalContent() {
  dslSlide {
    content { h1 { +"Horizontal 2" } }
  }
}

// Notice that vertical slides require a VerticalSlidesContext context.
fun Presentation.nestedVerticalContent(verticalSlidesContext: VerticalSlidesContext) {
  verticalSlidesContext.dslSlide {
    content { h1 { +"Vertical 2" } }
  }
}