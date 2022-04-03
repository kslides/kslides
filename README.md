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

[This](kslides-examples/src/main/kotlin/Slides.kt) presentation is served statically from
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
    // Makes this presentation available at helloworld.html
    path = "helloworld.html"

    // css styles can be specified as a string or with the kotlin css DSL
    css += """
      .htmlslide h2 {
        color: yellow;
      }
    """

    css {
      rule("#mdslide h2") {
        color = Color.green
      }
    }

    presentationConfig {
      transition = Transition.FADE

      slideConfig {
        backgroundColor = "#2A9EEE"
      }
    }

    markdownSlide {
      id = "mdslide"

      content {
        """
      # Markdown
      ## Hello World
      """
      }
    }

    htmlSlide {
      classes = "htmlslide"

      slideConfig {
        backgroundColor = "red"
      }

      content {
        """
      <h1>HTML</h1>
      <h2>Hello World</h2>
      """
      }
    }

    dslSlide {
      content {
        h1 { +"DSL" }
        h2 { +"Hello World" }
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
            beginToken = "helloworld begin",
            endToken = "helloworld end"
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

