@file:Suppress("unused", "PackageDirectoryMismatch")

package website

import com.kslides.kslides
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.ul

// --8<-- [start:basic]
fun dslBasic() {
  kslides {
    presentation {
      dslSlide {
        content {
          h1 { +"Kotlin HTML DSL" }
          p { +"Build slides with kotlinx.html." }
        }
      }
    }
  }
}
// --8<-- [end:basic]

// --8<-- [start:loop]
fun dslLoop() {
  val items = listOf("Type-safe", "Refactorable", "Composable")
  kslides {
    presentation {
      dslSlide {
        content {
          h2 { +"Why DSL slides?" }
          ul {
            items.forEach { li { +it } }
          }
        }
      }
    }
  }
}
// --8<-- [end:loop]
