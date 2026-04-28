@file:Suppress("unused", "PackageDirectoryMismatch")

package website

import com.kslides.kslides

// --8<-- [start:basic]
fun htmlBasic() {
  kslides {
    presentation {
      htmlSlide {
        content {
          """
          <h1>Raw HTML</h1>
          <p>Anything reveal.js accepts as slide markup.</p>
          """
        }
      }
    }
  }
}
// --8<-- [end:basic]

// --8<-- [start:classes]
fun htmlWithClasses() {
  kslides {
    presentation {
      htmlSlide {
        classes = "intro-slide"
        content {
          """
          <h1>Styled via class</h1>
          <p>Add CSS rules targeting <code>.intro-slide</code>.</p>
          """
        }
      }
    }
  }
}
// --8<-- [end:classes]
