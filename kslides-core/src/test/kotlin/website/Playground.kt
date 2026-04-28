@file:Suppress("unused", "PackageDirectoryMismatch")

package website

import com.kslides.PlaygroundTheme
import com.kslides.kslides
import com.kslides.playground

// --8<-- [start:basic]
fun playgroundBasic() {
  kslides {
    presentation {
      dslSlide {
        content {
          // Path or URL to a Kotlin source file shown in the editor.
          playground("src/main/kotlin/playground/HelloWorld.kt")
        }
      }
    }
  }
}
// --8<-- [end:basic]

// --8<-- [start:configured]
fun playgroundConfigured() {
  kslides {
    presentation {
      dslSlide {
        content {
          playground("src/main/kotlin/playground/HelloWorld.kt") {
            theme = PlaygroundTheme.DARCULA
            dataHighlightOnly = false
            from = 1
            to = 5
          }
        }
      }
    }
  }
}
// --8<-- [end:configured]

// --8<-- [start:url]
fun playgroundFromUrl() {
  kslides {
    presentation {
      dslSlide {
        content {
          playground(
            "https://raw.githubusercontent.com/kslides/kslides/master/kslides-examples/src/main/kotlin/playground/HelloWorld.kt",
          )
        }
      }
    }
  }
}
// --8<-- [end:url]
