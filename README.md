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
  presentation {}           // One or more presentations
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

#### Structure

```kotlin
presentationConfig {      // Optional
  menuConfig {}           // Optional
  copyCodeConfig {}       // Optional
  playgroundConfig {}     // Optional
  slideConfig {}          // Optional
}
```

A `menuConfig{}` block specifies the configuration for the
[reveal.js menu plugin](https://github.com/denehyg/reveal.js-menu)
and has these
[options](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/MenuConfig.kt).

A `copyCodeConfig{}` block specifies the configuration for the
[reveal.js CopyCode plugin](https://github.com/Martinomagnifico/reveal.js-copycode)
and has these
[options](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/CopyCodeConfig.kt).

A `playgroundConfig{}` block specifies the configuration for
[Kotlin Playgrounds](https://github.com/JetBrains/kotlin-playground)
and has these
[options](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/PlaygroundConfig.kt).

A `slideConfig{}` block appears in either a presentationConfig or a slide block.
The slideConfig options and defaults values are described
[options](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/SlideConfig.kt).

### _output{}_ Block

An `output{}` block specifies how and where presentation slides are published 
and has these
[options](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/OutputConfig.kt).

### _css{}_ Block

Presentation CSS can be specified using raw CSS or the Kotlin [CSS DSL](https://ktor.io/docs/css-dsl.html).

### _presentation{}_ Block

A `presentation{}` block includes one or more slide blocks. There are 3 types of slides: 
markdownSlide, htmlSlide and dslSlide.

#### Structure

```kotlin
presentation {
  presentationConfig {}     // Optional
  css {}                    // Optional
  slide {}                  // One or more slides
}
```

#### Variables

| Variable   | Default                              | Description                             | 
|------------|--------------------------------------|-----------------------------------------|
| _path_     | "/"                                  | Presentation directory or filename      |
| _css_      | kslides.css value                    | String alternative to the css{} block |
| _cssFiles_ | kslides.kslidesConfig.cssFiles value | List for including additional css files |
| _jsFiles_  | kslides.kslidesConfig.jsFiles value  | List for including additional js files  |


### _presentationConfig{}_ Block

### _css{}_ Block and/or _css_ String

Presentation CSS can be specified using raw CSS or the Kotlin [CSS DSL](https://ktor.io/docs/css-dsl.html).

Unlike CSS values in HTML files, which must be specified in the _head_, CSS values in kslide can specified
throughout the definition. It is convenient to have the CSS values right above the code in the slides where
they are referenced.

### _slide{}_ Blocks

#### Structure

```kotlin
markdownSlide {  
  slideConfig {}          // Optional
  content {}              // Required
}

htmlSlide {  
  slideConfig {}          // Optional
  content {}              // Required
}

dslSlide {  
  slideConfig {}          // Optional
  content {}              // Required
}
```

#### Variables

| Variable             | Default | Description                                                               | 
|----------------------|---------|---------------------------------------------------------------------------|
| _classes_            | ""      | _class_ value for underlying html tag                                     |
| _id_                 | ""      | _id_ value for underlying html tag                                        |
| _style_              | ""      | _style_ value for underlying html tag                                     |
| _hidden_             | false   | [Details](https://revealjs.com/slide-visibility/#hidden-slides-4.1.0)     |
| _uncounted_          | false   | [Details](https://revealjs.com/slide-visibility/#uncounted-slides)        |
| _autoAnimate_        | false   | [Details](https://revealjs.com/auto-animate/)                             |
| _autoAnimateRestart_ | false   | [Details](https://revealjs.com/auto-animate/#auto-animate-id-%26-restart) |

#### _markdownSlide_-only Variables

| Variable             | Default | Description                                                               | 
|----------------------|---------|---------------------------------------------------------------------------|
| _filename_           | false   | [Details](https://revealjs.com/markdown/#external-markdown)               |


### _verticalSlides{}_ Block

A `verticalSlides{}` block contains one or more slides and presents them vertically.

#### Structure

```kotlin
  verticalSlides {
    dslSLide {}              // One or more slides
    markdownSlide{}
  }
```

#### Variables

| Variable  | Default | Description                           | 
|-----------|---------|---------------------------------------|
| _classes_ | ""      | _class_ value for underlying html tag |
| _id_      | ""      | _id_ value for underlying html tag    |
| _style_   | ""      | _style_ value for underlying html tag |


## Misc Notes

### Using dslSlides

[This](https://plugins.jetbrains.com/plugin/12205-html-to-kotlinx-html) plugin makes it much easier to work with
HTML. Just copy some HTML into your copy buffer, and when you paste it, it will give you
the option to convert it to the appropriate Kotlin HTML DSL code. Install it in IntelliJ by going to 
"Plugins" and searching for `HTML to kotlinx.html` in "Marketplace"

### IntelliJ Settings

Disable the IntelliJ `Reformat code` and `Rearrange code` options when you commit to git.
The code in the presentation html files are space-sensitive and might not work if they are reformatted.

### Images

Presentations served by HTTP load static files from `/src/main/resources/public`, whereas
filesystem presentations load from `/docs`.

### Code Slides

Rather than embedding code directly in markdownSlides, it is much better to use the
`include()` call. You are likely to have formatting issues if you embed code directly
in the slide.
If you choose to embed code, remove indentation in the `content{}` block.

### Local Development

Speaker Notes do not work properly when running locally.

### Kotlin Playground

Playground code using `dataTargetPlatform = JUNIT` should not have a `package` decl.

### Heroku

Add a GRADLE_TASK config var: `GRADLE_TASK=-Pprod=true uberjar`

### MarkDown Slide

When a `markdownSlide` is in a `verticalSlides` block and references an external file, the string "---"
is interpreted as a vertical page separator and "--- " (with a space suffix) is rendered as a markdown horizontal
line.

## Third Party Plugins

* https://github.com/denehyg/reveal.js-menu
* https://github.com/Martinomagnifico/reveal.js-copycode

