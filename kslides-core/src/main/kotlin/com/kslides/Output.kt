package com.kslides

import com.github.pambrose.common.response.*
import com.github.pambrose.common.util.*
import com.kslides.Page.generatePage
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.*
import io.ktor.server.routing.*
import mu.*
import java.io.*

object Output : KLogging() {
  internal fun runHttpServer(output: PresentationOutput) {

    val port = System.getenv("PORT")?.toInt() ?: output.httpPort

    embeddedServer(CIO, port = port) {

      // By embedding this logic here, rather than in an Application.module() call, we are not able to use auto-reload
      install(CallLogging) { level = output.logLevel }
      install(DefaultHeaders) { header("X-Engine", "Ktor") }
      install(Compression) {
        gzip { priority = 1.0 }
        deflate { priority = 10.0; minimumSize(1024) /* condition*/ }
      }

      routing {
        if (output.defaultHttpRoot.isNotEmpty())
          static("/") {
            staticBasePackage = output.defaultHttpRoot
            resources(".")
          }

        output.kslides.staticRoots.forEach {
          if (it.isNotEmpty())
            static("/$it") {
              resources(it)
            }
        }

        output.kslides.presentationMap.forEach { (key, value) ->
          get(key) {
            respondWith {
              generatePage(value, value.finalConfig)
            }
          }
        }
      }
    }.start(wait = true)
  }

  internal fun writeToFileSystem(output: PresentationOutput) {
    require(output.outputDir.isNotEmpty()) { "outputDir value must not be empty" }

    val outputDir = output.outputDir
    val srcPrefix = output.staticRootDir.ensureSuffix("/")

    File(outputDir).mkdir()
    output.kslides.presentationMap
      .forEach { (key, p) ->
        val (file, prefix) =
          when {
            key == "/" -> File("$outputDir/index.html") to srcPrefix
            key.endsWith(".html") -> File("$outputDir/$key") to srcPrefix
            else -> {
              val pathElems = "$outputDir/$key".split("/").filter { it.isNotEmpty() }
              val path = pathElems.joinToString("/")
              val dotDot = List(pathElems.size - 1) { "../" }.joinToString("")
              File(path).mkdir()
              File("$path/index.html") to "$dotDot$srcPrefix"
            }
          }
        logger.info { "Writing presentation $key to $file" }
        file.writeText(generatePage(p, p.finalConfig, prefix))
      }
  }
}