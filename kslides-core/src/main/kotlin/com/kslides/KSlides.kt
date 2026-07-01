package com.kslides

import com.kslides.DiagramOutputType.Companion.outputTypeFromSuffix
import com.kslides.DiagramOutputType.SVG
import com.kslides.InternalUtils.mkdir
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
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.deflate
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.compression.minimumSize
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
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
      else
        close() // HTTP mode keeps the client for the server's lifetime; otherwise release it now
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
class KSlides : AutoCloseable {
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

  private val clientLazy =
    lazy {
      HttpClient(io.ktor.client.engine.cio.CIO) {
        install(HttpTimeout)
      }
    }

  internal val client by clientLazy

  /**
   * Release the lazily-created Ktor [HttpClient] if it was ever initialized (it is created only when
   * a `diagram{}` block POSTs to Kroki). filesystem-only runs close it automatically once slides are
   * written; HTTP mode keeps it alive for the server's lifetime. Idempotent and safe to call when the
   * client was never created. Lets consumers use `kslides { … }` results with `.use { }`.
   */
  override fun close() {
    if (clientLazy.isInitialized()) {
      logger.debug { "Closing kslides HttpClient" }
      clientLazy.value.close()
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

      // Create directory (including any missing parents) if absent
      if (!mkdir(outputDir)) logger.warn { "Unable to create output directory: $outputDir" }

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
                // Create directory (including any missing parents) if absent
                if (!mkdir(path)) logger.warn { "Unable to create directory: $path" }
                File("$path/index.html") to "$dotDot$rootPrefix"
              }
            }
          logger.info { "Writing presentation $key to $file" }
          file.writeText(generatePage(p, false, srcPrefix))
        }
    }

    // Hardcoded for HTTP since the reveal.js assets are shipped on the classpath in the jar.
    private const val REVEAL_ROOT_DIR = "revealjs"

    private fun appModule(config: OutputConfig): Application.() -> Unit =
      {
        // Embedding this logic here, rather than in an Application.module() call, forgoes auto-reload.
        installPlugins(config)

        val kslides = config.kslides
        kslides.presentationMap
          .apply {
            if (!containsKey("/") && !containsKey("/index.html"))
              logger.warn { """Missing a presentation with: path = "/"""" }
          }

        routing {
          iframeRoutes(config, kslides)
          krokiRoute(config, kslides)
          staticRoutes(config, kslides)
          presentationRoutes(kslides)
        }
      }

    private fun Application.installPlugins(config: OutputConfig) {
      install(CallLogging) { level = config.callLoggingLogLevel }
      install(DefaultHeaders) { header("X-Engine", "Ktor") }
      install(Compression) {
        gzip { priority = 1.0 }
        deflate {
          priority = 10.0
          minimumSize(1024) // condition
        }
      }
    }

    /** playground and letsPlot iframe endpoints. */
    private fun Route.iframeRoutes(
      config: OutputConfig,
      kslides: KSlides,
    ) {
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
    }

    /** Kroki diagram image endpoint. */
    private fun Route.krokiRoute(
      config: OutputConfig,
      kslides: KSlides,
    ) {
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
    }

    /** Bundled static asset roots: the default HTTP root plus the reveal.js dirs. */
    private fun Route.staticRoutes(
      config: OutputConfig,
      kslides: KSlides,
    ) {
      if (config.defaultHttpRoot.isNotBlank())
        staticResources("/", config.defaultHttpRoot)

      kslides.kslidesConfig.httpStaticRoots
        .filter { it.dirname.isNotBlank() }
        .map { it.dirname }
        .forEach {
          logger.debug { "Registering http dir $REVEAL_ROOT_DIR/$it" }
          staticResources("/$REVEAL_ROOT_DIR/$it", "$REVEAL_ROOT_DIR/$it")
        }
    }

    /** One route per presentation, rendering the page on each request. */
    private fun Route.presentationRoutes(kslides: KSlides) {
      kslides.presentationMap
        .forEach { (key, p) ->
          get(key) {
            respondWith {
              generatePage(p, true, "/$REVEAL_ROOT_DIR")
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
