package com.github.readingbat

import com.kslides.*
import com.kslides.KSlides.Companion.topLevel
import com.kslides.Page.generatePage
import io.kotest.assertions.throwables.*
import io.kotest.core.spec.style.*
import io.kotest.matchers.*

class PresentationTest : StringSpec(
  {
    "Simple presentation tests" {

      kslides {

        presentation {
          dslSlide { }
        }

        presentationBlocks.size shouldBe 1

        presentation {
          path = "test"
          dslSlide { }
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

    "Missing presentation test" {
      shouldThrowExactly<IllegalArgumentException> {
        kslides {}
      }
    }

    "Missing slide test" {
      shouldThrowExactly<IllegalArgumentException> {
        kslides {
          presentation {}
        }
      }
    }

    "Missing markdownSlide content test" {
      shouldThrowExactly<IllegalArgumentException> {
        kslides {
          presentation { markdownSlide { } }
        }.presentationMap.forEach { _, p ->
          generatePage(p)
        }
      }
    }

    "Missing htmlSlide content test" {
      shouldThrowExactly<IllegalArgumentException> {
        kslides {
          presentation { htmlSlide { } }
        }.presentationMap.forEach { _, p ->
          generatePage(p)
        }
      }
    }

    "Missing dslSlide content test" {
      shouldThrowExactly<IllegalArgumentException> {
        kslides {
          presentation { dslSlide { } }
        }.presentationMap.forEach { _, p ->
          generatePage(p)
        }
      }
    }

    "Default Css Test 1" {
      val kslides =
        kslides {
          presentation {
            dslSlide { content { } }
          }
        }.apply {
          presentationMap.forEach { _, p ->
            generatePage(p)
          }
        }

      kslides.css.toString() shouldBe ""
      kslides.presentationMap["/"]!!.css.toString() shouldBe ""
    }

    "Default Css Test 2" {
      val kslides =
        kslides {
          presentation {
            dslSlide { css += "aaa"; content { } }
          }
        }.apply {
          presentationMap.forEach { _, p ->
            generatePage(p)
          }
        }

      kslides.css.toString() shouldBe ""
      kslides.presentationMap["/"]!!.css.toString() shouldBe "aaa"
    }


    "Default Css Test 3" {
      val kslides =
        kslides {
          css += "aaa"
          presentation {
            dslSlide { content { } }
          }
        }.apply {
          presentationMap.forEach { _, p ->
            generatePage(p)
          }
        }

      kslides.css.toString() shouldBe "aaa"
      kslides.presentationMap["/"]!!.css.toString() shouldBe "aaa\n"
    }


    "Default Css Test 4" {
      val kslides =
        kslides {
          css += "aaa"
          presentation {
            dslSlide { css += "bbb"; content { } }
          }
        }.apply {
          presentationMap.forEach { _, p ->
            generatePage(p)
          }
        }

      kslides.css.toString() shouldBe "aaa"
      kslides.presentationMap["/"]!!.css.toString() shouldBe "aaa\nbbb"
    }
  })