
package com.github.readingbat

import com.kslides.*
import com.kslides.KSlides.Companion.topLevel
import io.kotest.assertions.throwables.*
import io.kotest.core.spec.style.*
import io.kotest.matchers.*

class PresentationTest : StringSpec(
  {
    "Simple presentation tests" {

      kslides {

        presentation {}

        presentationBlocks.size shouldBe 1

        presentation {
          path = "test"
        }

        presentationBlocks.size shouldBe 2
      }
    }

    "Simple presentation tests2" {

      shouldThrowExactly<IllegalArgumentException> {
        kslides {
          presentation {}
          presentation {}
        }
      }
    }

    "Simple presentation tests3" {

      shouldThrowExactly<IllegalArgumentException> {
        kslides {
          presentation {
            path = "test"
          }
          presentation {
            path = "test"
          }
        }
      }
    }

    "Simple presentation tests4" {

      shouldThrowExactly<IllegalArgumentException> {
        kslides {
          presentation {
            path = "test"
          }
          presentation {
            path = "/test"
          }
        }
      }
    }

    "Simple presentation tests5" {
      topLevel.staticRoots.forEach {
        shouldThrowExactly<IllegalArgumentException> {
          kslides {
            presentation {
              path = it
            }

            presentation {
              path = "/it"
            }
          }
        }
      }
    }
  })