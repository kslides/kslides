@file:Suppress("unused", "PackageDirectoryMismatch")

package website

import com.kslides.Speed
import com.kslides.Transition
import com.kslides.kslides

// --8<-- [start:cascade]
fun configCascade() {
  kslides {
    // 1. Global defaults — apply to every presentation
    presentationConfig {
      transition = Transition.SLIDE
      history = true
    }

    presentation {
      // 2. Presentation-level overrides
      presentationConfig {
        transition = Transition.FADE

        slideConfig {
          backgroundColor = "#1f1f1f"
        }
      }

      markdownSlide {
        // 3. Slide-level overrides
        slideConfig {
          backgroundColor = "#2A9EEE"
          transition = Transition.ZOOM
        }
        content { "# Cascading config" }
      }
    }
  }
}
// --8<-- [end:cascade]

// --8<-- [start:transitions]
fun transitionsExample() {
  kslides {
    presentation {
      presentationConfig {
        transition = Transition.CONVEX
        transitionSpeed = Speed.FAST
      }
      markdownSlide { content { "# Convex transition" } }
      markdownSlide { content { "# Same transition applies here" } }
    }
  }
}
// --8<-- [end:transitions]

// --8<-- [start:href]
fun navHrefs() {
  kslides {
    presentation {
      presentationConfig {
        topLeftHref = "https://github.com/kslides/kslides"
        topRightHref = "" // disable the top-right link
      }
      markdownSlide { content { "# Navigation links" } }
    }
  }
}
// --8<-- [end:href]
