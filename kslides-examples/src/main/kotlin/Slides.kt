import com.kslides.Effect
import com.kslides.PlaygroundMode
import com.kslides.PlaygroundTheme
import com.kslides.PresentationTheme
import com.kslides.Speed
import com.kslides.TargetPlatform
import com.kslides.Transition
import com.kslides.atag
import com.kslides.bodyRow
import com.kslides.by
import com.kslides.codeSnippet
import com.kslides.config.PlotlyIframeConfig
import com.kslides.fragment
import com.kslides.headRow
import com.kslides.include
import com.kslides.kslides
import com.kslides.listHref
import com.kslides.orderedList
import com.kslides.permuteBy
import com.kslides.playground
import com.kslides.plotly
import com.kslides.rawHtml
import com.kslides.toLinePatterns
import com.kslides.unorderedList
import kotlinx.css.*
import kotlinx.css.Float
import kotlinx.css.properties.*
import kotlinx.html.*
import space.kscience.dataforge.meta.configure
import space.kscience.dataforge.values.Value
import space.kscience.dataforge.values.asValue
import space.kscience.plotly.PlotlyConfig
import space.kscience.plotly.heatmap
import space.kscience.plotly.histogram
import space.kscience.plotly.layout
import space.kscience.plotly.palettes.T10
import space.kscience.plotly.scatter
import space.kscience.plotly.trace
import kotlin.collections.set
import kotlin.random.Random

fun main() {
  kslides {
    // Optional
    kslidesConfig {
      // kslides configuration options
    }

    // Optional
    output {
      enableFileSystem = true
      enableHttp = true
    }

    // Optional
    css {
      rule(".reveal h4") {
        color = Color("#586E75")
      }
    }
    // Or use the string version
    css += """
      .reveal.h5 {
        color: blue;
      }
    """

    // Optional
    presentationConfig {
      //width = "100%"
      //height = 700

      // Default config values for all presentations
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
      theme = PresentationTheme.SOLARIZED
      center = true
      enableMermaid = true

      menuConfig {
        numbers = true
      }

      copyCodeConfig {
        timeout = 2000
        copy = "Copy"
        copied = "Copied!"
      }

      playgroundConfig {
        theme = PlaygroundTheme.DARCULA
        lines = true
        style = "border:none;"
        width = "100%"
        height = "350px"

        // Increase the default playground font size
        css += """
          .CodeMirror pre {
            line-height: 25px;
          }

          .CodeMirror {
            font-size: 20px;
          }

          .code-output {
            line-height: 25px;
            font-size: 20px;
          }
        """
      }

      slideConfig {}
    }
    // playgroundConfig end

    val slides = "kslides-examples/src/main/kotlin/Slides.kt"

    presentation {
      path = "/"

      // Optional
      presentationConfig {
        // presentation-specific configurations
        menuConfig {}
        copyCodeConfig {}
        playgroundConfig {}
        slideConfig {}
        plotlyIframeConfig {}
      }

      css +=
        """
        #ghsrc {
          font-size: 30px;
          text-decoration: underline;
        }  
        img[alt=slideimg] { 
          width: 150px; 
        }
        """
      // Or use the Kotlin CSS DSL. Instead of #intro h1 { color: #FF5533; } use:
      css {
        rule("#intro h1") {
          color = Color("#FF5533")
        }
      }

      verticalSlides {
        // intro begin
        markdownSlide {
          id = "intro"
          content {
            """
            # kslides
            ![slideimg](images/slide-transparent.png)

            ### A Kotlin DSL wrapper for [reveal.js](https://revealjs.com)
            ### 👇
            Notes: This is a note for the opening slide 📝
            """
          }
        }
        // intro end

        slideDefinition(slides, "intro")
      }

      css +=
        """
        #markdownslide p { 
          color: #FF6836; 
        }
        """

      verticalSlides {
        // mdslide begin
        markdownSlide {
          id = "markdownslide"
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

        slideDefinition(slides, "mdslide")
      }

      css {
        rule("#htmlslide p") {
          color = Color("blue")
        }
      }

      verticalSlides {
        // htmlslide begin
        htmlSlide {
          id = "htmlslide"
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

        slideDefinition(slides, "htmlslide")
      }

      verticalSlides {
        // dslslide begin
        dslSlide {
          content {
            h1 { +"An HTML DSL Slide" }
            h2 { +"👀" }
            p("fragment fade-right") { +"Press CTRL+Shift+F to search all the slides" }
            p("fragment fade-right") { +"Press Alt+click to zoom in on elements" }
            p("fragment fade-right") { +"Press the down arrow to see the slide definition 👇" }
            notes { +"This is a note for the DSL slide 📝" }
          }
        }
        // dslslide end

        slideDefinition(slides, "dslslide")
      }

      verticalSlides {
        // highlights1 begin
        markdownSlide {
          content {
            """
            ## Highlighted Code with a markdownSlide   
            ```kotlin [|3,7|4,6|5|4-6]
            ${include("kslides-examples/src/main/kotlin/content/HelloWorldK.kt")}
            ```
            ### 👇 
            Note: This slide shows code highlights. You can specify the lines you want to highlight.
            """
          }
        }
        // highlights1 end

        slideDefinition(slides, "highlights1")
      }

      verticalSlides {
        // highlights2 begin
        dslSlide {
          content {
            h2 { +"Highlighted Code with a dslSlide" }
            codeSnippet {
              language = "kotlin"
              highlightPattern = "[|3,7|4,6|5|4-6]"
              +include("kslides-examples/src/main/kotlin/content/HelloWorldK.kt")
            }
            h3 { +"👇" }
            aside("notes") {
              +"This slide shows highlighted code. You can specify the lines you want to highlight."
            }
          }
        }
        // highlights2 end

        slideDefinition(slides, "highlights2")
      }

      verticalSlides {
        // animated1 begin
        // A for loop generates a series of slides, each with a different set of lines
        // Uses the same line number syntax used by revealjs: https://revealjs.com/code/
        for (linePattern in "[5,6,9|5-9|]".toLinePatterns())
          dslSlide {
            autoAnimate = true
            content {
              h2 { +"Animated Code with a dslSlide" }
              codeSnippet {
                language = "javascript"
                dataId = "code-animation"
                +include("kslides-examples/src/main/kotlin/content/assign.js", linePattern)
              }
              h3 { +"👇" }
              aside("notes") {
                +"This slide shows animated code highlights."
              }
            }
          }
        // animated1 end

        slideDefinition(slides, "animated1")
      }

      verticalSlides {
        // animated2 begin
        // A for loop generates a series of slides, each with a different set of lines
        // Uses the same line number syntax used by revealjs: https://revealjs.com/code/
        for (linePattern in "[5,6,9|5-9|]".toLinePatterns())
          htmlSlide {
            autoAnimate = true
            content {
              """
              <h2>Animated Code with an htmlSlide</h2>
              <pre data-id="code-animation" data-cc="false">
                <code class="javascript" data-trim="" data-line-numbers="">
                  ${include("kslides-examples/src/main/kotlin/content/assign.js", linePattern)}
                </code>
              </pre>
              <h3>👇</h3>
              <aside class="notes">
              This slide shows animated code highlights.
              </aside>
              """
            }
          }
        // animated2 end

        slideDefinition(slides, "animated2")
      }

      verticalSlides {
        // pg1 begin
        val pg = "kslides-examples/src/main/kotlin/playground"
        dslSlide {
          id = "playground"
          content {
            h2 { +"Kotlin Playground Support" }
            playground("$pg/HelloWorld.kt") {
              args = "1 2 3"
            }
          }
        }
        // pg1 end

        slideDefinition(slides, "pg1")

        // pg2 begin
        dslSlide {
          content {
            h2 { +"Playground with Additional Code" }
            playground("$pg/HelloPets.kt", "$pg/Cat.kt", "$pg/Dog.kt") {
              theme = PlaygroundTheme.IDEA
            }
          }
        }
        // pg2 end

        slideDefinition(slides, "pg2")

        // pg3 begin
        dslSlide {
          content {
            h2 { +"Playground Using JUnit" }
            small {
              +"( Replace TODO() with: "
              em { +"it%2==0" }
              +" )"
            }
            playground("$pg/TestLambdas.kt") {
              height = "450px"
              dataTargetPlatform = TargetPlatform.JUNIT
            }
          }
        }
        // pg3 end

        slideDefinition(slides, "pg3")

        // pg4 begin
        dslSlide {
          content {
            h2 { +"Playground Using Kotlin/JS" }
            playground("$pg/JsPlayground.txt") {
              theme = PlaygroundTheme.IDEA
              dataTargetPlatform = TargetPlatform.JS
              dataJsLibs = "https://unpkg.com/moment@2"
            }
          }
        }
        // pg4 end

        slideDefinition(slides, "pg4")

        // pg5 begin
        dslSlide {
          content {
            h2 { +"Playground Support for other Languages" }
            playground("kslides-examples/src/main/kotlin/content/helloworld.html") {
              //style = "border: 2px solid #586E75;"
              height = "375px"
              theme = PlaygroundTheme.DARCULA
              mode = PlaygroundMode.XML
              // Decrease the font size for just this playground
              css {
                rule(".CodeMirror pre") {
                  lineHeight = LineHeight("20px")
                }

                rule(".CodeMirror") {
                  fontSize = LinearDimension("15px")
                }

                rule(".code-output") {
                  lineHeight = LineHeight("20px")
                  fontSize = LinearDimension("15px")
                }
              }
            }
            +"Read-only languages include: JS, Java, Groovy, XML/HTML, C, Shell, Swift, Obj-C"
          }
        }
        // pg5 end

        slideDefinition(slides, "pg5")
      }

      verticalSlides {

        dslSlide {
          content {
            h2 {
              atag("Mermaid Support 👇", "https://mermaid-js.github.io/mermaid/#/")
            }
          }
        }

        // mermaid1 begin
        dslSlide {
          content {
            h2 {
              +"Mermaid "
              atag("Flowchart", "https://mermaid-js.github.io/mermaid/#/flowchart")
            }
            mermaid(
              """
              flowchart TD
                  A[Start] --> B{Is it?}
                  B -->|Yes| C[OK]
                  C --> D[Rethink]
                  D --> B
                  B ---->|No| E[End]
            """
            )
          }
        }
        // mermaid1 end

        slideDefinition(slides, "mermaid1")

        // mermaid2 begin
        dslSlide {
          content {
            h2 {
              +"Mermaid "
              atag("Pie Charts", "https://mermaid-js.github.io/mermaid/#/pie")
            }
            div("mermaid") {
              +"""
                pie title Pets adopted by volunteers
                    "Dogs" : 386
                    "Cats" : 85
                    "Rats" : 15
              """
            }
          }
        }
        // mermaid2 end

        slideDefinition(slides, "mermaid2")

        // mermaid3 begin
        dslSlide {
          content {
            h2 {
              +"Mermaid "
              atag("Sequence Diagram", "https://mermaid-js.github.io/mermaid/#/sequenceDiagram")
            }
            mermaid(
              """
              sequenceDiagram
                  Alice->>John: Hello John, how are you?
                  John-->>Alice: Great!
                  Alice-)John: See you later!
            """
            )
          }
        }
        // mermaid3 end

        slideDefinition(slides, "mermaid3")
      }

      verticalSlides {
        // plotly1 begin
        dslSlide {
          id = "plotly"
          content {
            h2 { +"A plotly-kt Plot" }
            plotly(
              dimensions = 801 by 400,
              iframeConfig = PlotlyIframeConfig {
                style = "width: 85%; border: 2px solid #586E75;"
                height = "415px"
              },
              plotlyConfig = PlotlyConfig { withEditorButton() }
            ) {
              layout {
                title = "A Simple Random Plot"
                xaxis.title = "x Axis Title"
                yaxis.title = "y Axis Title"
              }
              scatter {
                x.numbers = 0..400
                y.numbers = x.numbers.map { Random.nextDouble(10.0) }
              }
            }
          }
        }
        // plotly1 end

        slideDefinition(slides, "plotly1")

        // plotly2 begin
        dslSlide {
          content {
            h2 { +"A plotly-kt Histogram" }
            plotly(
              dimensions = 608 by 484,
              iframeConfig = PlotlyIframeConfig {
                style = "width: 65%; border: 2px solid #586E75;"
                height = "500px"
              },
              plotlyConfig = PlotlyConfig { withEditorButton() }
            ) {
              layout {
                title = "Horizontal Histogram"
                bargap = 0.1
                xaxis {
                  title = "Count"
                }
                yaxis {
                  title = "Value"
                  ticklen = 3
                  tickcolor("#FFF")
                }
              }

              histogram {
                y.set(listOf(1, 2, 2, 3, 2, 1, 4, 4))
                marker {
                  colors(listOf(T10.RED, T10.GREEN, T10.ORANGE, T10.BLUE))
                }
              }
            }
          }
        }
        // plotly2 end

        slideDefinition(slides, "plotly2")

        // plotly3 begin
        dslSlide {
          content {
            h2 { +"A plotly-kt 3D Surface Plot" }
            plotly(
              dimensions = 503 by 484,
              iframeConfig = PlotlyIframeConfig {
                style = "width: 54%; border: 2px solid #586E75;"
                height = "500px"
              },
              plotlyConfig = PlotlyConfig { withEditorButton() }
            ) {
              layout {
                title = "A 3D Surface Plot"
              }
              fun l(vararg numbers: Number) = numbers.map { it.asValue() }.asValue()
              trace {
                z.value = listOf(
                  l(8.83, 8.89, 8.81, 8.87, 8.9, 8.87),
                  l(8.89, 8.94, 8.85, 8.94, 8.96, 8.92),
                  l(8.84, 8.9, 8.82, 8.92, 8.93, 8.91),
                  l(8.79, 8.85, 8.79, 8.9, 8.94, 8.92),
                  l(8.79, 8.88, 8.81, 8.9, 8.95, 8.92),
                  l(8.8, 8.82, 8.78, 8.91, 8.94, 8.92),
                  l(8.75, 8.78, 8.77, 8.91, 8.95, 8.92),
                  l(8.8, 8.8, 8.77, 8.91, 8.95, 8.94),
                  l(8.74, 8.81, 8.76, 8.93, 8.98, 8.99),
                  l(8.89, 8.99, 8.92, 9.1, 9.13, 9.11),
                  l(8.97, 8.97, 8.91, 9.09, 9.11, 9.11),
                  l(9.04, 9.08, 9.05, 9.25, 9.28, 9.27),
                  l(9, 9.01, 9, 9.2, 9.23, 9.2),
                  l(8.99, 8.99, 8.98, 9.18, 9.2, 9.19),
                  l(8.93, 8.97, 8.97, 9.18, 9.2, 9.18)
                ).asValue()
                configure {
                  "type" put "surface"
                }
              }
            }
          }
        }
        // plotly3 end

        slideDefinition(slides, "plotly3")

        // plotly4 begin
        dslSlide {
          content {
            h2 { +"A plotly-kt 3D Scatter Plot" }
            plotly(
              dimensions = 503 by 484,
              iframeConfig = PlotlyIframeConfig {
                style = "width: 54%; border: 2px solid #586E75;"
                height = "500px"
              },
              plotlyConfig = PlotlyConfig { withEditorButton() }
            ) {
              layout {
                title = "A 3D Scatter Plot"
              }
              trace {
                configure {
                  "type" put "scatter3d"
                }
                x(1, 2, 3)
                y(1, 2, 3)
                z(1, 2, 3)
              }
            }
          }
        }
        // plotly4 end

        slideDefinition(slides, "plotly4")

        // plotly5 begin
        dslSlide {
          content {
            h2 { +"A plotly-kt Heatmap" }
            plotly(
              dimensions = 503 by 484,
              iframeConfig = PlotlyIframeConfig {
                style = "width: 54%; border: 2px solid #586E75;"
                height = "500px"
              },
              plotlyConfig = PlotlyConfig { withEditorButton() }
            ) {
              layout {
                title = "Red Heatmap"
              }
              heatmap {
                x.set(listOf(1, 2, 3, 4, 5))
                y.set(listOf(6, 7, 8, 9, 10))
                z.set((1..25).chunked(5))
                colorscale = Value.of("Reds")
              }
            }
          }
        }
        // plotly5 end

        slideDefinition(slides, "plotly5")
      }

      verticalSlides {
        // swapping begin
        listOf("One", "Two", "Three", "Four", "👇")
          .permuteBy(
            listOf(0, 1, 4),
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
                h2 { +"Animated List Items" }
                unorderedList(*items.toTypedArray())
              }
            }
          }
        // swapping end

        slideDefinition(slides, "swapping")
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
        // youtube begin
        dslSlide {
          id = "youtube"
          content {
            h2 { +"YouTube Content" }
            iframe {
              width = "560"
              height = "315"
              src = "https://www.youtube.com/embed/X8Az9X0mwUE?start=163"
              title = "YouTube video player"
              attributes["frameborder"] = "0"
              attributes["allow"] = "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope"
              attributes["allowfullscreen"] = "true"
            }
          }
        }
        // youtube end

        slideDefinition(slides, "youtube")
      }

      verticalSlides {
        // twitter begin
        dslSlide {
          id = "twitter"
          content {
            h2 { +"Twitter Content" }
            // tw-align-center added to center tweet on slide
            blockQuote(classes = "twitter-tweet tw-align-center") {
              style = ""
              p {
                lang = "en"
                dir = Dir.ltr
                +"Procedural programming is good."
                +"Object oriented programming is good."
                +"Functional programming is good."
                +"Programming with all three is best."
              }
              +"&mdash; Uncle Bob Martin (@unclebobmartin)"
              a {
                href = "https://twitter.com/unclebobmartin/status/1522904306948657160"
                +"May 7, 2022"
              }
            }
            script {
              async = true
              src = "https://platform.twitter.com/widgets.js"
              charset = "utf-8"
            }
          }
        }
        // twitter end

        slideDefinition(slides, "twitter")
      }

      css +=
        """
        #tables th {
          color: red; 
          border-bottom-color: #586E75;
        }
        """

      verticalSlides {
        // tabular begin
        dslSlide {
          id = "tables"
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

        slideDefinition(slides, "tabular")
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

        slideDefinition(slides, "iframe")
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
                  a { href = "?transition=${transition.name.lowercase()}#/transitions"; +transition.name }
                  if (index < Transition.values().size - 1)
                    +"-"
                  rawHtml("\n\t\t\t\t\t\t\t")
                }
            }
          }
        }
        // transition end

        slideDefinition(slides, "transition")
      }

      verticalSlides {
        // themes begin
        dslSlide {
          id = "themes"
          content {
            // Hack to swap themes after the page has loaded. Not flexible and only intended for the reveal.js demo deck.
            h2 { +"Themes" }
            p {
              +"reveal.js comes with some built-in themes:"
              br {}
              // The Theme enum includes all the built in themes
              PresentationTheme.values()
                .forEachIndexed { index, theme ->
                  a {
                    href = "#/themes"
                    onClick =
                      "document.getElementById('theme').setAttribute('href','revealjs/${theme.cssSrc}'); return false;"
                    +theme.name
                  }
                  if (index < PresentationTheme.values().size - 1)
                    +"-"
                  rawHtml("\n\t\t\t\t\t\t\t")
                }
            }
          }
        }
        // themes end

        slideDefinition(slides, "themes")
      }

      verticalSlides {
        // video1 begin
        dslSlide {
          slideConfig {
            backgroundVideo = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
          }

          content {
            h1 { +"Video Backgrounds"; style = "color: red;" }
          }
        }
        // video1 end

        slideDefinition(slides, "video1")
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

        slideDefinition(slides, "webcontent")
      }

      verticalSlides {
        // other begin
        markdownSlide {
          id = "features"
          content {
            """
            ## Other Features 🚡
            
            [Layouts](/layouts.html) 
         
            [Fragments](/fragments.html) 
                        
            [Backgrounds](/backgrounds.html) 

            [Multi-Columns DSL Slides](/multicols.html) 

            [Multi-Slide Markdown Slides](/multislide.html) 

            👇 ${fragment()}

            """
          }
        }
        // other end

        slideDefinition(slides, "other")
      }

      verticalSlides {
        // navigation begin
        markdownSlide {
          content {
            """
            ## Presentation Navigation 🦊 
            
            [Go to the previous slide](#/features) ${fragment()}
         
            [Go to the next slide](#/lastslide) ${fragment()}
            
            [Go to the presentation source on GitHub](https://github.com/kslides/kslides/blob/master/kslides-examples/src/main/kotlin/Slides.kt) ${fragment()}
            """
          }
        }
        // navigation end

        slideDefinition(slides, "navigation")
      }

//      verticalSlides {
//        // slidedef begin
//        slideDefinition(
//          "kslides-core/src/main/kotlin/com/kslides/Presentation.kt",
//          "slideDefinition",
//          title = "Slide Definition Source",
//          id = "lastslide"
//        )
//        // slidedef end
//
//        slideDefinition(slides, "slidedef")
//      }
    }
// presentation end

    presentation {
      path = "layouts.html"

      presentationConfig {
        topRightHref = "/#/features"
        topRightTitle = "Go back to main presentation"
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

        slideDefinition(slides, "layouts")
      }

      verticalSlides {
        dslSlide {
          id = "fit-text"
          content {
            h2 { +"Fit Text" }
            p { +"Resizes text to be as large as possible within its container. 👇" }
            codeSnippet {
              language = "kotlin"
              highlightPattern = "none"
              copyButton = false
              +include(slides, "3", beginToken = "ft1 begin", endToken = "ft1 end")
            }
            p { +"or" }
            pre {
              attributes["data-cc"] = "false"
              code("html") {
                attributes["data-trim"] = ""
                h2("r-fit-text") { +"FIT" }
              }
            }
          }
        }

        // ft1 begin
        dslSlide {
          content {
            h2("r-fit-text") { +"FIT" }
          }
        }
        // ft1 end

        slideDefinition(slides, "ft1")

        // fit-text2 begin
        dslSlide {
          content {
            h2("r-fit-text") { +"HELLO WORLD" }
            h2("r-fit-text") { +"BOTH THESE TITLES USE FIT-TEXT" }
          }
        }
        // fit-text2 end

        slideDefinition(slides, "fit-text2")
      }

      verticalSlides {
        dslSlide {
          id = "stretch"
          content {
            h2 { +"Stretch" }
            p { +"Makes an element as tall as possible while remaining within the slide bounds. 👇" }
            codeSnippet {
              language = "kotlin"
              highlightPattern = "none"
              copyButton = false
              +include(slides, "3-7", beginToken = "stretch begin", endToken = "stretch end")
            }
            p { +"or" }
            pre {
              attributes["data-cc"] = "false"
              code("html") {
                attributes["data-trim"] = ""
                h2 { +"Stretch Example" }
                img(classes = "r-stretch") {
                  src = "revealjs/assets/image2.png"
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
              src = "revealjs/assets/image2.png"
            }
            p { +"Image byline" }
            p { +"👇" }
          }
        }
        // stretch end

        slideDefinition(slides, "stretch")
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
                +include(slides, "3-21", beginToken = "stack1 begin", endToken = "stack1 end")
              }
            }
          }
        }

        // stack1 begin
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
        // stack1 end

        slideDefinition(slides, "stack1")
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
                +include(slides, "3-8", beginToken = "hstack begin", endToken = "hstack end")
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

        slideDefinition(slides, "hstack")
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
                +include(slides, "3-8", beginToken = "vstack begin", endToken = "vstack end")
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

        slideDefinition(slides, "vstack")
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

        slideDefinition(slides, "fragment-styles")
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

        slideDefinition(slides, "fragment-md")
      }
    }

    presentation {
      path = "backgrounds.html"

      presentationConfig {
        topRightHref = "/#/features"
        topRightTitle = "Go to main presentation"
        topRightText = "🔙"

        theme = PresentationTheme.SERIF
      }

      verticalSlides {
        // background1 begin
        dslSlide {
          slideConfig {
            background = "#00ffff"
          }
          content {
            h2 { +"data-background: #00ffff" }
          }
        }
        // background1 end

        slideDefinition(slides, "background1")
      }

      verticalSlides {
        // background2 begin
        dslSlide {
          slideConfig {
            background = "#bb00bb"
          }
          content {
            h2 { +"data-background: #bb00bb" }
          }
        }
        // background2 end

        slideDefinition(slides, "background2")
      }

      verticalSlides {
        // background3 begin
        dslSlide {
          slideConfig {
            backgroundColor = "lightblue"
          }
          content {
            h2 { +"data-background-color: lightblue" }
          }
        }
        // background3 end

        slideDefinition(slides, "background3")
      }

      verticalSlides {
        // background4 begin
        dslSlide {
          slideConfig {
            background = "#ff0000"
          }
          content {
            h2 { +"data-background: #ff0000" }
          }
        }

        dslSlide {
          slideConfig {
            background = "rgba(0, 0, 0, 0.2)"
          }
          content {
            h2 { +"data-background: rgba(0, 0, 0, 0.2)" }
          }
        }

        dslSlide {
          slideConfig {
            background = "salmon"
          }
          content {
            h2 { +"data-background: salmon" }
          }
        }
        // background4 end

        slideDefinition(slides, "background4")
      }

      // vertical-config begin
      verticalSlides {
        slideConfig {
          background = "rgba(0, 100, 100, 0.2)"
        }

        dslSlide {
          content {
            h2 { +"Background applied to stack (1/2)" }
          }
        }

        dslSlide {
          content {
            h2 { +"Background applied to stack (2/2)" }
          }
        }

        dslSlide {
          slideConfig {
            background = "rgb(66, 66, 66)"
          }
          content {
            h2 { +"Background applied to slide inside of stack" }
          }
        }

        slideDefinition(slides, "vertical-config")
      }
      // vertical-config end

      verticalSlides {
        // background-image begin
        dslSlide {
          slideConfig {
            backgroundTransition = Transition.SLIDE
            background = "revealjs/assets/image1.png"
          }
          content {
            h2 { +"Background image" }
          }
        }
        // background-image end

        slideDefinition(slides, "background-image")
      }

      verticalSlides {
        // repeat begin
        dslSlide {
          slideConfig {
            background = "revealjs/assets/image2.png"
            backgroundSize = "100px"
            backgroundRepeat = "repeat"
            backgroundColor = "#111"
          }
          content {
            h2 { +"Background repeat" }
          }
        }
        // repeat end

        slideDefinition(slides, "repeat")
      }

      verticalSlides {
        // background-video begin
        dslSlide {
          slideConfig {
            val aws = "https://s3.amazonaws.com/static.slid.es/site/homepage/v1"
            backgroundVideo = "$aws/homepage-video-editor.mp4,$aws/homepage-video-editor.webm"
          }
          content {
            h2 { +"Video background" }
          }
        }
        // background-video end

        slideDefinition(slides, "background-video")
      }

      verticalSlides {
        // background-iframe begin
        dslSlide {
          slideConfig {
            backgroundIframe =
              "https://slides.com/news/make-better-presentations/embed?style=hidden&autoSlide=4000"
          }
          content {
            h2 { +"Iframe background" }
          }
        }
        // background-iframe end
        slideDefinition(slides, "background-iframe")
      }
    }

    presentation {
      path = "multicols.html"

      presentationConfig {
        topRightHref = "/#/features"
        topRightTitle = "Go to main presentation"
        topRightText = "🔙"
      }

      css {
        /* Clear floats after the columns */
        rule(".multiColumn2:after") {
          content = QuotedString("")
          display = Display.table
          clear = Clear.both
        }

        rule(".column2") {
          float = Float.left
          width = LinearDimension("50%")
        }

        rule(".column2 li") {
          marginBottom = LinearDimension("10px")
        }
      }

      /*
        /* Clear floats after the columns */
        .multiColumn2:after {
          content: "";
          display: table;
          clear: both;
        }

        .column2 {
          float: left;
          width: 50%;
        }

        .column2 li {
          margin-bottom:10px;
        }

       */
      verticalSlides {
        // 2col begin
        dslSlide {
          content {
            h2 { +"Two Column Slide"; style = "margin-bottom:20px;" }
            div("multiColumn2") {
              val fmt = "font-size:30px; padding-top:10px;"
              div("column2") {
                p { +"Header 1"; style = "color: red;" }
                unorderedList("Item 1", "Item 2", "Item 3", "Item 4") { style = "$fmt list-style-type:circle;" }
              }
              div("column2") {
                p { +"Header 2"; style = "color: red;" }
                orderedList("Item 5", "Item 6", "Item 7", "Item 8") { style = fmt }
              }
            }
          }
        }
        // 2col end

        slideDefinition(slides, "2col")
      }

      css += """
        /* Clear floats after the columns */
        .multiColumn3:after {
          content: "";
          display: table;
          clear: both;
        }

        .column3 {
          float: left;
          width: 33%;
        }
        
        .column3 ul {
          font-size:30px; 
          list-style-type:square;
        }
        
        .column3 li {
          margin-bottom:10px;
        }               
      """

      verticalSlides {
        // 3col begin
        dslSlide {
          content {
            h2 { +"Three Column Slide" }
            div("multiColumn3") {
              div("column3") {
                p { +"Header 1"; style = "color: blue;" }
                unorderedList("Item 1", "Item 2", "Item 3", "Item 4", "Item 5")
              }
              div("column3") {
                p { +"Header 2"; style = "color: blue;" }
                val col2Items = List(5) { "Item ${it + 6}" }
                unorderedList(*col2Items.toTypedArray())
              }
              div("column3") {
                p { +"Header 3"; style = "color: blue;" }
                val col3Items = List(5) { "Item ${it + 11}" }
                unorderedList(*col3Items.toTypedArray())
              }
            }
          }
        }
        // 3col end

        slideDefinition(slides, "3col")
      }
    }

    presentation {
      path = "multislide.html"

      presentationConfig {
        topRightHref = "/#/features"
        topRightTitle = "Go back to main presentation"
        topRightText = "🔙"
      }

      // hmultislide begin
      markdownSlide {
        content {
          """
            ## This is a multi-slide Markdown Slide
            
            This is page 1 of 3
            
            ---
      
            ## This is a multi-slide Markdown Slide
            
            This is page 2 of 3
      
            ---
      
            ## This is a multi-slide Markdown Slide
            
            This is page 3 of 3
            """
        }
      }
      // hmultislide end

      slideDefinition(slides, "hmultislide")

      // vmultislide begin
      verticalSlides {
        markdownSlide {
          content {
            """
            ## This is a multi-slide Markdown Slide
            ### embedded in a verticalSlides{} block
            
            This is page 1 of 3
            
            ---
      
            ## This is a multi-slide Markdown Slide
            ### embedded in a verticalSlides{} block
            
            This is page 2 of 3
      
            ---
      
            ## This is a multi-slide Markdown Slide
            ### embedded in a verticalSlides{} block
            
            This is page 3 of 3
            """
          }
        }
      }
      // vmultislide end

      slideDefinition(slides, "vmultislide")
    }
  }
// kslides end
}