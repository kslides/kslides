package com.kslides

import com.kslides.DiagramOutputType.Companion.outputTypeFromSuffix
import com.kslides.DiagramOutputType.SVG
import com.kslides.FollowAlong.kslidesFollowRoute
import com.kslides.InternalUtils.mkdir
import com.kslides.KSlides.Companion.logger
import com.kslides.KSlides.Companion.writeSlidesToFileSystem
import com.kslides.LiveReload.kslidesReloadRoute
import com.kslides.Page.generatePage
import com.kslides.config.KSlidesConfig
import com.kslides.config.OutputConfig
import com.kslides.config.PresentationConfig
import com.pambrose.common.response.respondWith
import com.pambrose.common.util.ensureSuffix
import com.pambrose.common.util.toPath
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
import io.ktor.server.websocket.WebSockets
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
  buildKSlides(block)
    .apply {
      if (!outputConfig.enableFileSystem && !outputConfig.enableHttp)
        logger.warn { "Set enableHttp or enableFileSystem to true in the kslides output{} block" }

      if (outputConfig.devMode && !outputConfig.enableHttp)
        logger.warn { "output { devMode } has no effect without enableHttp = true" }

      if (outputConfig.followAlong && !outputConfig.enableHttp)
        logger.warn { "output { followAlong } has no effect without enableHttp = true" }

      if (outputConfig.enableFileSystem)
        writeSlidesToFileSystem(outputConfig)

      if (outputConfig.enableHttp)
        startHttpServer(wait = true)
      else
        close() // HTTP mode keeps the client for the server's lifetime; otherwise release it now
    }

/**
 * Evaluates the kslides DSL and validates the result **without emitting any output**: no files are
 * written and no HTTP server is started, but all configuration blocks (including `output {}`) are
 * applied. Used by tooling that renders presentations on its own terms — e.g. `exportPdf()` in the
 * kslides-export module, which serves the built [KSlides] from an ephemeral-port server via
 * [KSlides.startHttpServer].
 *
 * @param block the same configuration block accepted by [kslides].
 * @return the populated [KSlides] instance.
 * @throws IllegalArgumentException if no [KSlides.presentation] blocks are declared, or if any
 *   presentation contains zero slides.
 */
fun buildKSlides(block: KSlides.() -> Unit) =
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
    }

/**
 * Convenience wrapper around [buildKSlides] for tests: evaluates the DSL with both
 * [OutputConfig.enableFileSystem] and [OutputConfig.enableHttp] forced to `false`, so no files are
 * written and no server is started. The instance is closed before it is returned, releasing the
 * HTTP client a `diagram{}` deck may have created.
 *
 * @param block the same configuration block accepted by [kslides].
 */
fun kslidesTest(block: KSlides.() -> Unit) =
  buildKSlides {
    block()
    val userOutputBlock = outputConfigBlock
    output {
      userOutputBlock()
      enableFileSystem = false
      enableHttp = false
    }
  }.apply { close() }

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

  /**
   * The output configuration populated by the `output {}` block. Public so that tooling built on
   * [buildKSlides] (e.g. kslides-export) can read it after the DSL has been evaluated.
   */
  val outputConfig = OutputConfig(this)
  internal var kslidesConfigBlock: KSlidesConfig.() -> Unit = {}
  internal var globalPresentationConfigBlock: PresentationConfig.() -> Unit = {}
  internal var outputConfigBlock: OutputConfig.() -> Unit = {}
  internal var presentationBlocks = mutableListOf<Presentation.() -> Unit>()
  internal val presentationMap = mutableMapOf<String, Presentation>()
  internal val staticIframeContent = ConcurrentHashMap<String, String>()
  internal val dynamicIframeContent = ConcurrentHashMap<String, () -> String>()
  internal val staticKrokiContent = ConcurrentHashMap<String, ByteArray>()
  internal var slideCount = 1

  // Unique per-JVM id sent to live-reload clients on connect; a changed value after a restart is
  // what tells a reconnecting browser to reload. See LiveReload.
  internal val bootEpoch = System.currentTimeMillis().toString()

  // Rendering mutates shared per-render state (slideCount, each VerticalSlide's reconstructed child
  // list, per-slide iframe counters). Ktor serves pages concurrently, so serialize renders of this
  // KSlides on this lock to keep those transient mutations from interleaving. See Page.generatePage.
  internal val renderLock = Any()

  internal val presentations get() = presentationMap.values

  /**
   * The URL paths of all registered presentations (e.g. `"/"`, `"/demo.html"`), in declaration
   * order. Populated once [buildKSlides]/[kslides] has evaluated the DSL. Public for tooling
   * (e.g. kslides-export) that iterates the presentations served by [startHttpServer].
   */
  val presentationPaths: List<String> get() = presentationMap.keys.toList()

  internal fun presentation(
    name: String,
  ) = presentationMap[name] ?: throw IllegalArgumentException("Presentation $name not found")

  // Log the per-deck presenter URLs (with the token) so the presenter can copy one at launch.
  private fun logPresenterUrls(port: Int) {
    logger.info { "Follow-along presenting enabled. Presenter URLs:" }
    presentationPaths.forEach { path ->
      logger.info {
        "  http://localhost:$port$path?${FollowAlong.PRESENT_PARAM}=${outputConfig.followAlongToken}"
      }
    }
  }

  /**
   * Start the same Ktor HTTP server that `output { enableHttp = true }` runs, serving every
   * registered presentation plus the bundled static assets. When [com.kslides.config.OutputConfig.followAlong]
   * is enabled, the presenter URLs (with the token) are logged against the actual bound port.
   *
   * @param port port to bind; `0` picks an ephemeral free port (read it back from
   *   [KSlidesHttpServer.port]). Defaults to the configured HTTP port.
   * @param wait when `true`, block until the server is shut down (the [kslides] HTTP output mode);
   *   when `false` (the default), return immediately — the caller owns the returned handle and
   *   must [KSlidesHttpServer.close] it.
   */
  fun startHttpServer(
    port: Int = outputConfig.port,
    wait: Boolean = false,
  ): KSlidesHttpServer {
    // A blocking start never returns, so its URLs must be logged up front from the requested
    // port; a non-blocking start logs after binding, when even port 0 has resolved.
    if (outputConfig.followAlong && wait)
      logPresenterUrls(port)
    val server = KSlidesHttpServer(embeddedServer(CIO, port = port, module = appModule(outputConfig)).start(wait = wait))
    if (outputConfig.followAlong && !wait)
      logPresenterUrls(server.port)
    return server
  }

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

    /**
     * Resolve the file presentation [key] is written to under [outputDir], together with the walk
     * back up to [outputDir] that everything root-relative on that page is built from.
     *
     * The number of levels comes from the deck's own key and never from [outputDir] — the page
     * lives at `<outputDir>/<key>` and the content it references at `<outputDir>/...`, so a
     * multi-segment [outputDir] such as `build/docs` adds no distance between the two.
     *
     * The key's own shape decides the depth — see [deckLocation].
     */
    internal fun outputTarget(
      outputDir: String,
      key: String,
    ): Pair<File, String> {
      val (dirElems, fileName) = deckLocation(key)
      return File((listOf(outputDir) + dirElems + fileName).toPath(addPrefix = false, addTrailing = false)) to
        "../".repeat(dirElems.size)
    }

    /**
     * Split presentation [key] into the directory segments it nests under and the file it names.
     *
     * A `".html"` key names the file itself, so its last segment contributes no depth. Every other
     * key names a directory holding an `index.html`, so all of its segments do. Blank segments are
     * dropped, so the root key `/` yields no directories at all.
     *
     * Every consumer of "how deep is this deck" derives it here, so they cannot disagree.
     */
    internal fun deckLocation(key: String): Pair<List<String>, String> {
      val keyElems = key.split("/").filter { it.isNotBlank() }
      return if (key.endsWith(".html"))
        keyElems.dropLast(1) to keyElems.last()
      else
        keyElems to "index.html"
    }

    internal fun writeSlidesToFileSystem(config: OutputConfig) {
      require(config.outputDir.isNotBlank()) { "outputDir value must not be empty" }

      val outputDir = config.outputDir

      // Create directory (including any missing parents) if absent
      if (!mkdir(outputDir)) logger.warn { "Unable to create output directory: $outputDir" }

      config.kslides.presentationMap
        .forEach { (key, p) ->
          val (file, rootPrefix) = outputTarget(outputDir, key)
          // Create the containing directory (including any missing parents) if absent. Nested
          // ".html" decks need this too — they cannot rely on a sibling directory deck having
          // been declared first to create it for them.
          if (!mkdir(file.parent)) logger.warn { "Unable to create directory: ${file.parent}" }
          logger.info { "Writing presentation $key to $file" }
          file.writeText(generatePage(p, false, rootPrefix))
        }
    }

    // Hardcoded for HTTP since the reveal.js assets are shipped on the classpath in the jar.
    internal const val REVEAL_ROOT_DIR = "revealjs"

    private fun appModule(config: OutputConfig): Application.() -> Unit =
      {
        // Embedding this logic here, rather than in an Application.module() call, forgoes auto-reload.
        installPlugins(config)
        // Installed unconditionally: the plugin is inert without a webSocket{} route, and gating
        // it on the flags that register routes (devMode, followAlong) would be an invariant to
        // hand-maintain across two code sites.
        install(WebSockets) {
          // Reap dead connections (network drop, laptop sleep) so follow-along viewers learn
          // the presenter is gone even without a clean close. Browsers answer protocol pings
          // from their network process, so this cannot detect frozen (bfcache) pages — the
          // follow-along client closes eagerly on pagehide for that case instead.
          pingPeriodMillis = 15_000
          timeoutMillis = 30_000
        }

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
          if (config.devMode)
            kslidesReloadRoute(kslides.bootEpoch)
          if (config.followAlong)
            kslidesFollowRoute(kslides, config.followAlongToken)
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
              // Every route is registered at the root of the routing tree, so pages address
              // assets and iframe content absolutely regardless of how deep the deck's path is.
              generatePage(p, true, "/")
            }
          }
        }
    }
  }
}
