import com.github.pambrose.Presentation.Companion.present
import com.github.pambrose.Speed.Slow
import com.github.pambrose.Transition.Zoom
import com.github.pambrose.presentation
import kotlinx.html.h1
import kotlinx.html.p
import kotlinx.html.style

fun main() {
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
            htmlSlide(backgroundVideo = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4") {
                h1 {
                    style = "color: red;"
                    +"Vertical HTML Slide 👇"
                }
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

    // Run web server
    present()
}
