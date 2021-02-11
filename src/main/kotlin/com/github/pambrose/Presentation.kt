package com.github.pambrose

import com.github.pambrose.Page.generatePage
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.html.*
import mu.KLogging

@HtmlTagMarker
fun presentation(path: String = "/", title: String = "", theme: Theme = Theme.Black, block: Presentation.() -> Unit) =
    Presentation(path, title, "dist/theme/${theme.name.toLowerCase()}.css").apply { block(this) }

internal inline class Slide(val content: DIV.() -> Unit)

class Presentation(path: String, val title: String, val theme: String) {
    internal val slides = mutableListOf<Slide>()

    init {
        val adjustedPath = if (path.startsWith("/")) path else "/$path"
        if (presentations.containsKey(adjustedPath))
            throw IllegalArgumentException("Slide Deck path $adjustedPath already defined")
        presentations[adjustedPath] = this
    }

    @HtmlTagMarker
    fun htmlSlide(id: String = "", content: SECTION.() -> Unit) {
        slides += Slide {
            section {
                if (id.isNotEmpty())
                    this.id = id
                content()
            }
        }
    }

    @HtmlTagMarker
    fun section(id: String = "", content: SECTION.() -> Unit) = htmlSlide(id, content)

    @HtmlTagMarker
    fun markdownSlide(id: String = "", content: SECTION.() -> Unit) {
        htmlSlide {
            attributes["data-markdown"] = ""
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
        val presentations = mutableMapOf<String, Presentation>()

        fun present() {
            val environment = commandLineEnvironment(emptyArray())
            embeddedServer(CIO, environment).start(wait = true)
        }

        fun output() {
            for (presentation in presentations) {
                println(presentation.key)
                println(generatePage(presentation.value))
            }
        }
    }
}