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
    // Also serve up the presentations via HTTP
    enableHttp = true
  }

  // Default values for all presentations
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

    // Config values for this presentation
    presentationConfig {
      transition = Transition.FADE
      topLeftHref = ""  // Turn off top left href
      topRightHref = "" // Turn off top right href

      // Default values for all slides in this presentation
      slideConfig {
        backgroundColor = "#2A9EEE"
      }
    }

    // Slide that uses Markdown content
    markdownSlide {
      id = "mdslide"

      content {
        """
        # Markdown
        ## Hello World
        """
      }
    }

    // Two vertical slides slides
    verticalSlides {
      // Slide that uses HTML content
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

      // Slide that uses Kotlin HTML DSL content
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

## DSL Blocks

### _kslides{}_ Block

A `kslides{}` block contains configuration values, output directives, presentation configuration defaults,
css defaults, and presentation defintions. The child blocks can be declared in any order.

#### _kslides{}_ Structure

```kotlin
kslides {
  kslidesConfig {}          // Optional
  presentationConfig {}     // Optional
  output {}                 // Optional
  css {}                    // Optional and one or more 
  presentation {}           // One or more
}
```

#### _kslides{}_ Variables

| Variable | Default | Description                           | 
|----------|---------|---------------------------------------|
| _css_    | ""      | String alternative to the css{} block |

### _kslidesConfig{}_ Block

A `kslidesConfig{}` block specifies the kslides configuration for all presentations and has these
[options](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/KSlidesConfig.kt)
.

### _presentationConfig{}_ Block

A `presentationConfig{}` block can be used in a `kslides{}` block or a `presentation{}` block. 
As part of `kslides{}` block, it acts as the default value for all presentations.
As part of `presentation{}` block, it acts as the presentation-specific configuration.
A `presentationConfig{}` blocks has these
[options](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/PresentationConfig.kt)
.

#### _presentationConfig{}_ Structure

```kotlin
presentationConfig {      // Optional
  menuConfig {}           // Optional
  copyCodeConfig {}       // Optional
  playgroundConfig {}     // Optional
  slideConfig {}          // Optional
}
```


A `menuConfig` block appears in a presentationConfig block.
The menuConfig options and defaults values are described
[here](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/MenuConfig.kt).

A `copyCodeConfig` block appears in a presentationConfig block.
The copyCodeConfig options and defaults values are described
[here](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/CopyCodeConfig.kt)
.

A `playgroundConfig` block appears in a presentationConfig block.
The playgroundConfig options and defaults values are described
[here](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/PlaygroundConfig.kt)
.

A `slideConfig` block appears in either a presentationConfig or a slide block.
The slideConfig options and defaults values are described
[here](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/SlideConfig.kt).

### _output{}_ Block

The `output` block specifies how slide content is made available. The options and defaults values are
[here](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/OutputConfig.kt).

### _css{}_ Block

Presentation CSS can be specified using raw CSS or the Kotlin [CSS DSL](https://ktor.io/docs/css-dsl.html).

### _presentation{}_ Block

A `presentation` block includes one or more slide blocks. There are 3 types of slides.

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

### css{} Block and/or css String

Presentation CSS can be specified using raw CSS or the Kotlin [CSS DSL](https://ktor.io/docs/css-dsl.html).

Unlike CSS values in HTML files, which must be specified in the _head_, CSS values in kslide can specified
throughout the definition. It is convenient to have the CSS values right above the code in the slides where
they are referenced.

### presentationConfig{} Block

### presentation{} Block

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

#### _presentation{}_ Block Variables

| Variable   | Default                              | Description                             | 
|------------|--------------------------------------|-----------------------------------------|
| _path_     | "/"                                  | Presentation directory or filename      |
| _css_      | kslides.css value                    | String alternative to the css{} block |
| _cssFiles_ | kslides.kslidesConfig.cssFiles value | List for including additional css files |
| _jsFiles_  | kslides.kslidesConfig.jsFiles value  | List for including additional js files  |

#### _verticalSlides{}_ Block Variables

| Variable  | Default | Description                           | 
|-----------|---------|---------------------------------------|
| _classes_ | ""      | _class_ value for underlying html tag |
| _id_      | ""      | _id_ value for underlying html tag    |
| _style_   | ""      | _style_ value for underlying html tag |

#### _markdownSlide{}_ Block Variables

| Variable             | Default | Description                                                               | 
|----------------------|---------|---------------------------------------------------------------------------|
| _classes_            | ""      | _class_ value for underlying html tag                                     |
| _id_                 | ""      | _id_ value for underlying html tag                                        |
| _style_              | ""      | _style_ value for underlying html tag                                     |
| _hidden_             | false   | [Details](https://revealjs.com/slide-visibility/#hidden-slides-4.1.0)     |
| _uncounted_          | false   | [Details](https://revealjs.com/slide-visibility/#uncounted-slides)        |
| _autoAnimate_        | false   | [Details](https://revealjs.com/auto-animate/)                             |
| _autoAnimateRestart_ | false   | [Details](https://revealjs.com/auto-animate/#auto-animate-id-%26-restart) |
| _filename_           | false   | [Details](https://revealjs.com/markdown/#external-markdown)               |

#### _htmlSlide{}_ Block Variables

| Variable             | Default | Description                                                               | 
|----------------------|---------|---------------------------------------------------------------------------|
| _classes_            | ""      | _class_ value for underlying html tag                                     |
| _id_                 | ""      | _id_ value for underlying html tag                                        |
| _style_              | ""      | _style_ value for underlying html tag                                     |
| _hidden_             | false   | [Details](https://revealjs.com/slide-visibility/#hidden-slides-4.1.0)     |
| _uncounted_          | false   | [Details](https://revealjs.com/slide-visibility/#uncounted-slides)        |
| _autoAnimate_        | false   | [Details](https://revealjs.com/auto-animate/)                             |
| _autoAnimateRestart_ | false   | [Details](https://revealjs.com/auto-animate/#auto-animate-id-%26-restart) |

#### _dslSlide{}_ Block Variables

| Variable             | Default | Description                                                               | 
|----------------------|---------|---------------------------------------------------------------------------|
| _classes_            | ""      | _class_ value for underlying html tag                                     |
| _id_                 | ""      | _id_ value for underlying html tag                                        |
| _style_              | ""      | _style_ value for underlying html tag                                     |
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
If you choose to embed code, remove all indentation in the `content{}` block.

### Local Development

* Speaker Notes do not work properly when running locally.

### Kotlin Playground

Playground code using `dataTargetPlatform = JUNIT` should not hae a `package` decl.

### Heroku

* Add a config var: `GRADLE_TASK=-Pprod=true uberjar`

### MarkDown Slide

* When a `markdownSlide` is in a `verticalSlides` block and references an external file, the string "---"
  is interpreted as a vertical page separator and "--- " (with a space suffix) is rendered as a markdown horizontal
  line.
*

## Third Party Plugins

* https://github.com/Martinomagnifico/reveal.js-copycode
* https://github.com/denehyg/reveal.js-menu

