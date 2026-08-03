@file:Suppress("unused", "PackageDirectoryMismatch")

package website

import com.kslides.kslides

// --8<-- [start:follow-along]
fun followAlongPresenting() {
  kslides {
    output {
      enableHttp = true
      followAlong = true // presenter URLs (with the token) are logged at startup
      // presenterToken = "my-talk" // optional: fixed token instead of a random one per launch
    }

    presentation {
      markdownSlide { content { "# Welcome" } }
    }
  }
}
// --8<-- [end:follow-along]
