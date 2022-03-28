package com.kslides

import com.kslides.Page.generatePage
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import java.io.*

object Output {

  internal fun runHttpServer(output: PresentationOutput) {
    val environment = commandLineEnvironment(emptyArray())
    embeddedServer(factory = CIO, environment).start(wait = true)
  }

  internal fun writeToFileSystem(output: PresentationOutput) {
    require(output.dir.isNotEmpty()) { "dir value must not be empty" }

    File(output.dir).mkdir()
    output.kslides.presentationMap.forEach { (key, p) ->
      val (file, prefix) =
        when {
          key == "/" -> File("${output.dir}/index.html") to output.srcPrefix
          key.endsWith(".html") -> File("${output.dir}/$key") to output.srcPrefix
          else -> {
            val pathElems = "${output.dir}/$key".split("/").filter { it.isNotEmpty() }
            val path = pathElems.joinToString("/")
            val dotDot = List(pathElems.size - 1) { "../" }.joinToString("")
            File(path).mkdir()
            File("$path/index.html") to "$dotDot${output.srcPrefix}"
          }
        }
      println("Writing presentation $key to $file")
      file.writeText(generatePage(p, prefix))
    }
  }

}