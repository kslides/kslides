# kslides

[![Release](https://jitpack.io/v/pambrose/kslides.svg)](https://jitpack.io/#pambrose/kslides)
[![Build Status](https://travis-ci.org/pambrose/kslides.svg?branch=master)](https://travis-ci.org/pambrose/kslides)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/701fc37a847042d2ae2cd6e80075ff6f)](https://www.codacy.com/gh/pambrose/kslides/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=pambrose/kslides&amp;utm_campaign=Badge_Grade)
[![Kotlin](https://img.shields.io/badge/%20language-Kotlin-red.svg)](https://kotlinlang.org/)

**kslides** is a Kotlin DSL wrapper for [reveal.js](https://revealjs.com). It is meant for people who would prefer to
build presentations with IntelliJ than Powerpoint. It works particularly well for presentationing code and slides
authored in Markdown/HTML.

## Example

Click [here](https://kslides-readme.herokuapp.com) to see [this presentation](src/main/kotlin/Simple.kt) running.

```kotlin
fun main() {
  kslides {
    output {
      enableHttp = true
    }

    presentation {

      presentationConfig {
        history = true
        transition = Transition.SLIDE
        transitionSpeed = Speed.SLOW
      }

      markdownSlide {
        id = "start"

        slideConfig {
          transition = Transition.ZOOM
          transitionSpeed = Speed.FAST
        }

        content {
          """
          # Markdown Slide
          ## 🍒
          Press ESC to see presentation overview.
          """
        }
      }

      htmlSlide {
        content {
          """
          <h1>HTML Slide</h1>
          <h2>🐦</h2>
          <p>Use the arrow keys to navigate</p>
          """
        }
      }

      dslSlide {
        content {
          h1 { +"DSL Slide" }
          h2 { +"🐦" }
          p { +"Use the arrow keys to navigate" }
        }
      }


      markdownSlide {
        slideConfig {
          backgroundColor = "#4370A5"
        }

        content {
          """
          # Code Highlights    
          ```kotlin [1|2,5|3-4]
          ${includeFile("src/test/kotlin/examples/HelloWorldK.kt")}
          ```
          """
        }
      }

      markdownSlide {
        slideConfig {
          backgroundColor = "#DD70A5"
        }

        content {
          """
          # Code Highlights    
          ```java [1|2|3]
          ${includeFile("src/test/kotlin/examples/HelloWorldJ.java")}
          ```
          """
        }
      }

      verticalSlides {

        dslSlide {
          slideConfig {
            backgroundVideo = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
          }

          content {
            h1 {
              style = "color: red;"
              +"Vertical HTML Slide 👇"
            }
          }
        }

        markdownSlide {
          content {
            """
            # Vertical Markdown Slide 🦊 
            
            [Go back to the 1st slide](#/start) ${fragmentIndex(1)}
         
            [Go back to the 2nd slide](#/1) ${fragmentIndex(2)}
            
            """
          }
        }
      }
    }
  }
}
```

## Third Party Plugins

* https://github.com/Martinomagnifico/reveal.js-copycode
* https://github.com/denehyg/reveal.js-menu

## FAQ

* https://stackoverflow.com/questions/49267811/how-can-i-escape-3-backticks-code-block-in-3-backticks-code-block

## Getting Started

[![Template](https://img.shields.io/badge/kslides-template-blue?logo=github)](https://github.com/pambrose/kslides-template/generate)

[Create a presentation repo](https://github.com/pambrose/kslides-template/generate) using
the [kslides-template](https://github.com/pambrose/kslides-template) repo.

## Notes

* When a `markdownSlide` is in a `verticalSlides` section and references an external file, the string "---"
  is interpreted as a vertical page separator and "--- " (with a space suffix) is rendered as a markdown horizontal
  line. 
