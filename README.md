# kslides

[![Release](https://jitpack.io/v/pambrose/kslides.svg)](https://jitpack.io/#pambrose/kslides)
[![Build Status](https://travis-ci.org/pambrose/kslides.svg?branch=master)](https://travis-ci.org/pambrose/kslides)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/701fc37a847042d2ae2cd6e80075ff6f)](https://www.codacy.com/gh/pambrose/kslides/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=pambrose/kslides&amp;utm_campaign=Badge_Grade)
[![Kotlin](https://img.shields.io/badge/%20language-Kotlin-red.svg)](https://kotlinlang.org/)

**kslides** is a Kotlin DSL wrapper for [reveal.js](https://revealjs.com). It is meant for people who would prefer to
build presentations with IntelliJ than Powerpoint. It works particularly well for presentations with code and slides
authored in Markdown/HTML.

## Example

[This presentation](src/main/kotlin/com/kslides/examples/Readme.kt) is running dynamically on 
[Heroku](https://kslides-readme.herokuapp.com), and statically from 
[Netlify](https://kslides-readme.netlify.app) 
and [Github Pages](https://pambrose.github.io/kslides/).

```kotlin
  kslides {

  output {
    enableFileSystem = true
    enableHttp = true
  }

  presentation {

    presentationConfig {
      history = true
      transition = Transition.SLIDE
      transitionSpeed = Speed.SLOW

      slideConfig {
        backgroundColor = "#4370A5"
      }
    }

    markdownSlide {
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
      slideConfig {
        transition = Transition.ZOOM
        transitionSpeed = Speed.FAST
      }

      content {
        h1 { +"DSL Slide" }
        h2 { +"👀" }
        p { +"Use the arrow keys to navigate" }
      }
    }

    markdownSlide {
      content {
        """
          ## Kotlin Code Highlights    
          ```kotlin [1|3,8|4|5-7]
          ${includeFile("src/test/kotlin/examples/HelloWorldK.kt")}
          ```
          """
      }
    }

    markdownSlide {
      slideConfig {
        backgroundColor = "lightblue"
      }

      content {
        """
          ## Java Code Highlights    
          ```java [1|3,7|4,6|5]
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
            +"Vertical DSL Slide 👇"
          }
        }
      }

      markdownSlide {
        content {
          """
            # Vertical Markdown Slide 🦊 
            
            [Go back to the 1st slide](#/) ${fragmentIndex(1)}
         
            [Go back to the 2nd slide](#/1) ${fragmentIndex(2)}
            
            """
        }
      }
    }

    markdownSlide {
      content {
        """
          ## Presentation Description    
          ```kotlin [9-12|16-24|26-39|1-132]
          ${includeUrl("https://raw.githubusercontent.com/pambrose/kslides/master/src/main/kotlin/com/kslides/examples/Readme.kt")}
          ```
          """
      }
    }

  }
}
```

## Third Party Plugins

* https://github.com/Martinomagnifico/reveal.js-copycode
* https://github.com/denehyg/reveal.js-menu


## Getting Started

[![Template](https://img.shields.io/badge/kslides-template-blue?logo=github)](https://github.com/pambrose/kslides-template/generate)

[Create a presentation repo](https://github.com/pambrose/kslides-template/generate) using
the [kslides-template](https://github.com/pambrose/kslides-template) repo.

## Heroku Notes

* Add a Config Var for `GRADLE_TASK=-Pprod=true uberjar`


## MarkDown Slide Notes

* When a `markdownSlide` is in a `verticalSlides` section and references an external file, the string "---"
  is interpreted as a vertical page separator and "--- " (with a space suffix) is rendered as a markdown horizontal
  line.

* https://stackoverflow.com/questions/49267811/how-can-i-escape-3-backticks-code-block-in-3-backticks-code-block
