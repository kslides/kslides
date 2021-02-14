package com.github.pambrose

import com.github.pambrose.Page.generatePage
import com.github.pambrose.Page.rawHtml
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.css.CSSBuilder
import kotlinx.html.*
import mu.KLogging

class Presentation(path: String, val title: String, val theme: String) {

    var css = ""
    val plugins = mutableListOf("RevealZoom", "RevealSearch", "RevealMarkdown", "RevealHighlight")

    internal val slides = mutableListOf<DIV.() -> Unit>()

    init {
        if (path.removePrefix("/") in staticRoots)
            throw IllegalArgumentException("Invalid presentation path: \"${"/${path.removePrefix("/")}"}\"")

        val adjustedPath = if (path.startsWith("/")) path else "/$path"
        if (presentations.containsKey(adjustedPath))
            throw IllegalArgumentException("Presentation path already defined: \"$adjustedPath\"")
        presentations[adjustedPath] = this
    }

    @HtmlTagMarker
    fun css(content: CSSBuilder.() -> Unit) {
        css += CSSBuilder().apply(content).toString()
    }

    class VerticalContext {
        val vertSlides = mutableListOf<SECTION.() -> Unit>()
    }

    @HtmlTagMarker
    fun Presentation.verticalSlides(content: VerticalContext.() -> Unit) {
        val vertContext = VerticalContext()
        content.invoke(vertContext)
        slides += {
            section {
                vertContext.vertSlides.forEach {
                    section { it.invoke(this) }
                    rawHtml("\n\t")
                }
            }
            rawHtml("\n\t")
        }
    }

    @HtmlTagMarker
    fun VerticalContext.htmlSlide(
        id: String = "",
        transition: Transition = Transition.Slide,
        transitionIn: Transition = Transition.Slide,
        transitionOut: Transition = Transition.Slide,
        speed: Speed = Speed.Default,
        backgroundColor: String = "",
        backgroundIframe: String = "",
        backgroundInteractive: Boolean = false,
        backgroundVideo: String = "",
        content: SECTION.() -> Unit
    ) {
        vertSlides += {
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

            if (backgroundColor.isNotEmpty())
                attributes["data-background"] = backgroundColor

            if (backgroundIframe.isNotEmpty()) {
                attributes["data-background-iframe"] = backgroundIframe

                if (backgroundInteractive)
                    attributes["data-background-interactive"] = ""
            }

            if (backgroundVideo.isNotEmpty())
                attributes["data-background-video"] = backgroundVideo

            content.invoke(this)
        }
    }

    @HtmlTagMarker
    fun Presentation.htmlSlide(
        id: String = "",
        transition: Transition = Transition.Slide,
        transitionIn: Transition = Transition.Slide,
        transitionOut: Transition = Transition.Slide,
        speed: Speed = Speed.Default,
        backgroundColor: String = "",
        backgroundIframe: String = "",
        backgroundInteractive: Boolean = false,
        backgroundVideo: String = "",
        content: SECTION.() -> Unit
    ) {
        slides += {
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

                if (backgroundColor.isNotEmpty())
                    attributes["data-background"] = backgroundColor

                if (backgroundIframe.isNotEmpty()) {
                    attributes["data-background-iframe"] = backgroundIframe

                    if (backgroundInteractive)
                        attributes["data-background-interactive"] = ""
                }

                if (backgroundVideo.isNotEmpty())
                    attributes["data-background-video"] = backgroundVideo

                content.invoke(this)
            }
            rawHtml("\n\t")
        }
    }

    @HtmlTagMarker
    fun VerticalContext.markdownSlide(
        id: String = "",
        transition: Transition = Transition.Slide,
        transitionIn: Transition = Transition.Slide,
        transitionOut: Transition = Transition.Slide,
        speed: Speed = Speed.Default,
        backgroundColor: String = "",
        backgroundIframe: String = "",
        backgroundInteractive: Boolean = false,
        backgroundVideo: String = "",
        filename: String = "",
        content: SCRIPT.() -> Unit = {}
    ) {
        htmlSlide(
            id = id,
            transition = transition,
            transitionIn = transitionIn,
            transitionOut = transitionOut,
            speed = speed,
            backgroundColor = backgroundColor,
            backgroundIframe = backgroundIframe,
            backgroundInteractive = backgroundInteractive,
            backgroundVideo = backgroundVideo
        ) {
            // If this value is == "" it means read content inline
            attributes["data-markdown"] = filename

            attributes["data-separator"] = ""
            attributes["data-separator-vertical"] = ""

            //if (notes.isNotEmpty())
            //    attributes["data-separator-notes"] = notes

            if (filename.isEmpty())
                script {
                    type = "text/template"
                    content()
                }
        }
    }

    @HtmlTagMarker
    fun Presentation.markdownSlide(
        id: String = "",
        transition: Transition = Transition.Slide,
        transitionIn: Transition = Transition.Slide,
        transitionOut: Transition = Transition.Slide,
        speed: Speed = Speed.Default,
        backgroundColor: String = "",
        backgroundIframe: String = "",
        backgroundInteractive: Boolean = false,
        backgroundVideo: String = "",
        separator: String = "",
        vertical_separator: String = "",
        notes: String = "^Note:",
        filename: String = "",
        content: SCRIPT.() -> Unit = {}
    ) {
        htmlSlide(
            id = id,
            transition = transition,
            transitionIn = transitionIn,
            transitionOut = transitionOut,
            speed = speed,
            backgroundColor = backgroundColor,
            backgroundIframe = backgroundIframe,
            backgroundInteractive = backgroundInteractive,
            backgroundVideo = backgroundVideo
        ) {
            // If this value is == "" it means read content inline
            attributes["data-markdown"] = filename

            if (separator.isNotEmpty())
                attributes["data-separator"] = separator

            if (vertical_separator.isNotEmpty())
                attributes["data-separator-vertical"] = vertical_separator

            // If any of the data-separator values are defined, then plain --- in markdown will not work
            // So do not define data-separator-notes unless using other data-separator values
            if (notes.isNotEmpty() && separator.isNotEmpty() && vertical_separator.isNotEmpty())
                attributes["data-separator-notes"] = notes

            if (filename.isEmpty())
                script {
                    type = "text/template"
                    content()
                }
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