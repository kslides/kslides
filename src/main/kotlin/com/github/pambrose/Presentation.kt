package com.github.pambrose

import com.github.pambrose.Page.generatePage
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.html.*
import mu.KLogging

class Presentation(path: String, val title: String, val theme: String) {

    internal inline class Slide(val content: DIV.() -> Unit)

    internal val slides = mutableListOf<Slide>()

    init {
        if (path.removePrefix("/") in staticRoots)
            throw IllegalArgumentException("Invalid presentation path: \"${"/${path.removePrefix("/")}"}\"")

        val adjustedPath = if (path.startsWith("/")) path else "/$path"
        if (presentations.containsKey(adjustedPath))
            throw IllegalArgumentException("Presentation path already defined: \"$adjustedPath\"")
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
    fun markdownSlide(
        id: String = "",
        separator: String = "",//""\\n---\\n",
        vertical_separator: String = "", //""\\n--\\n",
        content: SECTION.() -> Unit
    ) {
        htmlSlide(id) {
            attributes["data-markdown"] = ""
            if (separator.isNotEmpty())
                attributes["data-separator"] = separator
            if (vertical_separator.isNotEmpty())
                attributes["data-separator-vertical"] = vertical_separator
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
        internal val presentations = mutableMapOf<String, Presentation>()

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

// Keep this global to make it easier for users to be prompted for completion in it
@HtmlTagMarker
fun presentation(path: String = "/", title: String = "", theme: Theme = Theme.Black, block: Presentation.() -> Unit) =
    Presentation(path, title, "dist/theme/${theme.name.toLowerCase()}.css").apply { block(this) }