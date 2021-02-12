package com.github.pambrose

import com.github.pambrose.Page.generatePage
import com.github.pambrose.Presentation.Companion.presentations
import com.github.pambrose.common.response.respondWith
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.content.*
import io.ktor.routing.*
import org.slf4j.event.Level

internal val staticRoots = listOf("assets", "css", "dist", "js", "plugin")

fun Application.module(testing: Boolean = false) {

    install(CallLogging) { level = Level.INFO }

    install(DefaultHeaders) { header("X-Engine", "Ktor") }

    install(Compression) {
        gzip { priority = 1.0 }
        deflate { priority = 10.0; minimumSize(1024) /* condition*/ }
    }

    routing {
        staticRoots.forEach {
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

