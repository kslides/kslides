@file:Suppress("unused", "PackageDirectoryMismatch")

package website

import com.kslides.include
import com.kslides.kslides

// --8<-- [start:file]
fun includeFromFile() {
  kslides {
    presentation {
      markdownSlide {
        content {
          include("src/main/resources/slides/intro.md")
        }
      }
    }
  }
}
// --8<-- [end:file]

// --8<-- [start:url]
fun includeFromUrl() {
  kslides {
    presentation {
      markdownSlide {
        content {
          include("https://raw.githubusercontent.com/kslides/kslides/master/README.md")
        }
      }
    }
  }
}
// --8<-- [end:url]
