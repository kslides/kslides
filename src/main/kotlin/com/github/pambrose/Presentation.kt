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
        filename: String = "",
        separator: String = "",
        vertical_separator: String = "",
        notes: String = "^Note:",
        content: SECTION.() -> Unit = {}
    ) {
        htmlSlide(id) {
            attributes["data-markdown"] = filename
            if (separator.isNotEmpty())
                attributes["data-separator"] = separator
            if (vertical_separator.isNotEmpty())
                attributes["data-separator-vertical"] = vertical_separator
            // If any of the data-separator values are defined, then plain --- in markdown will not work
            // So do not define data-separator-notes unless using other data-separator values
            if (notes.isNotEmpty() && separator.isNotEmpty() && vertical_separator.isNotEmpty())
                attributes["data-separator-notes"] = notes
            content()
        }
    }

    @HtmlTagMarker
    fun mulitMarkdownSlide(
        id: String = "",
        filename: String = "",
        notes: String = "^Note:",
        content: SECTION.() -> Unit = {}
    ) =
        markdownSlide(
            id = id,
            filename = filename,
            separator = "\r?\\n---\r?\\n",
            vertical_separator = "\r?\\n--\r?\\n",
            notes = notes,
            content = content
        )

    @HtmlTagMarker
    fun SECTION.markdown(content: SCRIPT.() -> Unit) {
        script {
            type = "text/template"
            content()
        }
    }

    fun SCRIPT.slideBackground(color: String) = "<!-- .slide: data-background=\"$color\" -->"

    fun SCRIPT.fragmentIndex(index: Int) =
        "<!-- .element: class=\"fragment\" data-fragment-index=\"$index\" -->"

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