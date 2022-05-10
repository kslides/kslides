package com.github.readingbat

import com.kslides.*
import com.kslides.Page.generatePage
import io.kotest.assertions.throwables.*
import io.kotest.core.spec.style.*
import io.kotest.matchers.*

class PresentationTest : StringSpec(
  {
    "Simple presentation tests" {

      kslidesTest {
        presentation {
          dslSlide {
            content {}
          }
        }

        presentationBlocks.size shouldBe 1

        presentation {
          path = "test"
          dslSlide {
            content {}
          }
        }

        presentationBlocks.size shouldBe 2
      }
    }

    "Simple presentation tests2" {

      shouldThrowExactly<IllegalArgumentException> {
        kslidesTest {
          presentation {}
          presentation {}
        }
      }
    }

    "Simple presentation tests3" {

      shouldThrowExactly<IllegalArgumentException> {
        kslidesTest {
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
        kslidesTest {
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
      KSlides().kslidesConfig.httpStaticRoots.forEach {
        shouldThrowExactly<IllegalArgumentException> {
          kslidesTest {
            presentation {
              path = it.dirname
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
        kslidesTest {
        }
      }
    }

    "Missing slide test" {
      shouldThrowExactly<IllegalArgumentException> {
        kslidesTest {
          presentation {}
        }
      }
    }

    "Missing markdownSlide content test" {
      shouldThrowExactly<IllegalArgumentException> {
        kslidesTest {
          presentation { markdownSlide { } }
        }.presentations.forEach { p ->
          generatePage(p)
        }
      }
    }

    "Missing htmlSlide content test" {
      shouldThrowExactly<IllegalArgumentException> {
        kslidesTest {
          presentation { htmlSlide { } }
        }.presentations.forEach { p ->
          generatePage(p)
        }
      }
    }

    "Missing dslSlide content test" {
      shouldThrowExactly<IllegalArgumentException> {
        kslidesTest {
          presentation { dslSlide { } }
        }.presentations.forEach { p ->
          generatePage(p)
        }
      }
    }

    "Default Css Test 1" {
      val kslides =
        kslidesTest {
          presentation {
            dslSlide { content { } }
          }
        }.apply {
          presentations.forEach { p ->
            generatePage(p)
          }
        }

      kslides.css.toString() shouldBe ""
      kslides.presentation("/").css.toString() shouldBe ""
    }

    "Default Css Test 2" {
      val kslides =
        kslidesTest {
          presentation {
            css += "aaa"
            dslSlide { content { } }
          }
        }.apply {
          presentations.forEach { p ->
            generatePage(p)
          }
        }

      kslides.css.toString() shouldBe ""
      kslides.presentation("/").css.toString().trim() shouldBe "aaa"
    }

    "Default Css Test 3" {
      val kslides =
        kslidesTest {
          css += "aaa"
          presentation {
            dslSlide { content { } }
          }
        }.apply {
          presentations.forEach { p ->
            generatePage(p)
          }
        }

      kslides.css.toString().trim() shouldBe "aaa"
      kslides.presentation("/").css.toString().trim() shouldBe "aaa"
    }

    "Default Css Test 4" {
      val kslides =
        kslidesTest {
          css += "aaa"
          presentation {
            css += "bbb"
            dslSlide {  content { } }
          }
        }.apply {
          presentations.forEach { p ->
            generatePage(p)
          }
        }

      kslides.css.toString().trim() shouldBe "aaa"
      kslides.presentation("/").css.toString().trim() shouldBe "aaa\n\nbbb"
    }

    "Css Test 1" {
      val kslides =
        shouldThrowExactly<IllegalArgumentException> {
          kslidesTest {
            presentation {
              dslSlide {
                css += "bbb"; content { }
              }
            }
          }.apply {
            presentations.forEach { p ->
              generatePage(p)
            }
          }
        }
    }

    "Css Test 2" {
      val kslides =
        shouldThrowExactly<IllegalArgumentException> {
          kslidesTest {
            presentation {
              verticalSlides {
                css += "bbb"
                dslSlide {
                  content { }
                }
              }
            }
          }.apply {
            presentations.forEach { p ->
              generatePage(p)
            }
          }
        }
    }

    "Vertical Slides Test" {
      val kslides =
        shouldThrowExactly<IllegalArgumentException> {
          kslidesTest {
            presentation {
              verticalSlides {
              }
            }
          }.apply {
            presentations.forEach { p ->
              generatePage(p)
            }
          }
        }
    }
  })