# kslides

[![Release](https://jitpack.io/v/kslides/kslides.svg)](https://jitpack.io/#kslides/kslides)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/701fc37a847042d2ae2cd6e80075ff6f)](https://www.codacy.com/gh/kslides/kslides/dashboard?amp;utm_medium=referral&amp;utm_content=kslides/kslides&amp;utm_campaign=Badge_Grade)
[![Build Status](https://app.travis-ci.com/kslides/kslides.svg?branch=master)](https://app.travis-ci.com/kslides/kslides)
[![Kotlin version](https://img.shields.io/badge/kotlin-1.6.21-red?logo=kotlin)](http://kotlinlang.org)
[![Netlify Status](https://api.netlify.com/api/v1/badges/6d0c3c20-6eb5-4c74-8451-5fa06acf242f/deploy-status)](https://app.netlify.com/sites/kslides/deploys)

**kslides** is a [Kotlin](https://kotlinlang.org) DSL for the awesome [reveal.js](https://revealjs.com)
presentation framework. It is meant for people who prefer working with an IDE rather than PowerPoint.
It works particularly well for presentations with code snippets and HTML animations. Slides are
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
generate and publish slide content once you have created and updated your new kslides repo.

## Defining a Presentation

A presentation is created using
a [Kotlin DSL](https://medium.com/adobetech/building-elegant-dsls-with-kotlin-707726c5ed21).
Defining a presentation requires a [minimal knowledge](#kotlin-details) 
of Kotlin. If you are comfortable with Python, Javascript or Java, you will have no problem with the Kotlin code.

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
css defaults, and presentation definitions. The child blocks can be declared in any order.

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

* A `css{}` block applies to all presentations and uses Kotlin [CSS DSL](https://ktor.io/docs/css-dsl.html) calls.
Presentation CSS can also be specified using raw CSS strings. A combination of the two approaches is also allowed. 

* A `presentation{}` block includes one or more slide descriptions. There are 3 types of slides:
_MarkdownSlide_, _HtmlSlide_ and _DslSlide_.


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

Multiple presentations can be defined using multiple `presentation{}` blocks, each with
a different `path` value.

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
  verticalSlide {
    htmlSlide {}
    dslSLide {}
  }
}
```

* A `presentationConfig{}` block specifies presentation-specific configuration values and has these
[options](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/config/PresentationConfig.kt).
This block overrides the values specified in _kslides.presentationConfig{}_. 

* A `css{}` block applies to this specific presentation and uses Kotlin [CSS DSL](https://ktor.io/docs/css-dsl.html) calls.
Presentation CSS can also be specified using raw CSS strings. A combination of the two approaches is also allowed.

Unlike CSS values in HTML files, which must be specified in the _head_, `css{}` blocks can be placed
throughout a presentation in kslides. It is convenient to have the CSS values near code in the slides where
they are referenced.

### markdownSlide, htmlSlide, and dslSlide Blocks

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

#### markdownSlide-specific Options

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
This block overrides the values specified in _kslides.presentationConfig.slideConfig{}_ and 
_kslides.presentation.presentationConfig.slideConfig{}_.

### content Block

`content{}` block contents vary by the type of the slide:
* `markdownSlide.content{}` blocks contain a String with Markdown
* `htmlSlide.content{}` blocks contain a String with HTML  
* `dslSlide.content{}` block contains calls to the Kotlin
[HTML DSL](https://github.com/Kotlin/kotlinx.html/wiki/Getting-started)
* `verticalSlides{}` block contains other slides


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
  markdownSlide {}
}
```

## kslides Functions

These functions are defined [here](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/Utils.kt).
Examples of their usage can be found [here](https://github.com/kslides/kslides/blob/master/kslides-examples/src/main/kotlin/Slides.kt).

| Function name             | Context            | Description                                |
|---------------------------|--------------------|--------------------------------------------|
| `slideBackground()`       | MarkdownSlide only |                                            |
| `fragment()`              | MarkdownSlide only |                                            |
| `rawHtml()`               | DslSlide           | Allows embedding of raw HTML in a DslSlide |
| `List<T>.permuteBy()`     | Animations         |                                            |
| `String.toLinePatterns()` | Animations         |                                            |
| `githubSourceUrl()`       | include() calls    | Returns URL for github content             |
| `githubRawUrl()`          | include() calls    | Returns URL for raw github content         |
| `include()`               | All Slides         | Preferred to embedding raw code in slides  |

The `include()` call accepts a filename or a URL _src_ argument. A filename is relative to the root of the repo
and a URL requires an _http://_ or _https://_ prefix.

## DslSlide-specific Functions

DslSlide-specific functions are defined [here](https://github.com/kslides/kslides/blob/master/kslides-core/src/main/kotlin/com/kslides/KSlidesDsl.kt).
Examples of their usage can be found [here](https://github.com/kslides/kslides/blob/master/kslides-examples/src/main/kotlin/Slides.kt).

| Function name     | Description                 |
|-------------------|-----------------------------|
| `codeSnippet{}`   | Embed a code snippet        |
| `playground{}`    | Embed a Kotlin Playground   |
| `unorderedList{}` | Generate an unordered list  |
| `orderedList()`   | Generate an ordered list    |
| `LI.listHref()`   | Generate a list href        |
| `THEAD.headRow()` | Generate a table header row |
| `TBODY.bodyRow()` | Generate a table body row   |


## Misc Notes

### Kotlin Details

kslides requires some Kotlin-specific knowledge:
* [String Interpolation](https://metapx.org/kotlin-string-interpolation/)
* [Named Arguments](https://kotlinlang.org/docs/functions.html#named-arguments)
* [Multiline Strings](https://kotlinlang.org/docs/java-to-kotlin-idioms-strings.html#use-multiline-strings)

### Using DslSlides

[This](https://plugins.jetbrains.com/plugin/12205-html-to-kotlinx-html) plugin makes it much easier to work with
HTML. Just copy some HTML into your copy buffer, and when you paste it, the plugin will give you
the option to convert it into Kotlin HTML DSL code. Install it in IntelliJ by going to 
"Plugins" and searching for `HTML to kotlinx.html` in "Marketplace"

### IntelliJ Settings

Disable the IntelliJ `Reformat code` and `Rearrange code` options when you commit to git.
The code in the presentation html files are space-sensitive and might not work if they are reformatted.

### Custom CSS
CSS values can be specified in a _css{}_ blocks in a presentation, but they also can be specified in 
the `src/main/resources/slides.css` file. The contents of that file are embedded directly into 
the presentation HTML files. Make sure you run 
`./gradlew clean build` after making changes to _slides.css_. 

### Images

Presentations served by HTTP load static files from `/src/main/resources/public`, whereas
filesystem presentations load from `/docs`.

### Code Slides

Rather than embedding code directly in markdownSlides, it is much better to use the
`include()` call. You are likely to have formatting issues if you embed code directly
in the slide.
If you choose to embed code directly in the slide, remove indentation in the `content{}` block.

### Local Development

Speaker Notes do not work properly when running locally. They will work from GitHub, Netlify, or Heroku
though.

### Kotlin Playground

Playground code using `dataTargetPlatform = JUNIT` should not have a `package` decl.

### Heroku

Go to your Heroku dashboard, choose your kslides app and click 
on _Settings_->_Reveal Config Vars_ and add a config var: `GRADLE_TASK=-Pprod=true uberjar`

### MarkDown Slide

When a `markdownSlide` is in a `verticalSlides{}` block and references an external file, the string "---"
is interpreted as a vertical page separator and "--- " (with a space suffix) is rendered as a markdown horizontal
line.

## Helpful Links

* [reveal.js Menu Plugin](https://github.com/denehyg/reveal.js-menu)
* [reveal.js CopyCode Plugin](https://github.com/Martinomagnifico/reveal.js-copycode)
* [Kotlin Playground](https://github.com/JetBrains/kotlin-playground)

