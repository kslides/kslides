package com.github.pambrose

import com.github.pambrose.SlideDeck.Companion.showSlides
import kotlinx.html.a
import kotlinx.html.h3
import kotlinx.html.h4
import kotlinx.html.section

fun main() {

    slidedeck {
        slide {
            section {
                h3 { +"Examples" }
                h4 { a { href = "/demo.html"; +"Demo Deck" } }
            }
        }

        section {
            +"Slide 2"
        }

        section {
            +"Slide 3"
        }
    }

    slidedeck("/demo.html") {
        section {
            +"Demo Slide 1"
        }

        section {
            +"Demo Slide 2"
        }
    }

    showSlides()
}

