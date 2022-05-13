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

## kslides DSL

### kslides Block

A `kslides{}` block contains configuration values, output directives, presentation configuration defaults,
css defaults, and presentation defintions. The child blocks can be declared in any order.

#### Options

| Name  | Default | Description                           | 
|-------|---------|---------------------------------------|
| _css_ | ""      | String alternative to the css{} block |

#### Structure

```kotlin
kslides {
  kslidesConfig {}          // Optional
  presentationConfig {}     // Optional
  output {}                 // Optional
  css {}                    // Zero or more css blocks
  presentation {}           // One or more presentations
}
```

* A `kslidesConfig{}` block specifies the kslides configuration for all presentations and has these
[options](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/KSlidesConfig.kt).

* A `presentationConfig{}` block specifies the default presentation configuration values
for all presentations and has these
[options](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/PresentationConfig.kt).

* An `output{}` block specifies how and where presentation slides are published and has these
[options](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/OutputConfig.kt).

* A `css{}` block applies to all presentations and includes Kotlin [CSS DSL](https://ktor.io/docs/css-dsl.html) calls.
CSS can also be specified using raw CSS strings. A combination of the two approaches is also allowed. 

* A `presentation{}` block includes one or more slide blocks. There are 3 types of slides:
markdownSlide, htmlSlide and dslSlide.


### presentationConfig Block

#### Structure

```kotlin
presentationConfig {      // Optional
  menuConfig {}           // Optional
  copyCodeConfig {}       // Optional
  playgroundConfig {}     // Optional
  slideConfig {}          // Optional
}
```

* A `menuConfig{}` block specifies the configuration for the
reveal.js [Menu plugin](https://github.com/denehyg/reveal.js-menu)
and has these
[options](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/MenuConfig.kt).

* A `copyCodeConfig{}` block specifies the configuration for the
reveal.js [CopyCode plugin](https://github.com/Martinomagnifico/reveal.js-copycode)
and has these
[options](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/CopyCodeConfig.kt).

* A `playgroundConfig{}` block specifies the configuration for
[Kotlin Playground](https://github.com/JetBrains/kotlin-playground) iframes
and has these
[options](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/PlaygroundConfig.kt).

* A `slideConfig{}` block specifies the default slide configuration values for all slides and has these 
[options](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/SlideConfig.kt).


### presentation Block

Multiple presentations can be can be defined using multiple `presentation{}` blocks. 

#### Options

| Name       | Default                              | Description                             | 
|------------|--------------------------------------|-----------------------------------------|
| _path_     | "/"                                  | Presentation directory or filename      |
| _css_      | kslides.css value                    | String alternative to the css{} block   |
| _cssFiles_ | kslides.kslidesConfig.cssFiles value | List for including additional css files |
| _jsFiles_  | kslides.kslidesConfig.jsFiles value  | List for including additional js files  |

#### Structure

```kotlin
presentation {
  presentationConfig {}     // Optional
  css {}                    // Zero or more css blocks
  markdownSlide {}          // One or more slides
  htmlSlide {}
  dslSLide {}
}
```

* A `presentationConfig{}` block specifies presentation-specific configuration values and has these
[options](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/PresentationConfig.kt).
These values override those specified in _kslides.presentationConfig{}_. 

* A `css{}` block applies to this specific presentation and includes Kotlin [CSS DSL](https://ktor.io/docs/css-dsl.html) calls.
CSS can also be specified using raw CSS strings. A combination of the two approaches is also allowed.

Unlike CSS values in HTML files, which must be specified in the _head_, `css{}` blocks can be placed
throughout a presentation in kslides. It is convenient to have the CSS values above the code in the slides where
they are referenced.

### kslides.presentation.markdownSlide, kslides.presentation.htmlSlide, and kslides.presentation.dslSlide Blocks

#### Options

| Name                 | Default | Description                                                               | 
|----------------------|---------|---------------------------------------------------------------------------|
| _classes_            | ""      | _class_ value for underlying html tag                                     |
| _id_                 | ""      | _id_ value for underlying html tag                                        |
| _style_              | ""      | _style_ value for underlying html tag                                     |
| _hidden_             | false   | [Details](https://revealjs.com/slide-visibility/#hidden-slides-4.1.0)     |
| _uncounted_          | false   | [Details](https://revealjs.com/slide-visibility/#uncounted-slides)        |
| _autoAnimate_        | false   | [Details](https://revealjs.com/auto-animate/)                             |
| _autoAnimateRestart_ | false   | [Details](https://revealjs.com/auto-animate/#auto-animate-id-%26-restart) |

#### _markdownSlide_-only Options

| Name       | Default | Description                                                 | 
|------------|---------|-------------------------------------------------------------|
| _filename_ | false   | [Details](https://revealjs.com/markdown/#external-markdown) |

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


### slideConfig Block

A `slideConfig{}` block specifies slide-specific configuration values and has these 
[options](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/SlideConfig.kt).
These values override those specified in _kslides.presentationConfig.slideConfig{}_  
and _presentation.presentationConfig.slideConfig{}_.

### content Block

The value types in `content{}` blocks vary by the type of the slide:
* `markdownSlide.content{}` blocks contain a String with Markdown
* `htmlSlide.content{}` blocks contain a String with HTML  
* `dslSlide.content{}` block contains calls to the Kotlin
[HTML DSL](https://github.com/Kotlin/kotlinx.html/wiki/Getting-started).


### verticalSlides Block

A `verticalSlides{}` block contains one or more slides and presents them vertically. 

#### Options

| Name      | Default | Description                           | 
|-----------|---------|---------------------------------------|
| _classes_ | ""      | _class_ value for underlying html tag |
| _id_      | ""      | _id_ value for underlying html tag    |
| _style_   | ""      | _style_ value for underlying html tag |

#### Structure

```kotlin
  verticalSlides {
    dslSLide {}              // One or more slides
    markdownSlide{}
  }
```


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
If you choose to embed code directly in the slide, remove indentation in the `content{}` block.

### Local Development

Speaker Notes do not work properly when running locally.

### Kotlin Playground

Playground code using `dataTargetPlatform = JUNIT` should not have a `package` decl.

### Heroku

Add a GRADLE_TASK config var: `GRADLE_TASK=-Pprod=true uberjar`

### MarkDown Slide

When a `markdownSlide` is in a `verticalSlides{}` block and references an external file, the string "---"
is interpreted as a vertical page separator and "--- " (with a space suffix) is rendered as a markdown horizontal
line.

## Helpful Links

* [reveal.js Menu Plugin](https://github.com/denehyg/reveal.js-menu)
* [reveal.js CopyCode Plugin](https://github.com/Martinomagnifico/reveal.js-copycode)
* [Kotlin Playground](https://github.com/JetBrains/kotlin-playground)

