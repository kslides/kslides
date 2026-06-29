package com.kslides

import com.kslides.Page.generatePage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldNotContain

/**
 * A blank diagram source / playground srcName must short-circuit before any network work and emit
 * no embed, rather than POSTing empty content to Kroki or rendering an empty iframe. These render
 * without touching the network precisely because the blank-source guard returns early.
 */
class BlankSourceGuardTest : StringSpec() {
  init {
    "diagram with a blank source is skipped — no kroki img, no exception" {
      val kslides =
        kslidesTest {
          presentation {
            dslSlide {
              content {
                diagram("plantuml") { source = "" }
              }
            }
          }
        }

      generatePage(kslides.presentation("/")) shouldNotContain "<img"
    }

    "playground with a blank srcName is skipped — no iframe, no exception" {
      val kslides =
        kslidesTest {
          presentation {
            dslSlide {
              content {
                playground("")
              }
            }
          }
        }

      generatePage(kslides.presentation("/")) shouldNotContain "<iframe"
    }
  }
}
