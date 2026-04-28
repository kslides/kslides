@file:Suppress("unused", "PackageDirectoryMismatch")

package website

import com.kslides.kslides

// --8<-- [start:basic]
fun markdownBasic() {
  kslides {
    presentation {
      markdownSlide {
        content {
          """
          # Markdown slide
          Use a triple-quoted string.
          """
        }
      }
    }
  }
}
// --8<-- [end:basic]

// --8<-- [start:bullets]
fun markdownBullets() {
  kslides {
    presentation {
      markdownSlide {
        content {
          """
          # Animated bullets
          - First <!-- .element: class="fragment" -->
          - Second <!-- .element: class="fragment" -->
          - Third <!-- .element: class="fragment" -->
          """
        }
      }
    }
  }
}
// --8<-- [end:bullets]

// --8<-- [start:notes]
fun markdownWithNotes() {
  kslides {
    presentation {
      markdownSlide {
        content {
          """
          # Slide with speaker notes
          Press `S` during the presentation.

          Note: These speaker notes only show in the speaker view.
          """
        }
      }
    }
  }
}
// --8<-- [end:notes]

// --8<-- [start:id]
fun markdownWithId() {
  kslides {
    presentation {
      markdownSlide {
        id = "intro"
        content {
          """
          # The intro slide
          Linkable as `#/intro`.
          """
        }
      }
    }
  }
}
// --8<-- [end:id]
