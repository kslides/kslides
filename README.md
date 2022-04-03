# kslides

[![Release](https://jitpack.io/v/pambrose/kslides.svg)](https://jitpack.io/#pambrose/kslides)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/701fc37a847042d2ae2cd6e80075ff6f)](https://www.codacy.com/gh/pambrose/kslides/dashboard?amp;utm_medium=referral&amp;utm_content=pambrose/kslides&amp;utm_campaign=Badge_Grade)
[![Build Status](https://travis-ci.org/pambrose/kslides.svg?branch=master)](https://travis-ci.org/pambrose/kslides)
[![Kotlin version](https://img.shields.io/badge/kotlin-1.6.20-red?logo=kotlin)](http://kotlinlang.org)

**kslides** is a [Kotlin](https://kotlinlang.org) DSL for the incredible [reveal.js](https://revealjs.com) 
presentation framework. It is meant for people who prefer working with an IDE rather than PowerPoint. 
It works particularly well for presentations with code snippets and slides
authored in Markdown/HTML.

[![kslides screenshot](https://pambrose.github.io/kslides/img/kslides-screenshot.png)](https://pambrose.github.io/kslides/)

[This](kslides-examples/src/main/kotlin/KSlides.kt) presentation is served statically from
[Netlify](https://kslides.netlify.app)
and [Github Pages](https://pambrose.github.io/kslides/).
It is also running dynamically on [Heroku](https://kslides-repo.herokuapp.com).

## Getting Started

[![Fork](https://img.shields.io/endpoint?color=%232A9EEE&logo=github&style=flat&url=https%3A%2F%2Fraw.githubusercontent.com%2Fpambrose%2Fkslides%2Fmaster%2Fdocs%2Fshields%2Ffork.json)](https://github.com/pambrose/kslides-template/fork)
[![Template](https://img.shields.io/endpoint?color=%232A9EEE&logo=github&style=flat&url=https%3A%2F%2Fraw.githubusercontent.com%2Fpambrose%2Fkslides%2Fmaster%2Fdocs%2Fshields%2Ftemplate.json)](https://github.com/pambrose/kslides-template/generate)

To create a kslides presentation, you can either [fork](https://github.com/pambrose/kslides-template/fork) 
the [kslides-template](https://github.com/pambrose/kslides-template) repo and assign it a new name, 
or generate a new repository using [kslides-template](https://github.com/pambrose/kslides-template)
as a [template](https://github.com/pambrose/kslides-template/generate). The advantage of forking is you 
will be able to pull upstream changes and stay current with kslides-template updates. Github provides more 
details on [templates](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-repository-from-a-template)
and [forks](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/working-with-forks/about-forks).


### Presentation Structure

A presentation is created using a [Kotlin DSL](https://medium.com/adobetech/building-elegant-dsls-with-kotlin-707726c5ed21). 
It requires a minimal knowledge of Kotlin to use. 

```kotlin
kslides {

  output {
    enableHttp = true
  }

  presentation {
    css += """
      #intro h1 { color: #FF5533; }
      #mdslide p { color: black; }
    """

    presentationConfig {
      githubCornerHref = githubSourceUrl("pambrose", "kslides", "kslides-examples/src/main/kotlin/Slides.kt")
      githubCornerTitle = "View presentation source on Github"
      slideNumber = "c/t"
      history = true
      transition = Transition.SLIDE
      transitionSpeed = Speed.SLOW
      gaPropertyId = "G-TRY2Q243XC"
      enableSpeakerNotes = true
      enableMenu = true

      slideConfig {
        backgroundColor = "#4370A5"
      }
    }

    markdownSlide {
      id = "intro"

      slideConfig {
        transition = Transition.ZOOM
      }

      content {
        """
        # kslides
        ### A Kotlin DSL wrapper for [reveal.js](https://revealjs.com)
        """
      }
    }

    markdownSlide {
      id = "mdslide"

      slideConfig {
        transition = Transition.ZOOM
      }

      content {
        """
        # Markdown Slide
        ## 🍒
        
        Use the arrow keys to navigate ${fragmentIndex(1)}
        
        Press ESC to see presentation overview ${fragmentIndex(2)}
        
        Press S to see speaker notes ${fragmentIndex(3)}

        """
      }
    }

    htmlSlide {
      content {
        """
        <h1>HTML Slide</h1>
        <h2>🐦</h2>
        <p>Use the arrow keys to navigate</p>
        <aside class="notes">
          These are notes for the htmlSlide 📝
        </aside>
        """
      }
    }

    dslSlide {
      slideConfig {
        transition = Transition.ZOOM
      }

      content {
        h1 { +"DSL Slide" }
        h2 { +"👀" }
        p { +"Use the arrow keys to navigate" }
        aside("notes") {
          +"These are notes for the dslSlide ⚾"
        }
      }
    }

    markdownSlide {
      content {
        """
        ## Code Highlights    
        ```kotlin [3,7|4,6|5]
        ${includeFile("kslides-examples/src/main/kotlin/examples/HelloWorldK.kt")}
        ```
        """
      }
    }

    verticalSlides {
      // We use a for loop here to generate a series of slides, each with a different set of lines
      // We use the same syntax used by revealjs: https://revealjs.com/code/
      for (lines in lineNumbers("[5,6,9|5-9|]")) {
        htmlSlide {
          autoAnimate = true
          content {
            """
            <h2>Animated Code 👇</h2>  
            <pre data-id="code-animation" data-cc="false"> 
              <code data-trim="" data-line-numbers="">
                ${includeFile("kslides-examples/src/main/kotlin/examples/assign.js", lines)}
              </code>
            </pre>
            """
          }
        }
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
        ## Presentation Definition    
        ```kotlin []
        ${
          includeFile(
            "kslides-examples/src/main/kotlin/Slides.kt",
            beginToken = "readme begin",
            endToken = "readme end"
          )
        }
        ```
        """
      }
    }
  }
}
```




## Misc Notes

### Local Development
* Speaker Notes do not work properly when running locally.

### Heroku 

* Add a Config Var: `GRADLE_TASK=-Pprod=true uberjar`

### MarkDown Slide 

* When a `markdownSlide` is in a `verticalSlides` section and references an external file, the string "---"
  is interpreted as a vertical page separator and "--- " (with a space suffix) is rendered as a markdown horizontal
  line.
* 
## Third Party Plugins

* https://github.com/Martinomagnifico/reveal.js-copycode
* https://github.com/denehyg/reveal.js-menu

