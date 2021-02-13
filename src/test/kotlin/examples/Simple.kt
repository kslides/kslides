import com.github.pambrose.Presentation.Companion.present
import com.github.pambrose.presentation
import kotlinx.html.h1

fun main() {
    presentation {
        htmlSlide {
            h1 { +"HTML Slide 🐦" }
        }

        markdownSlide {
            +"# Markdown Slide 🍒"
        }

        verticalSlides {
            htmlSlide {
                h1 { +"Vertical HTML Slide 🚗" }
            }

            markdownSlide {
                +"# Vertical Markdown Slide 🦊"
            }
        }
    }

    present()
}
