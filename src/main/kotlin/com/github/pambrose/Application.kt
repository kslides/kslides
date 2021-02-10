package com.github.pambrose

import com.github.pambrose.Installs.installs
import com.github.pambrose.Page.generatePage
import com.github.pambrose.SlideDeck.Companion.slidedecks
import com.github.pambrose.common.response.respondWith
import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.routing.*
import kotlinx.html.HTMLTag
import kotlinx.html.unsafe

fun Application.module(testing: Boolean = false) {
    installs()

    routing {
        static("/assets") { resources("assets") }
        static("/css") { resources("css") }
        static("/dist") { resources("dist") }
        static("/js") { resources("js") }
        static("/plugin") { resources("plugin") }

        for (slidedeck in slidedecks)
            get(slidedeck.key) {
                respondWith {
                    generatePage(slidedeck.value)
                }
            }
    }
}

fun HTMLTag.rawHtml(html: String) = unsafe { raw(html) }