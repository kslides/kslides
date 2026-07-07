@file:Suppress("unused", "PackageDirectoryMismatch")

package website

import com.kslides.include
import com.kslides.kslides
import com.kslides.mermaid

// --8<-- [start:basic]
fun mermaidSlide() {
  kslides {
    presentation {
      dslSlide {
        content {
          mermaid(
            """
            sequenceDiagram
              Browser->>Ktor: GET /slides
              Ktor->>kslides: render()
              kslides-->>Browser: HTML
            """,
          )
        }
      }
    }
  }
}
// --8<-- [end:basic]

// --8<-- [start:include]
fun mermaidFromFile() {
  kslides {
    presentation {
      dslSlide {
        content {
          mermaid(include("src/main/resources/diagrams/pipeline.mmd"))
        }
      }
    }
  }
}
// --8<-- [end:include]
