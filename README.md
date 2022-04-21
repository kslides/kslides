# kslides

[![Release](https://jitpack.io/v/kslides/kslides.svg)](https://jitpack.io/#kslides/kslides)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/701fc37a847042d2ae2cd6e80075ff6f)](https://www.codacy.com/gh/kslides/kslides/dashboard?amp;utm_medium=referral&amp;utm_content=kslides/kslides&amp;utm_campaign=Badge_Grade)
[![Build Status](https://app.travis-ci.com/kslides/kslides.svg?branch=master)](https://app.travis-ci.com/kslides/kslides)
[![Kotlin version](https://img.shields.io/badge/kotlin-1.6.21-red?logo=kotlin)](http://kotlinlang.org)

**kslides** is a [Kotlin](https://kotlinlang.org) DSL for the incredible [reveal.js](https://revealjs.com) 
presentation framework. It is meant for people who prefer working with an IDE rather than PowerPoint. 
It works particularly well for presentations with code snippets and slides
authored in Markdown/HTML.

[![kslides screenshot](https://kslides.github.io/kslides/images/kslides-screenshot.png)](https://kslides.github.io/kslides/)

The [above](kslides-examples/src/main/kotlin/Slides.kt) presentation is served statically from
[Netlify](https://kslides.netlify.app)
and [GitHub Pages](https://kslides.github.io/kslides/).
It is also running dynamically on [Heroku](https://kslides-repo.herokuapp.com).

## Getting Started

[![Fork](https://img.shields.io/endpoint?color=%232A9EEE&logo=github&style=flat&url=https%3A%2F%2Fraw.githubusercontent.com%2Fkslides%2Fkslides%2Fmaster%2Fdocs%2Fshields%2Ffork.json)](https://github.com/kslides/kslides-template/fork)

To create a kslides presentation, you can either [fork](https://github.com/kslides/kslides-template/fork) 
the [kslides-template](https://github.com/kslides/kslides-template) repo and assign it a new name, 
or generate a new repository using [kslides-template](https://github.com/kslides/kslides-template)
as a [template](https://github.com/kslides/kslides-template/generate). The advantage of forking is you 
will be able to pull upstream changes and stay current with kslides-template updates. GitHub provides more 
details on [templates](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-repository-from-a-template)
and [forks](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/working-with-forks/about-forks).

The [kslides-template](https://github.com/kslides/kslides-template) repo describes how to generate slide content once
you have created your new kslides repo.

## Defining a Presentation

A presentation is created using a [Kotlin DSL](https://medium.com/adobetech/building-elegant-dsls-with-kotlin-707726c5ed21). 
Defining a presentation requires a minimal knowledge of Kotlin. 

The following _kslides_ definition can be seen [here](https://kslides.github.io/kslides/helloworld.html).

```kotlin
kslides {

  output {
    // Writes the presentation to a file
    enableFilSystem = true
    // Serves up the presentation via HTTP
    enableHttp = true
  }

  presentation {
    // Make this presentation available at helloworld.html
    path = "helloworld.html"

    // css styles can be specified as a string or with the kotlin css DSL
    css +=
      """
      .htmlslide h2 {
        color: yellow;
      }
      """

    css {
      rule("#mdslide h2") {
        color = Color.green
      }
    }

    // presentationConfig values are the default values for all slides in a presentation
    presentationConfig {
      transition = Transition.FADE

      // slideConfig values here override the presentationDefault slideConfig values
      slideConfig {
        backgroundColor = "#2A9EEE"
      }
    }

    // Slide that uses Markdown for content
    markdownSlide {
      id = "mdslide"

      content {
        """
        # Markdown
        ## Hello World
        """
      }
    }

    // Two vertical slides
    verticalSlides {
      // Slide that uses HTML for content
      htmlSlide {
        classes = "htmlslide"

        // slideConfig values here override the presentationConfig slideConfig values
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

      // Slide that uses the Kotlin HTML DSL for content
      dslSlide {
        content {
          h1 { +"DSL" }
          h2 { +"Hello World" }
        }
      }
    }
  }
}
```

## Sections

### kslides

A _kslides_ section contains:
* an _output_ section
* a _css_ section
* a _presentationDefault_ section
* and one or more _presentation_ sections

```kotlin
kslides {
  output {}                 
  css {}                    // Optional
  presentationDefault {}    // Optional
  presentation {}           // One or more presentations
}
```

### output

An _output_ section defines how slide content is made available.
This _output_ section would write slide content to html files in the _/docs_ directory and serve 
the slide content via HTTP on port 8080.

```kotlin
output {
  enableFileSystem = true  
  outputDir = "docs"
  
  enableHttp = true
  httpPort = 8080
}
```

### css

### presentationDefault

### presentation

Each presentation section contains:
* a _css_ section
* a _presentationConfig_ section
* and one or more _slide_ sections

```kotlin
presentation {
  css {}
  presentationConfig {
    menuConfig {}
    copyCodeConfig {}
    slideConfig {}
  }
  markdownSlide {
    slideConfig {}
    output {}
  }
  verticalSlides {
    htmlSlide {
      slideConfig {}
      output {}
    }
    dslSlide {
      slideConfig {}
      output {}
    }
  }
}
```

## Misc Notes

### Images
* Presentations served by HTTP pull static files from ```/src/main/resources/public```. whereas 
filesystem presentations pull from ```/docs```.

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

