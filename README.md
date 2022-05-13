# kslides

[![Release](https://jitpack.io/v/kslides/kslides.svg)](https://jitpack.io/#kslides/kslides)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/701fc37a847042d2ae2cd6e80075ff6f)](https://www.codacy.com/gh/kslides/kslides/dashboard?amp;utm_medium=referral&amp;utm_content=kslides/kslides&amp;utm_campaign=Badge_Grade)
[![Build Status](https://app.travis-ci.com/kslides/kslides.svg?branch=master)](https://app.travis-ci.com/kslides/kslides)
[![Kotlin version](https://img.shields.io/badge/kotlin-1.6.21-red?logo=kotlin)](http://kotlinlang.org)

**kslides** is a [Kotlin](https://kotlinlang.org) DSL for the awesome [reveal.js](https://revealjs.com)
presentation framework. It is meant for people who prefer working with an IDE rather than PowerPoint.
It works particularly well for presentations with code snippets and animations. Slides are
authored in [Markdown](https://www.markdownguide.org), [HTML](https://www.w3schools.com/html/),
or the Kotlin [HTML DSL](https://github.com/Kotlin/kotlinx.html/wiki/Getting-started).

[![kslides screenshot](https://kslides.github.io/kslides/images/kslides-screenshot.png)](https://kslides.github.io/kslides/)

[This presentation](kslides-examples/src/main/kotlin/Slides.kt) is served statically from
[Netlify](https://kslides.netlify.app)
and [GitHub Pages](https://kslides.github.io/kslides/).
It is also running dynamically on [Heroku](https://kslides-repo.herokuapp.com).

## Getting Started

[![Template](https://img.shields.io/endpoint?color=%232A9EEE&logo=github&style=flat&url=https%3A%2F%2Fraw.githubusercontent.com%2Fkslides%2Fkslides%2Fmaster%2Fdocs%2Fshields%2Ftemplate.json)](https://github.com/kslides/kslides-template/generate)

To create a kslides presentation, generate a new repository using
the [kslides-template](https://github.com/kslides/kslides-template)
repo as a [template](https://github.com/kslides/kslides-template/generate).

The kslides-template [README.md](https://github.com/kslides/kslides-template/blob/master/README.md) describes how to
generate
and publish slide content once you have created your new kslides repo.

## Defining a Presentation

A presentation is created using
a [Kotlin DSL](https://medium.com/adobetech/building-elegant-dsls-with-kotlin-707726c5ed21).
Defining a presentation requires a minimal knowledge of Kotlin. If you are comfortable with Python, Javascript or Java,
you will have no problem with the Kotlin code.

The following _kslides_ definition generates [this presentation](https://kslides.github.io/kslides/helloworld.html).

```kotlin
  kslides {

  output {
    // Write the presentations to a file
    enableFileSystem = true
    // Serve up the presentations via HTTP
    enableHttp = true
  }

  // Default values for all presentations in this file
  presentationConfig {
  }

  presentation {
    // Make this presentation available at helloworld.html
    path = "helloworld.html"

    // Specify css styles as a string
    css +=
      """
        .htmlslide h2 {
          color: yellow;
        }
        """
    // or use the Kotlin CSS DSL
    css {
      rule("#mdslide h2") {
        color = Color.green
      }
    }

    // Default values for all slides in this presentation
    presentationConfig {
      transition = Transition.FADE
      topLeftHref = ""
      topRightHref = ""

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

    // Vertical section with two slides
    verticalSlides {
      // Slide that uses HTML for content
      htmlSlide {
        classes = "htmlslide"

        // Slide-specific config values
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

## ** THE DOCS ARE STILL A WORK IN PROGRESS **

## Sections

### kslides

A `kslides` section contains configuration values, output directives, css defaults, presentation configuration defaults
and presentation defintions. They can be declared in any order.

```kotlin
kslides {
  kslidesConfig {}          // Optional
  output {}                 // Optional
  presentationConfig {}     // Optional
  css {}                    // Optional
  presentation {}           // One or more presentations
}
```

#### _kslides_ variables

| Variable | Default | Description                             | 
|----------|---------|-----------------------------------------|
| _css_    | ""      | String alternative to the css{} section |

### kslidesConfig

The `kslideConfig` section specifies the kslides setup.
The kslideConfig options and defaults values are
[here](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/KSlidesConfig.kt).

### output

The `output` section specifies how slide content is made available. The options and defaults values are
[here](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/OutputConfig.kt).

```kotlin
presentationConfig {
  menuConfig {}           // Optional
  copyCodeConfig {}       // Optional
  playgroundConfig {}     // Optional
  slideConfig {}          // Optional
}
```

A `presentationConfig` section appears in either a kslides or a presentation section. The options and defaults values
are
[here](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/PresentationConfig.kt)
.

A `menuConfig` section appears in a presentationConfig section. The options and defaults values are
[here](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/MenuConfig.kt).

A `copyCodeConfig` section appears in a presentationConfig section. The options and defaults values are
[here](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/CopyCodeConfig.kt)
.

A `playgroundConfig` section appears in a presentationConfig section. The options and defaults values are
[here](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/PlaygroundConfig.kt)
.

A `slideConfig` section appears in either a presentationConfig or a slide section. The options and defaults values are
[here](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/SlideConfig.kt).

### css

Presentation CSS can be specified using raw CSS or the Kotlin [CSS DSL](https://ktor.io/docs/css-dsl.html).

### Presentation

A `presentation` section includes one or more slide sections. There are 3 types of slides.

```kotlin
presentation {
  presentationConfig {}     // Optional
  css {}                    // Optional
  slide {}                  // Where slide can be markdownSlide, htmlSlide or dslSlide
  verticalSlides {
    slide {}
    slide {}
  }
}
```

```kotlin
slide {  // Where slide can be markdownSlide, htmlSlide or dslSlide
  slideConfig {}          // Optional
  content {}              // Required
}
```

### css

Presentation CSS can be specified using raw CSS or the Kotlin [CSS DSL](https://ktor.io/docs/css-dsl.html).

Unlike CSS values in HTML files, which must be specified in the _head_, CSS values in kslide can specified
throughout the definition. It is convenient to have the CSS values right above the code in the slides where
they are referenced.

### presentationConfig

### presentation

```kotlin
presentation {
  css {}
  presentationConfig {}
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

#### _presentation_ variables

| Variable   | Default                              | Description                             | 
|------------|--------------------------------------|-----------------------------------------|
| _path_     | "/"                                  | Presentation directory or filename      |
| _css_      | kslides.css value                    | String alternative to the css{} section |
| _cssFiles_ | kslides.kslidesConfig.cssFiles value | List for including additional css files |
| _jsFiles_  | kslides.kslidesConfig.jsFiles value  | List for including additional js files  |

#### _verticalSlides_ variables

| Variable  | Default | Description                                 | 
|-----------|---------|---------------------------------------------|
| _classes_ | ""      | _class_ value for underlying html _section_ |
| _id_      | ""      | _id_ value for underlying html _section_    |
| _style_   | ""      | _style_ value for underlying html _section_ |

#### _markdownSlide_ variables

| Variable             | Default | Description                                                               | 
|----------------------|---------|---------------------------------------------------------------------------|
| _classes_            | ""      | _class_ value for underlying html _section_                               |
| _id_                 | ""      | _id_ value for underlying html _section_                                  |
| _style_              | ""      | _style_ value for underlying html _section_                               |
| _hidden_             | false   | [Details](https://revealjs.com/slide-visibility/#hidden-slides-4.1.0)     |
| _uncounted_          | false   | [Details](https://revealjs.com/slide-visibility/#uncounted-slides)        |
| _autoAnimate_        | false   | [Details](https://revealjs.com/auto-animate/)                             |
| _autoAnimateRestart_ | false   | [Details](https://revealjs.com/auto-animate/#auto-animate-id-%26-restart) |
| _filename_           | false   | [Details](https://revealjs.com/markdown/#external-markdown)               |

#### _htmlSlide_ variables

| Variable             | Default | Description                                                               | 
|----------------------|---------|---------------------------------------------------------------------------|
| _classes_            | ""      | _class_ value for underlying html _section_                               |
| _id_                 | ""      | _id_ value for underlying html _section_                                  |
| _style_              | ""      | _style_ value for underlying html _section_                               |
| _hidden_             | false   | [Details](https://revealjs.com/slide-visibility/#hidden-slides-4.1.0)     |
| _uncounted_          | false   | [Details](https://revealjs.com/slide-visibility/#uncounted-slides)        |
| _autoAnimate_        | false   | [Details](https://revealjs.com/auto-animate/)                             |
| _autoAnimateRestart_ | false   | [Details](https://revealjs.com/auto-animate/#auto-animate-id-%26-restart) |

#### _dslSlide_ variables

| Variable             | Default | Description                                                               | 
|----------------------|---------|---------------------------------------------------------------------------|
| _classes_            | ""      | _class_ value for underlying html _section_                               |
| _id_                 | ""      | _id_ value for underlying html _section_                                  |
| _style_              | ""      | _style_ value for underlying html _section_                               |
| _hidden_             | false   | [Details](https://revealjs.com/slide-visibility/#hidden-slides-4.1.0)     |
| _uncounted_          | false   | [Details](https://revealjs.com/slide-visibility/#uncounted-slides)        |
| _autoAnimate_        | false   | [Details](https://revealjs.com/auto-animate/)                             |
| _autoAnimateRestart_ | false   | [Details](https://revealjs.com/auto-animate/#auto-animate-id-%26-restart) |


## Misc Notes

### Using dslSlides

[This](https://plugins.jetbrains.com/plugin/12205-html-to-kotlinx-html) plugin makes it much easier to work with
HTML. Just copy some HTML into your copy buffer, and when you paste it, it will give you
the option to convert it to the appropriate Kotlin HTML DSL code. Install it by going to "Plugins" and searching for
`HTML to kotlinx.html` in "Marketplace"

### Formatting

Disable the IntelliJ `Reformat code` option when you commit. The presentation
html files are space-sensitive and might not work if they are formatted.

### Images

* Presentations served by HTTP load static files from `/src/main/resources/public`, whereas
  filesystem presentations load from `/docs`.

### Code Slides

Rather than embedding code directly in markdownSlides, it is much better to use the
`include()` call. You are likely to have space issues if you embed code directly,
If you choose to embed code, remove all indentation in the `content{}` section.

### Local Development

* Speaker Notes do not work properly when running locally.

### Kotlin Playground

Playground code using `dataTargetPlatform = JUNIT` should not hae a `package` decl.

### Heroku

* Add a config var: `GRADLE_TASK=-Pprod=true uberjar`

### MarkDown Slide

* When a `markdownSlide` is in a `verticalSlides` section and references an external file, the string "---"
  is interpreted as a vertical page separator and "--- " (with a space suffix) is rendered as a markdown horizontal
  line.
*

## Third Party Plugins

* https://github.com/Martinomagnifico/reveal.js-copycode
* https://github.com/denehyg/reveal.js-menu

