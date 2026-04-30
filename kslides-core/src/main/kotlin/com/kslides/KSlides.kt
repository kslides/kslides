package com.kslides

import com.kslides.DiagramOutputType.Companion.outputTypeFromSuffix
import com.kslides.DiagramOutputType.SVG
import com.kslides.KSlides.Companion.logger
import com.kslides.KSlides.Companion.runHttpServer
import com.kslides.KSlides.Companion.writeSlidesToFileSystem
import com.kslides.Page.generatePage
import com.kslides.config.KSlidesConfig
import com.kslides.config.OutputConfig
import com.kslides.config.PresentationConfig
import com.pambrose.common.response.respondWith
import com.pambrose.common.util.ensureSuffix
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.css.CssBuilder
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Marks receiver types that participate in the kslides DSL so that Kotlin's scope-control can
 * prevent unintended nesting (e.g. calling `markdownSlide{}` directly from inside another slide's
 * content block).
 *
 * Applied to types rather than functions because `@DslMarker` on functions is a no-op
 * (see [KT-81567](https://youtrack.jetbrains.com/issue/KT-81567)).
 */
@DslMarker
annotation class KSlidesDslMarker

/**
 * Top-level entry point for defining presentations.
 *
 * A typical script looks like:
 *
 * ```kotlin
 * kslides {
 *   output { enableFileSystem = true; enableHttp = false }
 *   presentation {
 *     path = "helloworld.html"
 *     markdownSlide { content { "# Hello" } }
 *   }
 * }
 * ```
 *
 * On return, the configured output modes are executed: `enableFileSystem` writes static HTML under
 * the output directory, and `enableHttp` starts a Ktor server on the configured port. If neither is
 * enabled, a warning is logged and no slides are emitted.
 *
 * @param block configuration block applied to a fresh [KSlides] instance.
 * @return the populated [KSlides] instance (primarily useful for tests).
 * @throws IllegalArgumentException if no [KSlides.presentation] blocks are declared, or if any
 *   presentation contains zero slides.
 */
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
            presentationBlock()
            require(slides.isNotEmpty()) { "At least one slide must be defined for a presentation" }
            validatePath()

            finalConfig =
              PresentationConfig()
                .also { config ->
                  config.mergeConfig(kslides.globalPresentationConfig)
                  config.mergeConfig(presentationConfig)
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
        writeSlidesToFileSystem(outputConfig)

      if (outputConfig.enableHttp)
        runHttpServer(outputConfig, true)
    }

/**
 * Convenience wrapper around [kslides] for tests: evaluates the DSL with both [OutputConfig.enableFileSystem]
 * and [OutputConfig.enableHttp] forced to `false` so no files are written and no server is started.
 *
 * @param block the same configuration block accepted by [kslides].
 */
fun kslidesTest(block: KSlides.() -> Unit) =
  kslides {
    block()
    val userOutputBlock = outputConfigBlock
    output {
      userOutputBlock()
      enableFileSystem = false
      enableHttp = false
    }
  }

/**
 * Root orchestrator for a set of presentations. Instances are not meant to be constructed
 * directly — use the [kslides] top-level function instead, which applies the DSL block and then
 * triggers the configured output modes.
 *
 * Holds global configuration ([kslidesConfig], [presentationConfig], [output]), shared CSS
 * ([css]), and the collection of [Presentation] definitions produced by each [presentation]
 * block. Also caches iframe and Kroki content so repeated lookups from the Ktor server do not
 * re-execute expensive content generators.
 */
@KSlidesDslMarker
class KSlides {
  internal val kslidesConfig = KSlidesConfig()
  internal val globalPresentationConfig = PresentationConfig().apply { assignDefaults() }
  internal val outputConfig = OutputConfig(this)
  internal var kslidesConfigBlock: KSlidesConfig.() -> Unit = {}
  internal var globalPresentationConfigBlock: PresentationConfig.() -> Unit = {}
  internal var outputConfigBlock: OutputConfig.() -> Unit = {}
  internal var presentationBlocks = mutableListOf<Presentation.() -> Unit>()
  internal val presentationMap = mutableMapOf<String, Presentation>()
  internal val staticIframeContent = ConcurrentHashMap<String, String>()
  internal val dynamicIframeContent = ConcurrentHashMap<String, () -> String>()
  internal val staticKrokiContent = ConcurrentHashMap<String, ByteArray>()
  internal var slideCount = 1

  internal val presentations get() = presentationMap.values

  internal fun presentation(
    name: String,
  ) = presentationMap[name] ?: throw IllegalArgumentException("Presentation $name not found")

  internal val client by lazy {
    HttpClient(io.ktor.client.engine.cio.CIO) {
      install(HttpTimeout)
    }
  }

  /**
   * Global CSS applied to every [Presentation] in this [KSlides]. Appended to via the [css] DSL
   * block or via `css += "..."` / `css += { ... }`.
   */
  val css = CssValue()

  /**
   * Configure values that apply to all presentations (e.g. static asset roots, Playground URL,
   * Kroki URL, Lets-Plot JS version, HTTP client timeout). The block is evaluated after the full
   * `kslides {}` DSL so that later `kslidesConfig{}` calls override earlier ones.
   */
  fun kslidesConfig(block: KSlidesConfig.() -> Unit) {
    kslidesConfigBlock = block
  }

  /**
   * Configure how the presentations are emitted: filesystem mode (`enableFileSystem`), HTTP mode
   * (`enableHttp`), output directory, HTTP port, etc.
   */
  fun output(block: OutputConfig.() -> Unit) {
    outputConfigBlock = block
  }

  /**
   * Default [PresentationConfig] applied to every [Presentation]. Per-presentation overrides
   * take precedence; see the configuration-cascade description in the project README.
   */
  fun presentationConfig(block: PresentationConfig.() -> Unit) {
    globalPresentationConfigBlock = block
  }

  /**
   * Append CSS (declared via Kotlin's CSS DSL) to the global stylesheet that is injected into
   * every generated presentation page.
   */
  fun css(block: CssBuilder.() -> Unit) {
    css += block
  }

  /**
   * Register a [Presentation] definition. Each call adds one entry; the `path` assigned inside
   * the block determines its URL (HTTP mode) or output filename (filesystem mode).
   *
   * At least one `presentation {}` call is required for [kslides] to succeed.
   */
  fun presentation(block: Presentation.() -> Unit) {
    presentationBlocks += block
  }

  companion object {
    internal val logger = KotlinLogging.logger {}

    internal fun writeSlidesToFileSystem(config: OutputConfig) {
      require(config.outputDir.isNotBlank()) { "outputDir value must not be empty" }

      val outputDir = config.outputDir
      val rootPrefix = config.staticRootDir.ensureSuffix("/")

      // Create directory if missing
      File(outputDir).mkdir()

      config.kslides.presentationMap
        .forEach { (key, p) ->
          val (file, srcPrefix) =
            when {
              key == "/" -> {
                File("$outputDir/index.html") to rootPrefix
              }

              key.endsWith(".html") -> {
                File("$outputDir/$key") to rootPrefix
              }

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

    private fun appModule(config: OutputConfig): Application.() -> Unit =
      {
        // By embedding this logic here, rather than in an Application.module() call, we are not able to use auto-reload
        install(CallLogging) { level = config.callLoggingLogLevel }
        install(DefaultHeaders) { header("X-Engine", "Ktor") }
        install(Compression) {
          gzip { priority = 1.0 }
          deflate {
            priority = 10.0
            minimumSize(1024) // condition
          }
        }

        val kslides = config.kslides

        kslides.presentationMap
          .apply {
            if (!containsKey("/") && !containsKey("/index.html"))
              logger.warn { """Missing a presentation with: path = "/"""" }
          }

        routing {
          // playground and letsPlot iframe endpoints
          listOf(config.playgroundDir, config.letsPlotDir)
            .forEach {
              get("$it/{fname}") {
                respondWith {
                  val path = call.parameters["fname"] ?: throw IllegalArgumentException("Missing $it arg")
                  kslides.dynamicIframeContent[path]?.invoke()
                    ?: kslides.staticIframeContent[path]
                    ?: throw IllegalArgumentException("Invalid $it path: $path")
                }
              }
            }

          // kroki endpoint
          get("${config.krokiDir}/{fname}") {
            val filename =
              call.parameters["fname"] ?: throw IllegalArgumentException("Missing ${config.krokiDir} filename")
            val bytes =
              kslides.staticKrokiContent[filename]
                ?: throw IllegalArgumentException("Invalid ${config.krokiDir} path: $filename")
            val suffix = filename.substringAfterLast(".")
            when (val outputType = outputTypeFromSuffix(suffix)) {
              SVG -> call.respondText(String(bytes), outputType.contentType)
              else -> call.respondBytes(bytes, outputType.contentType)
            }
          }

          if (config.defaultHttpRoot.isNotBlank())
            staticResources("/", config.defaultHttpRoot)

          // This is hardcoded for http since it is shipped with the jar
          val revealRootDir = "revealjs"
          val baseDirNames =
            kslides.kslidesConfig.httpStaticRoots
              .filter { it.dirname.isNotBlank() }
              .map { it.dirname }

          if (baseDirNames.isNotEmpty()) {
            baseDirNames.forEach {
              logger.debug { "Registering http dir $revealRootDir/$it" }
              staticResources("/$revealRootDir/$it", "$revealRootDir/$it")
            }
          }

          kslides.presentationMap
            .forEach { (key, p) ->
              get(key) {
                respondWith {
                  generatePage(p, true, "/$revealRootDir")
                }
              }
            }
        }
      }

    internal fun runHttpServer(
      config: OutputConfig,
      wait: Boolean,
    ) {
      embeddedServer(CIO, port = config.port, module = appModule(config)).start(wait = wait)
    }
  }
}
