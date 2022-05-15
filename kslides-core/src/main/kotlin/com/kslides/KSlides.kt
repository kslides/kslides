package com.kslides

import com.github.pambrose.common.response.*
import com.github.pambrose.common.util.*
import com.kslides.KSlides.Companion.logger
import com.kslides.KSlides.Companion.runHttpServer
import com.kslides.KSlides.Companion.writeToFileSystem
import com.kslides.Page.generatePage
import com.kslides.Playground.setupPlayground
import com.kslides.Playground.writePlaygroundFiles
import com.kslides.Plotly.setupPlotly
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
fun kslides(block: KSlides.() -> Unit) =
  KSlides()
    .apply {
      block()
      require(presentationBlocks.isNotEmpty()) { "At least one presentation must be defined" }

      kslidesConfigBlock(kslidesConfig)

      globalPresentationConfigBlock(globalPresentationConfig)

      presentationBlocks.forEach { presentationBlock ->
        Presentation(this)
          .apply {
            presentationBlock(this)
            require(slides.isNotEmpty()) { "At least one slide must be defined for a presentation" }
            validatePath()

            finalConfig =
              PresentationConfig()
                .apply {
                  mergeConfig(kslides.globalPresentationConfig)
                  mergeConfig(presentationConfig)
                }

            assignCssFiles()
            assignJsFiles()
            assignPlugins()
            assignDependencies()
          }
      }

      outputConfigBlock(outputConfig)

      if (!outputConfig.enableFileSystem && !outputConfig.enableHttp)
        logger.warn { "Set enableHttp or enableFileSystem to true in the kslides output{} block" }

      if (outputConfig.enableFileSystem)
        writeToFileSystem(outputConfig)

      if (outputConfig.enableHttp || playgroundUrls.isNotEmpty())
        runHttpServer(outputConfig, false)

      if (playgroundUrls.isNotEmpty())
        writePlaygroundFiles(outputConfig, playgroundUrls)

      if (outputConfig.enableHttp)
        CountDownLatch(1).await()
    }

@KSlidesDslMarker
internal fun kslidesTest(block: KSlides.() -> Unit) =
  kslides {
    block()
    output {
      enableFileSystem = false
      enableHttp = false
    }
  }

class KSlides {
  internal val kslidesConfig = KSlidesConfig()
  internal val globalPresentationConfig = PresentationConfig().apply { assignDefaults() }
  internal val outputConfig = OutputConfig(this)
  internal var kslidesConfigBlock: KSlidesConfig.() -> Unit = {}
  internal var globalPresentationConfigBlock: PresentationConfig.() -> Unit = {}
  internal var outputConfigBlock: OutputConfig.() -> Unit = {}
  internal var presentationBlocks = mutableListOf<Presentation.() -> Unit>()
  internal val presentationMap = mutableMapOf<String, Presentation>()
  internal val playgroundUrls = mutableListOf<Pair<DslSlide, String>>()
  internal val plotlyContent = mutableMapOf<String, String>()
  internal val presentations get() = presentationMap.values
  internal fun presentation(name: String) =
    presentationMap[name] ?: throw IllegalArgumentException("Presentation $name not found")

  // User variables
  val css = CssValue()

  @KSlidesDslMarker
  fun kslidesConfig(block: KSlidesConfig.() -> Unit) {
    kslidesConfigBlock = block
  }

  @KSlidesDslMarker
  fun output(block: OutputConfig.() -> Unit) {
    outputConfigBlock = block
  }

  @KSlidesDslMarker
  fun presentationConfig(block: PresentationConfig.() -> Unit) {
    globalPresentationConfigBlock = block
  }

  @KSlidesDslMarker
  fun css(block: CssBuilder.() -> Unit) {
    css += block
  }

  @KSlidesDslMarker
  fun presentation(block: Presentation.() -> Unit) {
    presentationBlocks += block
  }

  companion object : KLogging() {
    internal fun writeToFileSystem(config: OutputConfig) {
      require(config.outputDir.isNotBlank()) { "outputDir value must not be empty" }

      val outputDir = config.outputDir
      val rootPrefix = config.staticRootDir.ensureSuffix("/")

      // Create directory if missing
      File(outputDir).mkdir()

      config.kslides.presentationMap
        .forEach { (key, p) ->
          val (file, srcPrefix) =
            when {
              key == "/" -> File("$outputDir/index.html") to rootPrefix
              key.endsWith(".html") -> File("$outputDir/$key") to rootPrefix
              else -> {
                val pathElems = "$outputDir/$key".split("/").filter { it.isNotBlank() }
                val path = pathElems.joinToString("/")
                val dotDot = List(pathElems.size - 1) { "../" }.joinToString("")
                // Create directory if missing
                File(path).mkdir()
                File("$path/index.html") to "$dotDot$rootPrefix"
              }
            }
          logger.info { "Writing presentation $key to $file" }
          file.writeText(generatePage(p, false, srcPrefix))
        }
    }

    internal fun runHttpServer(config: OutputConfig, wait: Boolean) {
      embeddedServer(CIO, port = config.port) {
        // By embedding this logic here, rather than in an Application.module() call, we are not able to use auto-reload
        install(CallLogging) { level = config.callLoggingLogLevel }
        install(DefaultHeaders) { header("X-Engine", "Ktor") }
        install(Compression) {
          gzip { priority = 1.0 }
          deflate { priority = 10.0; minimumSize(1024) /* condition*/ }
        }

        config.kslides.presentationMap
          .apply {
            if (!containsKey("/") && !containsKey("/index.html"))
              logger.warn { """Missing a presentation with: path = "/"""" }
          }

        routing {
          setupPlayground(config.kslides)
          setupPlotly(config.kslides)

          if (config.defaultHttpRoot.isNotBlank())
            static("/") {
              staticBasePackage = config.defaultHttpRoot
              resources(".")
            }

          // This is hardcoded for http since it is shipped with the jar
          val rootDir = "revealjs"
          val baseDirs =
            config.kslides.kslidesConfig.httpStaticRoots
              .filter { it.dirname.isNotBlank() }
              .map { it.dirname }

          if (baseDirs.isNotEmpty())
            static("/") {
              staticBasePackage = rootDir
              static(rootDir) {
                baseDirs.forEach {
                  static(it) {
                    logger.debug { "Registering http dir $it" }
                    resources(it)
                  }
                }
              }
            }

          config.kslides.presentationMap
            .forEach { (key, p) ->
              get(key) {
                respondWith {
                  generatePage(p, true, "/$rootDir")
                }
              }
            }
        }
      }.start(wait = wait)
    }
  }
}
