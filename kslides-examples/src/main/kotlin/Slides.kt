import com.github.pambrose.common.util.*
import com.kslides.*
import kotlinx.css.*
import kotlinx.html.*

fun main() {

  kslides {

    val slides = "kslides-examples/src/main/kotlin/Slides.kt"

    output {
      enableFileSystem = true
      enableHttp = true
    }

    presentationDefault {
      // Default config values for all presentations go here
      topLeftHref = "https://github.com/kslides/kslides/"
      topLeftTitle = "View presentation source on Github"

      topRightHref = "/"
      topRightTitle = "Go to 1st Slide"
      topRightText = "🏠"

      slideNumber = "c/t"
      hash = true
      history = true
      transition = Transition.SLIDE
      transitionSpeed = Speed.SLOW
      gaPropertyId = "G-TRY2Q243XC"
      enableSpeakerNotes = true
      enableMenu = true
      theme = Theme.SOLARIZED
      center = true
    }

    // readme begin
    presentation {
      css +=
        """
        #intro h1 { color: #FF5533; }
        #mdslide p { color: #FF6836; }
        img[alt=slide] { width: 150px; }
        """

      presentationConfig {
        // presentation-sepecific confurations go here
        slideConfig {
          // defaults for slides go here
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
            ![slide](images/slide-transparent.png)

            ### A Kotlin DSL wrapper for [reveal.js](https://revealjs.com)
            ### 👇
            Notes: This is a note for the opening slide 📝
            """
          }
        }
        // intro end

        slideSource(slides, "intro")
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

        slideSource(slides, "mdslide")
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

        slideSource(slides, "htmlslide")
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

        slideSource(slides, "dslslide")
      }

      verticalSlides {
        // highlights begin
        markdownSlide {
          content {
            """
            ## Highlighted Code with a markdownSlide   
            ```kotlin [|3,7|4,6| 5 | 4-6]
            ${includeFile("kslides-examples/src/main/kotlin/examples/HelloWorldK.kt")}
            ```
            ### 👇 
            Note: This slide shows code highlights. You can specify the lines you want to highlight
            """
          }
        }
        // highlights end

        slideSource(slides, "highlights")
      }

      verticalSlides {
        // animated-html begin
        // A for loop generates a series of slides, each with a different set of lines
        // Uses the same line number syntax used by revealjs: https://revealjs.com/code/
        for (lines in lineNumbers("[5,6,9|5-9|]"))
          htmlSlide {
            autoAnimate = true
            content {
              """
              <h2>Animated Code with an htmlSlide</h2>
              <pre data-id="code-animation" data-cc="false">
                <code data-trim="" data-line-numbers="">
                  ${includeFile("kslides-examples/src/main/kotlin/examples/assign.js", lines)}
                </code>
              </pre>
              <h2>👇</h2>
              <aside class="notes">
              This slide shows animated code highlights.
              </aside>
              """
            }
          }
        // animated-html end

        slideSource(slides, "animated-html")
      }

      verticalSlides {
        // animated-dsl begin
        // A for loop generates a series of slides, each with a different set of lines
        // Uses the same line number syntax used by revealjs: https://revealjs.com/code/
        for (lines in lineNumbers("[5,6,9|5-9|]"))
          dslSlide {
            autoAnimate = true
            content {
              h2 { +"Animated Code with a dslSlide" }
              pre {
                attributes["data-id"] = "code-animation"
                attributes["data-cc"] = "false"
                code {
                  attributes["data-trim"] = ""
                  attributes["data-line-numbers"] = ""
                  +includeFile("kslides-examples/src/main/kotlin/examples/assign.js", lines)
                }
              }
              h2 { +"👇" }
              aside("notes") {
                +"This slide shows animated code highlights."
              }
            }
          }
        // animated-dsl end

        slideSource(slides, "animated-dsl")
      }

      verticalSlides {
        // swapping begin
        listOf("One", "Two", "Three", "Four")
          .permuteBy(
            listOf(0, 1),
            listOf(0, 1, 2),
            listOf(0, 1, 2, 3),
            listOf(1, 0, 3, 2),
            listOf(1, 2, 0, 3),
            listOf(1, 2, 3, 0),
            listOf(0, 3, 2, 1),
          )
          .forEach { items ->
            dslSlide {
              autoAnimate = true
              content {
                h2 { +"Animated List Items 👇" }
                unorderedList(*items.toTypedArray())
              }
            }
          }
        // swapping end

        slideSource(slides, "swapping")
      }

      verticalSlides {

        dslSlide {
          style = "height: 600px"
          autoAnimate = true

          content {
            h4 {
              attributes["data-id"] = "slidenum"
              style = "opacity: 0.75;"
              +"Slide 1"
            }
            h2 {
              attributes["data-id"] = "title"
              style = "margin-top: 250px;"
              +"Animate Anything 👇"
            }
            div {
              attributes["data-id"] = "1"
              style = "background: cyan; position: absolute; top: 150px; left: 16%; width: 60px; height: 60px;"
            }
            div {
              attributes["data-id"] = "2"
              style = "background: magenta; position: absolute; top: 150px; left: 36%; width: 60px; height: 60px;"
            }
            div {
              attributes["data-id"] = "3"
              style = "background: yellow; position: absolute; top: 150px; left: 56%; width: 60px; height: 60px;"
            }
            div {
              attributes["data-id"] = "4"
              style = "background: red; position: absolute; top: 150px; left: 76%; width: 60px; height: 60px;"
            }
          }
        }

        dslSlide {
          style = "height: 600px"
          autoAnimate = true

          content {
            h4 {
              attributes["data-id"] = "slidenum"
              style = "opacity: 0.75;"
              +"Slide 2"
            }
            h2 {
              attributes["data-id"] = "title"
              style = "margin-top: 450px"
              +"With Auto Animate 👇"
            }
            div {
              attributes["data-id"] = "1"
              style = "background: cyan; position: absolute; bottom: 190px; left: 16%; width: 60px; height: 60px;"
            }
            div {
              attributes["data-id"] = "2"
              style = "background: magenta; position: absolute; bottom: 190px; left: 36%; width: 60px; height: 160px;"
            }
            div {
              attributes["data-id"] = "3"
              style = "background: yellow; position: absolute; bottom: 190px; left: 56%; width: 60px; height: 260px;"
            }
            div {
              attributes["data-id"] = "4"
              style = "background: red; position: absolute; bottom: 190px; left: 76%; width: 60px; height: 360px;"
            }
          }
        }

        dslSlide {
          style = "height: 600px"
          autoAnimate = true

          content {
            h4 {
              attributes["data-id"] = "slidenum"
              style = "opacity: 0.75;"
              +"Slide 3"
            }
            h2 {
              attributes["data-id"] = "title"
              style = "margin-top: 500px; opacity: 0.5;"
              +"With Auto Animate 👇"
            }
            div {
              attributes["data-id"] = "1"
              style =
                "background: cyan; position: absolute; top: 50%; left: 50%; width: 400px; height: 400px; margin: -200px 0 0 -200px; border-radius: 400px;"
            }
            div {
              attributes["data-id"] = "2"
              style =
                "background: magenta; position: absolute; top: 50%; left: 50%; width: 300px; height: 300px; margin: -150px 0 0 -150px; border-radius: 400px;"
            }
            div {
              attributes["data-id"] = "3"
              style =
                "background: yellow; position: absolute; top: 50%; left: 50%; width: 200px; height: 200px; margin: -100px 0 0 -100px; border-radius: 400px;"
            }
            div {
              attributes["data-id"] = "4"
              style =
                "background: red; position: absolute; top: 50%; left: 50%; width: 100px; height: 100px; margin: -50px 0 0 -50px; border-radius: 400px;"
            }
          }
        }

        dslSlide {
          style = "height: 600px"
          autoAnimate = true

          content {
            h4 {
              attributes["data-id"] = "slidenum"
              style = "opacity: 0.75;"
              +"Slide 4"
            }
            h2 {
              attributes["data-id"] = "title"
              style = "margin-top: 300px; opacity: 0.25;"
              +"With Auto Animate"
            }
            div {
              attributes["data-id"] = "1"
              style = "background: cyan; position: absolute; top: 250px; left: 16%; width: 60px; height: 60px;"
            }
            div {
              attributes["data-id"] = "2"
              style = "background: magenta; position: absolute; top: 250px; left: 36%; width: 60px; height: 60px;"
            }
            div {
              attributes["data-id"] = "3"
              style = "background: yellow; position: absolute; top: 250px; left: 56%; width: 60px; height: 60px;"
            }
            div {
              attributes["data-id"] = "4"
              style = "background: red; position: absolute; top: 250px; left: 76%; width: 60px; height: 60px;"
            }
          }
        }
      }

      verticalSlides {
        // tabular begin
        dslSlide {
          content {
            h2 { +"Tables" }
            table {
              thead {
                headRow("Item", "Value", "Quantity")
              }
              tbody {
                bodyRow("Apples", "$1", "7")
                bodyRow("Lemonade", "$2", "18")
                // Or use the verbose form
                tr {
                  td { +"Bread" }
                  td { +"$3" }
                  td { +"2" }
                }
              }
            }
          }
        }
        // tabular end

        slideSource(slides, "tabular")
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

        slideSource(slides, "iframe")
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

        slideSource(slides, "transition")
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

        slideSource(slides, "themes")
      }

      verticalSlides {
        // video begin
        dslSlide {
          slideConfig {
            backgroundVideo = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
          }

          content {
            h1 { style = "color: red;"; +"Video Backgrounds" }
          }
        }
        // video end

        slideSource(slides, "video")
      }

      verticalSlides {

        // webcontent begin
        dslSlide {
          content {
            h2 { +"Embedded Web Content" }
            iframe {
              src = "https://slides.com/news/auto-animate/embed"
              height = "540"
              width = "700"
              style = "border:none;"
            }
          }
        }
        // webcontent end

        slideSource(slides, "webcontent")
      }

      verticalSlides {
        // other begin
        markdownSlide {
          id = "features"
          content {
            """
            ## Other Features 🚡
            
            [Layouts](/layouts.html) ${fragment()}
         
            [Fragments](/fragments.html) ${fragment()}
                        
            👇 ${fragment()}

            """
          }
        }
        // other end

        slideSource(slides, "other")
      }

      verticalSlides {
        // navigation begin
        markdownSlide {
          content {
            """
            ## Presentation Navigation 🦊 
            
            [Go to the 1st slide](#/intro) ${fragment()}
         
            [Go to the 2nd slide](#/mdslide) ${fragment()}
            
            [Go to the presentation definition slide](#/definition) ${fragment()}
            """
          }
        }
        // navigation end

        slideSource(slides, "navigation")
      }

      slideSource(slides, "readme", "Presentation Definition", id = "definition")

      slideSource("kslides-core/src/main/kotlin/com/kslides/Presentation.kt", "slideSource", "Slide Source Definition")
    }
    // readme end

    presentation {
      path = "layouts.html"

      presentationConfig {
        topRightHref = "/#/features"
        topRightTitle = "Go to main presentation"
        topRightText = "🔙"
      }

      verticalSlides {
        // layouts begin
        dslSlide {
          content {
            h2 { +"Layout Examples" }
            unorderedList(
              { listHref("#/fit-text", "Fit Text") },
              { listHref("#/stretch", "Stretch") },
              { listHref("#/stack", "Stack") },
              { listHref("#/hstack", "HStack") },
              { listHref("#/vstack", "VStack") },
            )
            p { +"👇" }
          }
        }
        // layouts end

        slideSource(slides, "layouts")
      }

      verticalSlides {
        dslSlide {
          id = "fit-text"
          content {
            h2 { +"Fit Text" }
            p { +"Resizes text to be as large as possible within its container. 👇" }
            pre {
              code("html") {
                attributes["data-trim"] = "true"
                attributes["data-line-numbers"] = "true"
                +includeFile(
                  slides,
                  "3",
                  beginToken = "fit-text1 begin",
                  endToken = "fit-text1 end",
                  enableEscape = false
                )
              }
            }
            p { +"or" }
            pre {
              code("html") {
                attributes["data-trim"] = "true"
                attributes["data-line-numbers"] = "true"
                h2("r-fit-text") { +"FIT" }
              }
            }
          }
        }

        // fit-text1 begin
        dslSlide {
          content {
            h2("r-fit-text") { +"FIT" }
          }
        }
        // fit-text1 end

        slideSource(slides, "fit-text1")

        // fit-text2 begin
        dslSlide {
          content {
            h2("r-fit-text") { +"HELLO WORLD" }
            h2("r-fit-text") { +"BOTH THESE TITLES USE FIT-TEXT" }
          }
        }
        // fit-text2 end

        slideSource(slides, "fit-text2")
      }

      verticalSlides {
        dslSlide {
          id = "stretch"
          content {
            h2 { +"Stretch" }
            p { +"Makes an element as tall as possible while remaining within the slide bounds. 👇" }
            pre {
              code("kotlin") {
                attributes["data-trim"] = "true"
                attributes["data-line-numbers"] = "true"
                +includeFile(
                  slides,
                  "3-7",
                  beginToken = "stretch begin",
                  endToken = "stretch end",
                  enableEscape = false
                )
              }
            }
            p { +"or" }
            pre {
              code("html") {
                attributes["data-trim"] = "true"
                attributes["data-line-numbers"] = "true"
                h2 { +"Stretch Example" }
                img(classes = "r-stretch") {
                  src = "images/image2.png"
                }
                p { +"Image byline" }
                p { +"👇" }
              }
            }
          }
        }

        // stretch begin
        dslSlide {
          content {
            h2 { +"Stretch Example" }
            img(classes = "r-stretch") {
              src = "images/image2.png"
            }
            p { +"Image byline" }
            p { +"👇" }
          }
        }
        // stretch end

        slideSource(slides, "stretch")
      }

      verticalSlides {
        dslSlide {
          id = "stack"
          content {
            h2 { +"Stack" }
            p { +"Stacks multiple elements on top of each other, for use with fragments. 👇" }
            pre {
              code("kotlin") {
                attributes["data-trim"] = "true"
                attributes["data-line-numbers"] = "true"
                +includeFile(
                  slides,
                  "3-21",
                  beginToken = "stack begin",
                  endToken = "stack end",
                  enableEscape = false
                )
              }
            }
          }
        }

        // stack begin
        dslSlide {
          content {
            h2 { +"Stack Example" }
            div("r-stack") {
              p("fragment fade-in-then-out") { +"One" }
              p("fragment fade-in-then-out") { +"Two" }
              p("fragment fade-in-then-out") { +"Three" }
              p("fragment fade-in-then-out") { +"Four" }
            }
            div("r-stack") {
              val kitten = "https://placekitten.com"
              img(classes = "fragment") {
                src = "$kitten/450/300"; width = "450"; height = "300"
              }
              img(classes = "fragment") {
                src = "$kitten/300/450"; width = "300"; height = "450"
              }
              img(classes = "fragment") {
                src = "$kitten/400/400"; width = "400"; height = "400"
              }
            }
          }
        }
        // stack end

        slideSource(slides, "stack")
      }

      verticalSlides {
        dslSlide {
          id = "hstack"
          content {
            h2 { +"HStack 👇" }
            p { +"Stacks multiple elements horizontally." }
            pre {
              code("kotlin") {
                attributes["data-trim"] = "true"
                attributes["data-line-numbers"] = "true"
                +includeFile(
                  slides,
                  "3-8",
                  beginToken = "hstack begin",
                  endToken = "hstack end",
                  enableEscape = false
                )
              }
            }
          }
        }

        // hstack begin
        dslSlide {
          content {
            h2 { +"HStack Example 👇" }
            div("r-hstack") {
              p { +"One"; style = "padding: 0.50em; background: #eee; margin: 0.25em" }
              p { +"Two"; style = "padding: 0.75em; background: #eee; margin: 0.25em" }
              p { +"Three"; style = "padding: 1.00em; background: #eee; margin: 0.25em" }
            }
          }
        }
        // hstack end

        slideSource(slides, "hstack")
      }

      verticalSlides {
        dslSlide {
          id = "vstack"
          content {
            h2 { +"VStack 👇" }
            p { +"Stacks multiple elements vertically." }
            pre {
              code("kotlin") {
                attributes["data-trim"] = "true"
                attributes["data-line-numbers"] = "true"
                +includeFile(
                  slides,
                  "3-8",
                  beginToken = "vstack begin",
                  endToken = "vstack end",
                  enableEscape = false
                )
              }
            }
          }
        }

        // vstack begin
        dslSlide {
          content {
            h2 { +"VStack Example 👇" }
            div("r-vstack") {
              p { +"One"; style = "padding: 0.50em; background: #eee; margin: 0.25em" }
              p { +"Two"; style = "padding: 0.75em; background: #eee; margin: 0.25em" }
              p { +"Three"; style = "padding: 1.00em; background: #eee; margin: 0.25em" }
            }
          }
        }
        // vstack end

        slideSource(slides, "vstack")
      }
    }

    presentation {
      path = "fragments.html"

      presentationConfig {
        topRightHref = "/#/features"
        topRightTitle = "Go to main presentation"
        topRightText = "🔙"
      }

      verticalSlides {
        // fragment-styles begin
        dslSlide {
          content {
            h2 { +"Fragment Styles" }
            p { +"There are different types of fragments, like: 👇" }
            p("fragment grow") { +"grow" }
            p("fragment shrink") { +"shrink" }
            p("fragment fade-out") { +"fade-out" }
            p {
              span("fragment fade-right") { style = "display: inline-block;"; +"fade-right," }
              +" "
              span("fragment fade-up") { style = "display: inline-block;"; +"fade-up," }
              +" "
              span("fragment fade-down") { style = "display: inline-block;"; +"fade-down, " }
              +" "
              span("fragment fade-left") { style = "display: inline-block;"; +"fade-left" }
            }
            p("fragment fade-in-then-out") { +"fade-in-then-out" }
            p("fragment fade-in-then-semi-out") { +"fade-in-then-semi-out" }
            p {
              +"Highlight "
              span("fragment highlight-red") { +"red " }
              span("fragment highlight-blue") { +"blue " }
              span("fragment highlight-green") { +"green " }
            }
          }
        }
        // fragment-styles end

        slideSource(slides, "fragment-styles")
      }

      verticalSlides {

        // fragment-md begin
        markdownSlide {
          content {
            """
            ## Markdown Slide with Fragments

            highlight-red ${fragment(Effect.HIGHLIGHT_RED)}
            
            fade-in-then-semi-out ${fragment(Effect.FADE_IN_THEN_SEMI_OUT)}
            
            fade-left ${fragment(Effect.FADE_LEFT)}

            fade-right ${fragment(Effect.FADE_RIGHT)}
            
            fade-up 👇 ${fragment(Effect.FADE_UP)}
           
            """
          }
        }
        // fragment-md end

        slideSource(slides, "fragment-md")
      }
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

        // slideConfig values override the presentationDefault slideConfig values
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

          // slideConfig values override the presentationConfig slideConfig values
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
}