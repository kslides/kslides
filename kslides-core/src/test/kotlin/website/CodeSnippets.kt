@file:Suppress("unused", "PackageDirectoryMismatch")

package website

import com.kslides.codeSnippet
import com.kslides.include
import com.kslides.kslides

// --8<-- [start:inline]
fun codeSnippetInline() {
  kslides {
    presentation {
      dslSlide {
        content {
          codeSnippet {
            language = "kotlin"
            highlightPattern = "1-3"
            code =
              $$"""
              fun greet(name: String) {
                println("Hello, $name")
              }
              """.trimIndent()
          }
        }
      }
    }
  }
}
// --8<-- [end:inline]

// --8<-- [start:from-url]
fun codeSnippetFromUrl() {
  kslides {
    presentation {
      dslSlide {
        content {
          codeSnippet {
            language = "kotlin"
            highlightPattern = "1|3-5"
            code = include(
              "https://raw.githubusercontent.com/kslides/kslides/master/kslides-examples/src/main/kotlin/playground/HelloWorld.kt",
            )
          }
        }
      }
    }
  }
}
// --8<-- [end:from-url]
