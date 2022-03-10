import com.github.pambrose.*
import com.github.pambrose.Presentation.Companion.present
import com.github.pambrose.SlideConfig.Companion.slideConfig
import com.github.pambrose.Speed.Slow
import com.github.pambrose.Transition.Slide
import com.github.pambrose.Transition.Zoom
import kotlinx.html.*

fun main() {
    presentation {
        htmlSlide {
            h1 { +"HTML Slide 🐦" }
            p { +"Use the arrow keys to navigate" }
        }

        markdownSlide(
            config = slideConfig { transition(Zoom, Slow) },
            content = """
                # Markdown Slide 🍒 
                
                Press ESC to see presentation overview.
            """
        )

        markdownSlide(
            config = slideConfig { backgroundColor = "#4370A5" },
            content = """
                # Code Highlights    
                ```kotlin [1|2,5|3-4]
                fun main() {
                    repeat(10) {
                        println("Hello")
                        println("World")
                    }
                }
                ```
            """
        )

        verticalSlides {
            htmlSlide(slideConfig { backgroundVideo = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4" }) {
                h1 {
                    style = "color: red;"
                    +"Vertical HTML Slide 👇"
                }
            }

            markdownSlide(
                """
                    # Vertical Markdown Slide 🦊 
                    
                    [Go back to the 1st slide](#/0) ${fragmentIndex(1)}
                 
                    [Go back to the 2nd slide](#/1) ${fragmentIndex(2)}
                 """
            )
        }

        config {
            history = true
            transition = Slide
            transitionSpeed = Slow
        }
    }

    // Run the web server
    present()
}