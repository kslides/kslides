@file:Suppress("unused", "PackageDirectoryMismatch")

package website

import com.kslides.KSlides
import com.kslides.export.exportPdf
import com.kslides.kslides

// --8<-- [start:config]
fun pdfConfiguration() {
  kslides {
    output {
      enableFileSystem = true

      pdf {
        outputDir = "build/pdf"   // where the exported files land (default)
        previewPng = true         // also capture a first-slide PNG per deck
        browserChannel = "chrome" // use installed Chrome; skips the Chromium download
        exclude("scratch")        // leave a deck out of "export everything"
      }
    }

    presentation {
      markdownSlide { content { "# My Deck" } }
    }
  }
}
// --8<-- [end:config]

// --8<-- [start:export]
fun KSlides.myDeck() {
  presentation {
    markdownSlide { content { "# My Deck" } }
  }
}

fun exportAll() {
  exportPdf { myDeck() }         // every presentation -> one PDF each
}

fun exportOne() {
  exportPdf("demo") { myDeck() } // just the presentation at /demo.html
}
// --8<-- [end:export]
