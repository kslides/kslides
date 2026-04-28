@file:Suppress("unused", "PackageDirectoryMismatch")

package website

import com.kslides.kslides
import kotlinx.css.Color
import kotlinx.css.FontWeight
import kotlinx.css.color
import kotlinx.css.fontWeight

// --8<-- [start:string]
fun stringCss() {
  kslides {
    presentation {
      css +=
        """
        h1 { color: gold; }
        .slides { font-family: "Inter", sans-serif; }
        """
      markdownSlide { content { "# Gold heading" } }
    }
  }
}
// --8<-- [end:string]

// --8<-- [start:dsl]
fun dslCss() {
  kslides {
    presentation {
      css {
        rule("h1") {
          color = Color.deepPink
          fontWeight = FontWeight.bold
        }
      }
      markdownSlide { content { "# Pink heading" } }
    }
  }
}
// --8<-- [end:dsl]
