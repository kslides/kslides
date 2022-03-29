package com.kslides

import com.github.pambrose.common.response.*
import com.kslides.Page.generatePage
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.*
import io.ktor.server.routing.*

fun Application.module(output: PresentationOutput, testing: Boolean = false) {

  install(CallLogging) { level = output.logLevel }

  install(DefaultHeaders) { header("X-Engine", "Ktor") }

  install(Compression) {
    gzip { priority = 1.0 }
    deflate { priority = 10.0; minimumSize(1024) /* condition*/ }
  }

  routing {

    output.kslides.also { topLevel ->

      if (output.defaultRoot.isNotEmpty())
        static("/") {
          staticBasePackage = output.defaultRoot
          resources(".")
        }

      topLevel.staticRoots.forEach {
        if (it.isNotEmpty())
          static("/$it") {
            resources(it)
          }
      }

      topLevel.presentationMap.forEach { (key, value) ->
        get(key) {
          respondWith {
            generatePage(value)
          }
        }
      }
    }
  }
}