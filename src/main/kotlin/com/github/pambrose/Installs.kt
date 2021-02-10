package com.github.pambrose

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import mu.KLogging
import org.slf4j.event.Level

object Installs : KLogging() {

    fun Application.installs() {

        install(CallLogging) {
            level = Level.INFO

            format { call ->
                val request = call.request
                val response = call.response
                val logStr = request.toLogString()
                val remote = request.origin.remoteHost

                when (val status = response.status() ?: HttpStatusCode(-1, "Unknown")) {
                    HttpStatusCode.Found -> "Redirect: $logStr -> ${response.headers[HttpHeaders.Location]} - $remote"
                    else -> "$status: $logStr - $remote"
                }
            }
        }

        install(DefaultHeaders) {
            header("X-Engine", "Ktor")
        }

        install(Compression) {
            gzip {
                priority = 1.0
            }
            deflate {
                priority = 10.0
                minimumSize(1024) // condition
            }
        }
    }
}