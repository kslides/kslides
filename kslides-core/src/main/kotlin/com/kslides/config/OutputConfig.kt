package com.kslides.config

import com.kslides.KSlides
import com.kslides.KSlidesDslMarker
import com.pambrose.common.util.toPath
import org.slf4j.event.Level
import java.util.UUID

/**
 * Controls where and how presentations are emitted. Configured via [com.kslides.KSlides.output].
 * Both output modes can be enabled simultaneously; if neither is enabled a warning is logged
 * and no output is produced.
 *
 * @property kslides back-reference to the owning [KSlides] instance.
 */
@KSlidesDslMarker
class OutputConfig(
  val kslides: KSlides,
) {
  /** When `true`, write static HTML files under [outputDir]. Default `true`. */
  var enableFileSystem = true

  /** Root directory for filesystem-mode output. Default `"docs"` (GitHub Pages / Netlify friendly). */
  var outputDir = "docs"

  /**
   * Directory holding the bundled reveal.js assets, in **filesystem mode only**. A generated page
   * links to them through this directory, reached from wherever the page itself sits. HTTP mode
   * ignores this and serves the assets from the classpath via [defaultHttpRoot] instead.
   */
  var staticRootDir = "revealjs"

  /** Subdirectory of [outputDir] where Kotlin Playground iframe HTML files are written. */
  var playgroundDir = "playground"

  /** Subdirectory of [outputDir] where Lets-Plot iframe HTML files are written. */
  var letsPlotDir = "letsPlot"

  /** Subdirectory of [outputDir] where Kroki diagram images are written. */
  var krokiDir = "kroki"

  /** When `true`, start a Ktor HTTP server serving the presentations. Default `true`. */
  var enableHttp = true

  /**
   * When `true` (and [enableHttp]), served pages embed a live-reload client that reconnects to a
   * websocket and refreshes the browser — restoring the current slide — whenever the server
   * restarts. Intended for local authoring only: leave `false` for published output, since with
   * [enableFileSystem] on the reload client is also written into the static pages under [outputDir].
   * Default `false`.
   */
  var devMode = false

  /**
   * When `true` (and [enableHttp]), enable follow-along presenting: the presenter opens a deck
   * with `?present=<token>` and every other visitor's browser follows the presenter's slide and
   * fragment position live, with a break-away/rejoin toggle. The presenter URLs (with the token)
   * are logged at server startup. Has no effect on filesystem output. Default `false`.
   */
  var followAlong = false

  /**
   * The presenter token for [followAlong]. Blank (the default) generates a random token per
   * launch; set it explicitly when the presenter needs a predictable URL (e.g. typing it on
   * another device). This is lightweight, demo-grade auth — the token travels in the URL.
   */
  var presenterToken = ""

  // Only the random fallback is cached — a configured presenterToken always wins, no matter
  // when it is assigned relative to the first read.
  private val randomPresenterToken by lazy { UUID.randomUUID().toString() }

  /** The effective [followAlong] presenter token: [presenterToken], or a random one per launch. */
  internal val followAlongToken: String get() = presenterToken.ifBlank { randomPresenterToken }

  /** HTTP port. Overridden at runtime by the `PORT` environment variable (Heroku convention). */
  var httpPort = 8080

  /** Directory on the classpath from which static content is served in HTTP mode. */
  var defaultHttpRoot = "public"

  /** Log level passed to Ktor's `CallLogging` plugin. */
  var callLoggingLogLevel = Level.INFO

  /** PDF export settings, consumed by the `kslides-export` module. Configured via [pdf]. */
  val pdfConfig = PdfConfig()

  /** Configure PDF export (page size, output directory, per-presentation opt-out). See [PdfConfig]. */
  fun pdf(block: PdfConfig.() -> Unit) = pdfConfig.block()

  internal val port: Int get() = System.getenv("PORT")?.toInt() ?: httpPort

  internal val playgroundPath: String get() = listOf(outputDir, playgroundDir).toPath(false)
  internal val letsPlotPath: String get() = listOf(outputDir, letsPlotDir).toPath(false)
  internal val krokiPath: String get() = listOf(outputDir, krokiDir).toPath(false)
}
