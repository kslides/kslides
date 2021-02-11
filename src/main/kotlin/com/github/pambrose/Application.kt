package com.github.pambrose

import com.github.pambrose.Installs.installs
import com.github.pambrose.Page.generatePage
import com.github.pambrose.Presentation.Companion.presentations
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

        presentations.forEach { presentation ->
            get(presentation.key) {
                respondWith {
                    generatePage(presentation.value)
                }
            }
        }
    }
}

fun HTMLTag.rawHtml(html: String) = unsafe { raw(html) }