@file:Suppress("unused", "PackageDirectoryMismatch")

package website

import com.kslides.kslides
import kotlinx.html.h1

// --8<-- [start:basic]
fun verticalBasic() {
  kslides {
    presentation {
      markdownSlide {
        content { "# Topic" }
      }

      verticalSlides {
        markdownSlide {
          content { "## Detail 1" }
        }
        markdownSlide {
          content { "## Detail 2" }
        }
        dslSlide {
          content { h1 { +"Wrap-up" } }
        }
      }
    }
  }
}
// --8<-- [end:basic]
