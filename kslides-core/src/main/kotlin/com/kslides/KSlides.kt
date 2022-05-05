package com.kslides

import com.github.pambrose.common.response.*
import com.github.pambrose.common.util.*
import com.kslides.KSlides.Companion.runHttpServer
import com.kslides.KSlides.Companion.writeToFileSystem
import com.kslides.Page.generatePage
import com.kslides.Playground.playgroundFiles
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.routing.*
import kotlinx.css.*
import mu.*
import java.io.*

@DslMarker
annotation class KSlidesDslMarker

@KSlidesDslMarker
fun kslides(kslidesBlock: KSlides.() -> Unit) =
  KSlides()
    .apply {
      kslidesBlock()
      require(presentationBlocks.isNotEmpty()) { "At least one presentation must be defined" }

      configBlock(globalConfig)

      presentationBlocks.forEach { presentationBlock ->
        Presentation(this)
          .apply {
            presentationBlock(this)
            require(slides.isNotEmpty()) { "At least one slide must be defined for a presentation" }
            validatePath()

            finalConfig =
              PresentationConfig()
                .apply {
                  mergeConfig(kslides.globalConfig)
                  mergeConfig(presentationConfig)
                }

            assignCssFiles()
            assignJsFiles()
            assignPlugins()
            assignDependencies()
          }
      }

      outputBlock(outputContext)

      if (outputContext.enableFileSystem)
        writeToFileSystem(outputContext)

      if (outputContext.enableHttp)
        runHttpServer(outputContext)

      if (!outputContext.enableFileSystem && !outputContext.enableHttp)
        KSlides.logger.warn { "Set enableHttp or enableFileSystem to true in the output block" }
    }

class KSlides {
  internal val globalConfig = PresentationConfig(true)
  internal val outputContext = OutputContext(this)
  internal var configBlock: PresentationConfig.() -> Unit = {}
  internal var outputBlock: OutputContext.() -> Unit = {}
  internal var presentationBlocks = mutableListOf<Presentation.() -> Unit>()
  internal val presentationMap = mutableMapOf<String, Presentation>()
  internal val presentations get() = presentationMap.values
  internal fun presentation(name: String) =
    presentationMap[name] ?: throw IllegalArgumentException("Presentation $name not found")

  val staticRoots = mutableListOf("assets", "css", "dist", "js", "plugin", "revealjs")
  val css = AppendableString()

  @KSlidesDslMarker
  inline fun css(block: CssBuilder.() -> Unit) {
    css += "${CssBuilder().apply(block)}\n"
  }

  @KSlidesDslMarker
  fun presentationDefault(block: PresentationConfig.() -> Unit) {
    configBlock = block
  }

  @KSlidesDslMarker
  fun output(outputBlock: OutputContext.() -> Unit) {
    this.outputBlock = outputBlock
  }

  @KSlidesDslMarker
  fun presentation(block: Presentation.() -> Unit) {
    presentationBlocks += block
  }

  companion object : KLogging() {
    internal fun runHttpServer(output: OutputContext) {
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

          playgroundFiles()

          if (output.defaultHttpRoot.isNotBlank())
            static("/") {
              staticBasePackage = output.defaultHttpRoot
              resources(".")
            }

          output.kslides.staticRoots.forEach {
            if (it.isNotBlank())
              static("/$it") {
                resources(it)
              }
          }

          output.kslides.presentationMap.forEach { (key, p) ->
            get(key) {
              respondWith {
                generatePage(p)
              }
            }
          }
        }
      }.start(wait = true)
    }

    internal fun writeToFileSystem(output: OutputContext) {
      require(output.outputDir.isNotBlank()) { "outputDir value must not be empty" }

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
          file.writeText(generatePage(p, prefix))
        }
    }
  }
}
