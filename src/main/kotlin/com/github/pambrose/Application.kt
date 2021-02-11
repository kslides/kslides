package com.github.pambrose

import com.github.pambrose.Installs.installs
import com.github.pambrose.Page.generatePage
import com.github.pambrose.Presentation.Companion.presentations
import com.github.pambrose.common.response.respondWith
import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.routing.*

val staticContent = listOf("assets", "css", "dist", "js", "plugin")

fun Application.module(testing: Boolean = false) {
    installs()

    routing {
        staticContent.forEach {
            static("/$it") { resources(it) }
        }

        presentations.forEach { presentation ->
            get(presentation.key) {
                respondWith {
                    generatePage(presentation.value)
                }
            }
        }
    }
}

