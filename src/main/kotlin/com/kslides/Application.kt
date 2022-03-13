package com.kslides

import com.github.pambrose.common.response.*
import com.kslides.Page.generatePage
import com.kslides.Presentation.Companion.presentations
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.*
import io.ktor.server.routing.*
import org.slf4j.event.*

internal val staticRoots = listOf("public", "assets", "css", "dist", "js", "plugin")

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
                    generatePage(presentation.value, "")
                }
            }
        }
    }
}