import com.github.pambrose.common.util.*
import com.kslides.*
import kotlinx.css.*
import kotlinx.html.*

fun main() {

  // slideSource begin
  @KSlidesDslMarker
  fun Presentation.slideSource(
    source: String,
    title: String,
    token: String,
    lines: String = "",
    id: String = "",
  ) {
    markdownSlide {
      if (id.isNotBlank()) this.id = id
      slideConfig { markdownNotesSeparator = "^^" }
      content {
        """
        ## $title    
        ```kotlin $lines
        ${includeFile(source, beginToken = "$token begin", endToken = "$token end")}
        ```
        """
      }
    }
  }
  // slideSource end

  @KSlidesDslMarker
  fun Presentation.slideSource(
    context: VerticalSlideContext,
    source: String,
    title: String,
    token: String,
    lines: String = "",
    id: String = "",
  ) {
    with(context) {
      markdownSlide {
        if (id.isNotBlank()) this.id = id
        slideConfig { markdownNotesSeparator = "^^" }
        content {
          """
          ## $title    
          ```kotlin $lines
          ${includeFile(source, beginToken = "$token begin", endToken = "$token end")}
          ```
          """
        }
      }
    }
  }

  kslides {

    val slides = "kslides-examples/src/main/kotlin/Slides.kt"

    output {
      enableFileSystem = true
      enableHttp = true
    }

    presentationDefault {
      // Default config values for all presentationns go here
    }

    // readme begin
    presentation {
      css +=
        """
        #intro h1 { color: #FF5533; }
        #mdslide p { color: #FF6836; }
        #githubCorner path { fill: #258BD2; }
        """

      presentationConfig {
        githubCornerHref = "https://github.com/kslides/kslides/"
        githubCornerTitle = "View presentation source on Github"
        slideNumber = "c/t"
        hash = true
        history = true
        transition = Transition.SLIDE
        transitionSpeed = Speed.SLOW
        gaPropertyId = "G-TRY2Q243XC"
        enableSpeakerNotes = true
        enableMenu = true
        theme = Theme.SOLARIZED

        slideConfig {
          // defaults for slides can be set here
        }
      }

      verticalSlides {

        // intro begin
        markdownSlide {
          id = "intro"

          slideConfig {
            // Config values for this slide can be set here
            // backgroundColor = "#4370A5"
          }

          content {
            """
            # kslides
            ### A Kotlin DSL wrapper for [reveal.js](https://revealjs.com)
            ### 👇
            Notes: This is a note for the opening slide 📝
            """
          }
        }
        // intro end

        slideSource(this, slides, "Intro Slide Definition", "intro")
      }

      verticalSlides {
        // mdslide begin
        markdownSlide {
          id = "mdslide"

          content {
            """
            # A Markdown Slide
            ## 🍒
            
            Use the arrow keys to navigate ${fragment(Effect.FADE_LEFT)}
            
            Press ESC to see the presentation overview ${fragment(Effect.FADE_LEFT)}
                      
            Press the down arrow to see the slide definition 👇 ${fragment(Effect.FADE_LEFT)}

            Notes: This is a note for the Markdown slide 📝
            """
          }
        }
        // mdslide end

        slideSource(this, slides, "Markdown Slide Definition", "mdslide")
      }

      verticalSlides {
        // htmlslide begin
        htmlSlide {
          content {
            """
            <h1>An HTML Slide</h1>
            <h2>🐦</h2>
            <p class="fragment fade-up">Press S to see the speaker notes</p> 
            <p class="fragment fade-up">Press M to see the menu</p> 
            <p class="fragment fade-up">Press B or . to pause the presentation</p> 
            <p class="fragment fade-up">Press the down arrow to see the slide definition 👇</p>

            <aside class="notes">
              This is a note for the HTML slide 📝
            </aside>
            """
          }
        }
        // htmlslide end

        slideSource(this, slides, "HTML Slide Definition", "htmlslide")
      }

      verticalSlides {
        // dslslide begin
        dslSlide {
          content {
            h1 { +"A DSL Slide" }
            h2 { +"👀" }
            p("fragment fade-right") { +"Press CTRL+Shift+F to search all the slides" }
            p("fragment fade-right") { +"Press Alt+click to zoom in on elements" }
            p("fragment fade-right") { +"Press the down arrow to see the slide definition 👇" }
            notes { +"This is a note for the DSL slide 📝" }
          }
        }
        // dslslide end

        slideSource(this, slides, "DSL Slide Definition", "dslslide")
      }

      verticalSlides {
        // highlights begin
        markdownSlide {
          content {
            """
            ## Highlighted Code    
            ```kotlin [3,7|4,6|5]
            ${includeFile("kslides-examples/src/main/kotlin/examples/HelloWorldK.kt")}
            ```
            ### 👇 
            Note: This slide shows code highlights. You can specify the lines you want to highlight
            """
          }
        }
        // highlights end

        slideSource(this, slides, "Highlighted Code Definition", "highlights")
      }

      verticalSlides {
        // animated begin
        // A for loop generates a series of slides, each with a different set of lines
        // Uses the same line number syntax used by revealjs: https://revealjs.com/code/
        for (lines in lineNumbers("[5,6,9|5-9|]")) {
          htmlSlide {
            autoAnimate = true
            content {
              """
              <h2>Animated Code</h2>  
              <pre data-id="code-animation" data-cc="false"> 
                <code data-trim="" data-line-numbers="">
                  ${includeFile("kslides-examples/src/main/kotlin/examples/assign.js", lines)}
                </code>
              </pre>
              <h2>👇</h2>  
              <aside class="notes">
              This slide shows animated code highlights. 
              Indicate the lines you want to highlight by using a for loop
              </aside>
              """
            }
          }
        }
        // animated end

        slideSource(this, slides, "Animated Code Definition", "animated")
      }

      verticalSlides {
        // iframe begin
        dslSlide {
          slideConfig {
            backgroundIframe = "https://revealjs.com/backgrounds/#iframe-backgrounds"
          }
          content {
            div {
              style =
                """
                  position: absolute; width: 40%; right: 0; 
                  box-shadow: 0 1px 4px rgba(0,0,0,0.5), 0 5px 25px rgba(0,0,0,0.2); 
                  background-color: rgba(0, 0, 0, 0.9); 
                  color: #fff; 
                  padding: 20px; 
                  font-size: 20px; 
                  text-align: left;
                """
              h2 { +"Iframe Backgrounds" }
              p {
                +"""Since reveal.js runs on the web, you can easily embed other web content. Try interacting with the
              page in the background."""
              }
            }
          }
        }
        // iframe end

        slideSource(this, slides, "Iframe Backgrounds Definition", "iframe")
      }

      verticalSlides {
        // transition begin
        dslSlide {
          id = "transitions"
          content {
            h2 { +"Transitions" }
            p {
              +"You can select from different transitions, like:"
              br {}
              // The Transition enum includes all the built-in transitions
              Transition.values()
                .forEachIndexed { index, transition ->
                  a { href = "?transition=${transition.name.toLower()}#/transitions"; +transition.name }
                  if (index < Transition.values().size - 1)
                    +"-"
                }
            }
          }
        }
        // transition end

        slideSource(this, slides, "Transitions Definition", "transition")
      }

      verticalSlides {
        // themes begin
        dslSlide {
          id = "themes"
          content {
            h2 { +"Themes" }
            p {
              +"reveal.js comes with some built-in themes:"
              br {}
              // The Theme enum includes all the built in themes
              Theme.values()
                .forEachIndexed { index, theme ->
                  a {
                    href = "#/themes"
                    onClick =
                      "document.getElementById('theme').setAttribute('href','dist/theme/${theme.name.toLower()}.css'); return false;"
                    +theme.name
                  }
                  if (index < Theme.values().size - 1)
                    +"-"
                }
            }
          }
        }
        // themes end

        slideSource(this, slides, "Themes Definition", "themes")
      }

      verticalSlides {
        // video begin
        dslSlide {
          slideConfig {
            backgroundVideo = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
          }

          content {
            h1 {
              style = "color: red;"
              +"Video Backgrounds"
            }
          }
        }
        // video end

        slideSource(this, slides, "Video Definition", "video")
      }

      verticalSlides {
        // navigation begin
        markdownSlide {
          content {
            """
            ## Navigation Slide 🦊 
            
            [Go to the 1st slide](#/intro) ${fragment()}
         
            [Go to the 2nd slide](#/mdslide) ${fragment()}
            
            [Go to the definition slide](#/definition) ${fragment()}
            
            """
          }
        }
        // navigation end

        slideSource(this, slides, "Navigtion Definition", "navigation")
      }

      slideSource(slides, "Presentation Definition", "readme", "[]", "definition")

      slideSource(slides, "Slide Source Definition", "slideSource", "[]")
    }
    // readme end

    presentation {
      path = "helloworld.html"               // Make this presentation available at helloworld.html

      css +=                                 // css styles can be specified as a string or with the kotlin css DSL
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

      presentationConfig {                   // These values will be the default values for all the slides
        transition = Transition.FADE

        slideConfig {                        // slideConfig values here override the presentationDefault values
          backgroundColor = "#2A9EEE"
        }
      }

      markdownSlide {                        // Slide that uses Markdown
        id = "mdslide"

        content {
          """
          # Markdown
          ## Hello World
          """
        }
      }

      verticalSlides {                       // Two vertical slides
        htmlSlide {                          // Slide that uses HTML
          classes = "htmlslide"

          slideConfig {                      // slideConfig values here override the presentationDefault values
            backgroundColor = "red"
          }

          content {
            """
            <h1>HTML</h1>
            <h2>Hello World</h2>
            """
          }
        }

        dslSlide {                           // Slide that uses the Kotlin HTML DSL
          content {
            h1 { +"DSL" }
            h2 { +"Hello World" }
          }
        }
      }
    }
  }
}