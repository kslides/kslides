// Examples shown on the kslides documentation site. Not test code.
@file:Suppress("unused", "PackageDirectoryMismatch")

package website

import com.kslides.kslides

// --8<-- [start:hello]
fun main() {
  kslides {
    presentation {
      markdownSlide {
        content {
          """
          # Hello, kslides!
          A Kotlin DSL for reveal.js.
          """
        }
      }
    }
  }
}
// --8<-- [end:hello]
