@file:Suppress("unused", "PackageDirectoryMismatch")

package website

import com.kslides.DiagramOutputType
import com.kslides.diagram
import com.kslides.kslides

// --8<-- [start:plantuml]
fun plantumlDiagram() {
  kslides {
    presentation {
      dslSlide {
        content {
          diagram("plantuml") {
            outputType = DiagramOutputType.SVG
            source =
              """
              @startuml
              Alice -> Bob: Hello
              Bob --> Alice: Hi back
              @enduml
              """.trimIndent()
          }
        }
      }
    }
  }
}
// --8<-- [end:plantuml]

// --8<-- [start:mermaid]
fun mermaidDiagram() {
  kslides {
    presentation {
      dslSlide {
        content {
          diagram("mermaid") {
            source =
              """
              graph LR
                A[Author] --> B(kslides)
                B --> C{Output}
                C -->|HTML| D[Static site]
                C -->|HTTP| E[Live server]
              """.trimIndent()
          }
        }
      }
    }
  }
}
// --8<-- [end:mermaid]
