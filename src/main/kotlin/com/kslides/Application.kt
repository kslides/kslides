package com.kslides

import com.github.pambrose.common.response.*
import com.kslides.KSlides.Companion.topLevel
import com.kslides.Page.generatePage
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.*
import io.ktor.server.routing.*
import org.slf4j.event.*

fun Application.module(testing: Boolean = false) {

  install(CallLogging) { level = Level.INFO }

  install(DefaultHeaders) { header("X-Engine", "Ktor") }

  install(Compression) {
    gzip { priority = 1.0 }
    deflate { priority = 10.0; minimumSize(1024) /* condition*/ }
  }

  routing {
    topLevel.staticRoots.forEach {
      static("/$it") { resources(it) }
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