package com.kslides

import com.github.pambrose.common.response.*
import com.github.pambrose.common.util.*
import com.kslides.KSlides.Companion.runHttpServer
import com.kslides.KSlides.Companion.writePlaygroundFiles
import com.kslides.KSlides.Companion.writeToFileSystem
import com.kslides.Page.generatePage
import com.kslides.Playground.setupPlayground
import com.kslides.config.*
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
import java.util.concurrent.*

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

      outputBlock(outputConfig)

      if (!outputConfig.enableFileSystem && !outputConfig.enableHttp)
        KSlides.logger.warn { "Set enableHttp or enableFileSystem to true in the kslides output{} section" }

      if (outputConfig.enableFileSystem)
        writeToFileSystem(outputConfig)

      if (outputConfig.enableHttp || playgroundUrls.isNotEmpty())
        runHttpServer(outputConfig, false)

      if (playgroundUrls.isNotEmpty())
        writePlaygroundFiles(outputConfig, playgroundUrls)

      if (outputConfig.enableHttp)
        CountDownLatch(1).await()
    }

class KSlides {
  internal val globalConfig = PresentationConfig(true)
  internal val outputConfig = OutputConfig(this)
  internal var configBlock: PresentationConfig.() -> Unit = {}
  internal var outputBlock: OutputConfig.() -> Unit = {}
  internal var presentationBlocks = mutableListOf<Presentation.() -> Unit>()
  internal val presentationMap = mutableMapOf<String, Presentation>()
  internal val playgroundUrls = mutableListOf<Pair<String, String>>()
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
  fun output(outputBlock: OutputConfig.() -> Unit) {
    this.outputBlock = outputBlock
  }

  @KSlidesDslMarker
  fun presentation(block: Presentation.() -> Unit) {
    presentationBlocks += block
  }

  companion object : KLogging() {
    internal fun writeToFileSystem(config: OutputConfig) {
      require(config.outputDir.isNotBlank()) { "outputDir value must not be empty" }

      val outputDir = config.outputDir
      val srcPrefix = config.staticRootDir.ensureSuffix("/")

      // Create directory if missing
      File(outputDir).mkdir()

      config.kslides.presentationMap
        .forEach { (key, p) ->
          val (file, prefix) =
            when {
              key == "/" -> File("$outputDir/index.html") to srcPrefix
              key.endsWith(".html") -> File("$outputDir/$key") to srcPrefix
              else -> {
                val pathElems = "$outputDir/$key".split("/").filter { it.isNotBlank() }
                val path = pathElems.joinToString("/")
                val dotDot = List(pathElems.size - 1) { "../" }.joinToString("")
                // Create directory if missing
                File(path).mkdir()
                File("$path/index.html") to "$dotDot$srcPrefix"
              }
            }
          logger.info { "Writing presentation $key to $file" }
          file.writeText(generatePage(p, false, prefix))
        }
    }

    internal fun runHttpServer(config: OutputConfig, wait: Boolean) {
      embeddedServer(CIO, port = config.port) {
        // By embedding this logic here, rather than in an Application.module() call, we are not able to use auto-reload
        install(CallLogging) { level = config.logLevel }
        install(DefaultHeaders) { header("X-Engine", "Ktor") }
        install(Compression) {
          gzip { priority = 1.0 }
          deflate { priority = 10.0; minimumSize(1024) /* condition*/ }
        }

        routing {

          setupPlayground()

          if (config.defaultHttpRoot.isNotBlank())
            static("/") {
              staticBasePackage = config.defaultHttpRoot
              resources(".")
            }

          config.kslides.staticRoots.forEach {
            if (it.isNotBlank())
              static("/$it") {
                resources(it)
              }
          }

          config.kslides.presentationMap.forEach { (key, p) ->
            get(key) {
              respondWith {
                generatePage(p, true)
              }
            }
          }
        }
      }.start(wait = wait)
    }

    internal fun writePlaygroundFiles(config: OutputConfig, playgroundUrls: List<Pair<String, String>>) {
      val root = config.outputDir.ensureSuffix("/")
      val playground = config.playgroundDir.ensureSuffix("/")
      val fullPath = "$root$playground"
      // Create directory if missing
      File(fullPath).mkdir()

      playgroundUrls.forEach { (filename, url) ->
        val fullname = "$root$filename"
        val content = includeUrl("http://0.0.0.0:${config.port}/$url", indentToken = "", escapeHtml = false)

        logger.info { "Writing playground content to: $root$filename" }
        File(fullname).writeText(content)
      }
    }
  }
}
