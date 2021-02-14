# kslides

[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/pambrose/kslides)
[![Deploy on Heroku](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/pambrose/kslides)
[![Run on Repl.it](https://repl.it/badge/github/pambrose/kslides)](https://repl.it/github/pambrose/kslides)

**kslides** is a Kotlin DSL wrapper for [reveal.js](https://revealjs.com). It is meant for people who would prefer to
build presentations with IntelliJ than Powerpoint.

## Presentation Structure

```kotlin
presentation {
  htmlSlide(id = "start") {
    h1 { +"HTML Slide 🐦" }
    p { +"Use the arrow keys to navigate" }
  }

  markdownSlide(transition = Zoom, speed = Slow) {
    +"""
      # Markdown Slide 🍒 
      
      Press ESC to see presentation overview.
    """
  }

  markdownSlide(backgroundColor = "#4370A5") {
    +"""
      # Code Highlights    
      ```kotlin [1|2,5|3-4]
      fun main() {
          repeat(10) {
              println("Hello")
              println("World")
          }
      }
    """
  }
  verticalSlides {
    htmlSlide {
      h1 { +"Vertical HTML Slide 👇" }
    }

    markdownSlide {
      +"""
        # Vertical Markdown Slide 🦊 
        
        [Go back to the 1st slide](#/start) ${fragmentIndex(1)}
      
        [Go back to the 2nd slide](#/1) ${fragmentIndex(2)}
      """
    }
  }
}
```

Click [here](https://kslides-simple.herokuapp.com) to see [this presentation](src/main/kotlin/Simple.kt) running.

## Notes

* When a `markdownSlide` is in a `verticalSlides` section and references an external file, the string "---"
  is interpreted as a vertical page separator and "--- " (with a space suffix) is rendered as a markdown horizontal
  line. 
