@file:Suppress("unused", "PackageDirectoryMismatch")

package website

import com.kslides.kslides

// --8<-- [start:filesystem]
fun staticOutput() {
  kslides {
    output {
      enableFileSystem = true
      enableHttp = false
      // outputDir defaults to "docs"
    }

    presentation {
      path = "index.html"
      markdownSlide { content { "# Static site" } }
    }
  }
}
// --8<-- [end:filesystem]

// --8<-- [start:http]
fun httpOutput() {
  kslides {
    output {
      enableFileSystem = false
      enableHttp = true
      httpPort = 8080
    }

    presentation {
      path = "index.html"
      markdownSlide { content { "# Live server" } }
    }
  }
}
// --8<-- [end:http]

// --8<-- [start:multi]
fun multiplePresentations() {
  kslides {
    presentation {
      path = "index.html"
      markdownSlide { content { "# Welcome" } }
    }

    presentation {
      path = "talks/2026.html"
      markdownSlide { content { "# 2026 talk" } }
    }
  }
}
// --8<-- [end:multi]
