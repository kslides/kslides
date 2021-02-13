# kslides

[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/pambrose/kslides)
[![Deploy on Heroku](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/pambrose/kslides)
[![Run on Repl.it](https://repl.it/badge/github/pambrose/kslides)](https://repl.it/github/pambrose/kslides)

A Kotlin DSL wrapper for people who would prefer to build presentations with IntelliJ than Powerpoint.

kslides wraps [reveal.js](https://revealjs.com).

## Presentation Structure

```kotlin
presentation {
  htmlSlide(id = "start") {
    h1 { +"HTML Slide 🐦" }
    p { +"Press ESC to see presentation overview" }
  }

  markdownSlide {
    +"""
           # Markdown Slide 🍒 
           
           Use the arrow keys to navigate.
        """
  }

  verticalSlides {
    htmlSlide {
      h1 { +"Vertical HTML Slide 👇" }
    }

    markdownSlide {
      +"""
                # Vertical Markdown Slide 🦊 
                
                [Go back to the 1st slide](#/start)
            """
    }
  }
}
```

Click [here](https://kslides-simple.herokuapp.com) to see how this presentation appears when run as
a [kotlin program](src/main/kotlin/Simple.kt).

## Odd Behavior

* When a `markdownSlide` references an external file and is in a `verticalSlides` section, the string "---"
  is interpreted as a vertical page separator and "--- " (with a space suffix) is rendered as a markdown horizontal
  line. 
