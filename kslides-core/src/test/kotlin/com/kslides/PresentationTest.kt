package com.kslides

import com.kslides.Page.generatePage
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class PresentationTest : StringSpec() {
  init {
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
      kslides
        .presentation("/")
        .css
        .toString()
        .trim() shouldBe "aaa"
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
      kslides
        .presentation("/")
        .css
        .toString()
        .trim() shouldBe "aaa"
    }

    "Default Css Test 4" {
      val kslides =
        kslidesTest {
          css += "aaa"
          presentation {
            css += "bbb"
            dslSlide { content { } }
          }
        }.apply {
          presentations.forEach { p ->
            generatePage(p)
          }
        }

      kslides.css.toString().trim() shouldBe "aaa"
      kslides
        .presentation("/")
        .css
        .toString()
        .trim() shouldBe "aaa\n\nbbb"
    }

    "Css Test 1" {
      shouldThrowExactly<IllegalArgumentException> {
        kslidesTest {
          presentation {
            dslSlide {
              css += "bbb"
              content { }
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
      val ex =
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
      // require(...) now returns the message string (no manual throw); verify it reaches the caller.
      ex.message shouldContain "verticalSlides{} block requires one or more slides"
    }

    "Google Analytics loader uses the configured property id, not a hardcoded one" {
      val kslides =
        kslidesTest {
          presentation {
            presentationConfig { gaPropertyId = "G-TESTID123" }
            dslSlide { content { } }
          }
        }

      val html = generatePage(kslides.presentation("/"))
      html shouldContain "googletagmanager.com/gtag/js?id=G-TESTID123"
      html shouldContain "gtag('config', 'G-TESTID123')"
      // The maintainer's id must never leak into a consumer's generated deck.
      html shouldNotContain "G-Z6YBNZS12K"
    }

    "data-visibility: hidden wins when both hidden and uncounted are set" {
      val kslides =
        kslidesTest {
          presentation {
            dslSlide {
              hidden = true
              uncounted = true
              content { }
            }
          }
        }

      val html = generatePage(kslides.presentation("/"))
      html shouldContain """data-visibility="hidden""""
      html shouldNotContain """data-visibility="uncounted""""
    }

    "data-visibility: hidden-only emits hidden" {
      val kslides =
        kslidesTest {
          presentation {
            dslSlide {
              hidden = true
              content { }
            }
          }
        }
      generatePage(kslides.presentation("/")) shouldContain """data-visibility="hidden""""
    }

    "data-visibility: uncounted-only emits uncounted" {
      val kslides =
        kslidesTest {
          presentation {
            dslSlide {
              uncounted = true
              content { }
            }
          }
        }
      generatePage(kslides.presentation("/")) shouldContain """data-visibility="uncounted""""
    }

    "generatePage is idempotent across repeated renders, including vertical stacks" {
      val kslides =
        kslidesTest {
          presentation {
            dslSlide { content { } }
            verticalSlides {
              dslSlide { content { } }
              dslSlide { content { } }
            }
          }
        }
      val p = kslides.presentation("/")

      // Vertical-stack children are reconstructed on each render and draw ids from a shared counter
      // that generatePage resets; rendering must therefore be a pure function of the built deck.
      generatePage(p, false) shouldBe generatePage(p, false)
      generatePage(p, true) shouldBe generatePage(p, true)
    }
  }
}
