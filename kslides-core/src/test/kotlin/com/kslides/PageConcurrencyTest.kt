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
      val expected = generatePage(p, true)

      // Render the same presentation from many threads at once. Rendering mutates shared per-render
      // state (slideCount and each verticalSlides{} stack's reconstructed child list), so without
      // renderLock these interleave and produce corrupted/varying output or a
      // ConcurrentModificationException. With the lock, every result equals the serial render.
      val results =
        (1..200)
          .toList()
          .parallelStream()
          .map { generatePage(p, true) }
          .toList()

      results.forEach { it shouldBe expected }
    }
  }
}
