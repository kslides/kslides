@file:Suppress("unused", "PackageDirectoryMismatch")

package website

import com.kslides.buildKSlides
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

// --8<-- [start:devmode]
fun devModeOutput() {
  kslides {
    output {
      // Live-reload dev server: the page refreshes to the current slide when the app restarts.
      enableHttp = true
      devMode = true
      enableFileSystem = false // no need to write static files while authoring
    }

    presentation {
      path = "index.html"
      markdownSlide { content { "# Editing live" } }
    }
  }
}
// --8<-- [end:devmode]

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

// --8<-- [start:programmatic]
fun driveKslidesFromCode() {
  val deck =
    buildKSlides {
      presentation {
        markdownSlide { content { "# Hello" } }
      }
    }

  // port 0 picks a free one; the handle is AutoCloseable
  deck.startHttpServer(port = 0).use { server ->
    deck.presentationPaths.forEach { path ->
      println("http://localhost:${server.port}$path")
    }
  }
}
// --8<-- [end:programmatic]
