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

class SlideDeck(internal val path: String) {
    internal val slides = mutableListOf<Slide>()

    init {
        if (slidedecks.containsKey(path))
            throw IllegalArgumentException("SlideDeck path $path already defined")
        slidedecks[path] = this
    }

    @HtmlTagMarker
    fun slide(content: DIV.() -> Unit) {
        slides += Slide(content)
    }

    @HtmlTagMarker
    fun section(content: SECTION.() -> Unit) {
        slides += Slide({ section { content() } })
    }


    companion object : KLogging() {
        val slidedecks = mutableMapOf<String, SlideDeck>()

        fun showSlides() {
            val environment = commandLineEnvironment(emptyArray())
            embeddedServer(CIO, environment).start(wait = true)
        }
    }
}