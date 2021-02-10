package com.github.pambrose

import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.html.DIV
import kotlinx.html.HtmlTagMarker
import kotlinx.html.SECTION
import kotlinx.html.section
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
    fun slide(content: SECTION.() -> Unit) {
        slides += Slide({ section { content() } })
    }

    @HtmlTagMarker
    fun section(content: SECTION.() -> Unit) = slide({ section { content() } })

    companion object : KLogging() {
        val slidedecks = mutableMapOf<String, SlideDeck>()

        fun presentSlides() {
            val environment = commandLineEnvironment(emptyArray())
            embeddedServer(CIO, environment).start(wait = true)
        }
    }
}