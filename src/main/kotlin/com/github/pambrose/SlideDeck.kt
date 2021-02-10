package com.github.pambrose

import com.github.pambrose.Page.generatePage
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.html.*
import mu.KLogging

@HtmlTagMarker
fun slidedeck(path: String = "/", block: SlideDeck.() -> Unit) =
    SlideDeck(path).apply { block(this) }

internal inline class Slide(val content: DIV.() -> Unit)

class SlideDeck(path: String) {
    internal val slides = mutableListOf<Slide>()

    init {
        val adjustedPath = if (path.startsWith("/")) path else "/$path"
        if (slidedecks.containsKey(adjustedPath))
            throw IllegalArgumentException("Slide Deck path $adjustedPath already defined")
        slidedecks[adjustedPath] = this
    }

    @HtmlTagMarker
    fun htmlSlide(content: SECTION.() -> Unit) {
        slides += Slide({ section { content() } })
    }

    @HtmlTagMarker
    fun section(content: SECTION.() -> Unit) = htmlSlide(content)

    @HtmlTagMarker
    fun markdownSlide(content: SECTION.() -> Unit) {
        htmlSlide {
            attributes["data-markdown"] = "true"
            content()
        }
    }

    @HtmlTagMarker
    fun SECTION.markdown(content: SCRIPT.() -> Unit) {
        script {
            type = "text/template"
            content()
        }
    }

    companion object : KLogging() {
        val slidedecks = mutableMapOf<String, SlideDeck>()

        fun presentSlides() {
            val environment = commandLineEnvironment(emptyArray())
            embeddedServer(CIO, environment).start(wait = true)
        }

        fun printSlides() {
            for (slidedeck in slidedecks) {
                println(slidedeck.key)
                println(generatePage(slidedeck.value))
            }
        }
    }
}