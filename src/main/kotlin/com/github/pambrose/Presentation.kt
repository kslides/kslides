package com.github.pambrose

import com.github.pambrose.Page.generatePage
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.html.*
import mu.KLogging

class Presentation(path: String, val title: String, val theme: String) {

    var css = ""

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
    fun htmlSlide(
        id: String = "",
        transition: Transition = Transition.Slide,
        transitionIn: Transition = Transition.Slide,
        transitionOut: Transition = Transition.Slide,
        speed: Speed = Speed.Default,
        background: String = "",
        content: SECTION.() -> Unit
    ) {
        slides += Slide {
            section {
                if (id.isNotEmpty())
                    this.id = id

                if (transition != Transition.Slide)
                    attributes["data-transition"] = transition.asInOut()
                else {
                    if (transitionIn != Transition.Slide || transitionOut != Transition.Slide)
                        attributes["data-transition"] = "${transitionIn.asIn()} ${transitionOut.asOut()}"
                }

                if (speed != Speed.Default)
                    attributes["data-transition-speed"] = speed.name.toLowerCase()

                if (background.isNotEmpty())
                    attributes["data-background"] = background

                content()
            }
        }
    }

    @HtmlTagMarker
    fun section(
        id: String = "",
        transition: Transition = Transition.Slide,
        transitionIn: Transition = Transition.Slide,
        transitionOut: Transition = Transition.Slide,
        speed: Speed = Speed.Default,
        background: String = "",
        content: SECTION.() -> Unit
    ) =
        htmlSlide(
            id = id,
            transition = transition,
            transitionIn = transitionIn,
            transitionOut = transitionOut,
            speed = speed,
            background = background,
            content = content
        )

    @HtmlTagMarker
    fun markdownSlide(
        id: String = "",
        transition: Transition = Transition.Slide,
        transitionIn: Transition = Transition.Slide,
        transitionOut: Transition = Transition.Slide,
        speed: Speed = Speed.Default,
        background: String = "",
        filename: String = "",
        separator: String = "",
        vertical_separator: String = "",
        notes: String = "^Note:",
        content: SECTION.() -> Unit = {}
    ) {
        htmlSlide(id, transition, transitionIn, transitionOut, speed, background) {
            attributes["data-markdown"] = filename  // It is okay of this is == ""

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
        transition: Transition = Transition.Slide,
        transitionIn: Transition = Transition.Slide,
        transitionOut: Transition = Transition.Slide,
        speed: Speed = Speed.Default,
        background: String = "",
        filename: String = "",
        notes: String = "^Note:",
        content: SECTION.() -> Unit = {}
    ) =
        markdownSlide(
            id = id,
            transition = transition,
            transitionIn = transitionIn,
            transitionOut = transitionOut,
            speed = speed,
            background = background,
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