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

// --8<-- [start:code-global]
fun globalCodeFont() {
  kslides {
    // Seeded into every presentation. reveal.js sizes code at 0.55em by default; lower
    // ".reveal pre" so long lines fit the slide, or raise it to enlarge all code blocks.
    css +=
      """
      .reveal pre { font-size: 0.4em; }
      """
    presentation {
      markdownSlide {
        content {
          """
          ```kotlin
          val answer = 42
          ```
          """
        }
      }
    }
  }
}
// --8<-- [end:code-global]

// --8<-- [start:code-slide]
fun perSlideCodeFont() {
  kslides {
    presentation {
      // "#bigcode pre" is an id selector, so it outranks the global ".reveal pre" rule and
      // only this slide's code is enlarged. No !important needed.
      css +=
        """
        #bigcode pre { font-size: 0.7em; }
        """
      markdownSlide {
        id = "bigcode"
        content {
          """
          ```kotlin
          val answer = 42
          ```
          """
        }
      }
    }
  }
}
// --8<-- [end:code-slide]

// --8<-- [start:code-shared]
fun sharedCodeFont() {
  kslides {
    // Shrink all code globally...
    css +=
      """
      .reveal pre { font-size: 0.4em; }
      """
    presentation {
      // ...then enlarge it on a subset of slides. An id must be unique, but a class can be reused.
      // ".reveal .big pre" has two classes, so it outranks the global ".reveal pre" on specificity
      // and wins no matter which rule is declared first.
      css +=
        """
        .reveal .big pre { font-size: 0.7em; }
        """
      markdownSlide {
        classes = "big"
        content {
          """
          ```kotlin
          val first = 1
          ```
          """
        }
      }
      markdownSlide {
        classes = "big"
        content {
          """
          ```kotlin
          val second = 2
          ```
          """
        }
      }
    }
  }
}
// --8<-- [end:code-shared]
