package com.kslides

import com.kslides.Page.generatePage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.html.h2

class PageConcurrencyTest : StringSpec() {
  init {
    "generatePage renders consistently under concurrent access" {
      val kslides =
        kslidesTest {
          presentation {
            dslSlide { content { h2 { +"Top" } } }
            verticalSlides {
              dslSlide { content { h2 { +"V1" } } }
              dslSlide { content { h2 { +"V2" } } }
              markdownSlide { content { "# V3" } }
            }
          }
        }
      val p = kslides.presentation("/")
      val expected = generatePage(p)

      // Render the same presentation from many threads at once. Without renderLock these interleave
      // and corrupt output (or throw ConcurrentModificationException from a vertical stack's
      // reconstructed child list); with the lock every render equals the serial one.
      (1..64).toList().parallelStream().forEach { generatePage(p) shouldBe expected }
    }
  }
}
