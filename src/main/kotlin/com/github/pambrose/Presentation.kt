package com.github.pambrose

import com.github.pambrose.Page.generatePage
import com.github.pambrose.Page.rawHtml
import com.github.pambrose.SlideConfig.Companion.slideConfig
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.css.CSSBuilder
import kotlinx.html.*
import mu.KLogging

class Presentation internal constructor(path: String, val title: String, val theme: String) {

    var css = ""
    val jsFiles =
        mutableListOf(
            "dist/reveal.js",
            "plugin/zoom/zoom.js",
            "plugin/notes/notes.js",
            "plugin/search/search.js",
            "plugin/markdown/markdown.js",
            "plugin/highlight/highlight.js"
        )

    val config = ConfigOptions()

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
    fun css(block: CSSBuilder.() -> Unit) {
        css += CSSBuilder().apply(block).toString()
    }

    class VerticalContext {
        val vertSlides = mutableListOf<SECTION.() -> Unit>()
    }

    @HtmlTagMarker
    fun verticalSlides(block: VerticalContext.() -> Unit) {
        val vertContext = VerticalContext()
        block.invoke(vertContext)
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
        config: SlideConfig = slideConfig {},
        id: String = "",
        block: SECTION.() -> Unit
    ) {
        vertSlides += {
            if (id.isNotEmpty())
                this.id = id

            applyConfig(config)

            block.invoke(this)
        }
    }

    @HtmlTagMarker
    fun htmlSlide(
        config: SlideConfig = slideConfig {},
        id: String = "",
        block: SECTION.() -> Unit
    ) {
        slides += {
            section {
                if (id.isNotEmpty())
                    this.id = id

                applyConfig(config)

                block.invoke(this)
            }
            rawHtml("\n\t")
        }
    }

    @HtmlTagMarker
    fun VerticalContext.markdownSlide(
        content: String = "",
        config: SlideConfig = slideConfig {},
        id: String = "",
        filename: String = "",
    ) {
        htmlSlide(config = config, id = id) {
            // If this value is == "" it means read content inline
            attributes["data-markdown"] = filename

            attributes["data-separator"] = ""
            attributes["data-separator-vertical"] = ""

            //if (notes.isNotEmpty())
            //    attributes["data-separator-notes"] = notes

            if (filename.isEmpty())
                script {
                    type = "text/template"
                    rawHtml(content)
                }
        }
    }

    @HtmlTagMarker
    fun markdownSlide(
        content: String = "",
        config: SlideConfig = slideConfig {},
        id: String = "",
        filename: String = "",
        separator: String = "",
        vertical_separator: String = "",
        notes: String = "^Note:",
    ) {
        htmlSlide(config = config, id = id) {
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
                    rawHtml(content)
                }
        }
    }

    @HtmlTagMarker
    fun config(block: ConfigOptions.() -> Unit) = block.invoke(config)

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

        private fun SECTION.applyConfig(config: SlideConfig) {
            if (config.transition != Transition.Slide)
                attributes["data-transition"] = config.transition.asInOut()
            else {
                if (config.transitionIn != Transition.Slide || config.transitionOut != Transition.Slide)
                    attributes["data-transition"] = "${config.transitionIn.asIn()} ${config.transitionOut.asOut()}"
            }

            if (config.speed != Speed.Default)
                attributes["data-transition-speed"] = config.speed.name.toLowerCase()

            if (config.backgroundColor.isNotEmpty())
                attributes["data-background"] = config.backgroundColor

            if (config.backgroundIframe.isNotEmpty()) {
                attributes["data-background-iframe"] = config.backgroundIframe

                if (config.backgroundInteractive)
                    attributes["data-background-interactive"] = ""
            }

            if (config.backgroundVideo.isNotEmpty())
                attributes["data-background-video"] = config.backgroundVideo
        }
    }
}