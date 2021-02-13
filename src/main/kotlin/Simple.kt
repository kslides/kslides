import com.github.pambrose.Presentation.Companion.present
import com.github.pambrose.presentation
import kotlinx.html.h1
import kotlinx.html.p

fun main() {
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

    present()
}
