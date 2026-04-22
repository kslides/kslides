package com.kslides.config

import com.kslides.KSlides
import com.kslides.KSlidesDslMarker
import com.pambrose.common.util.toPath
import org.slf4j.event.Level

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

  /** Directory containing the bundled reveal.js static assets (relative to the classpath). */
  var staticRootDir = "revealjs"

  /** Subdirectory of [outputDir] where Kotlin Playground iframe HTML files are written. */
  var playgroundDir = "playground"

  /** Subdirectory of [outputDir] where Lets-Plot iframe HTML files are written. */
  var letsPlotDir = "letsPlot"

  /** Subdirectory of [outputDir] where Kroki diagram images are written. */
  var krokiDir = "kroki"

  /** When `true`, start a Ktor HTTP server serving the presentations. Default `true`. */
  var enableHttp = true

  /** HTTP port. Overridden at runtime by the `PORT` environment variable (Heroku convention). */
  var httpPort = 8080

  /** Directory on the classpath from which static content is served in HTTP mode. */
  var defaultHttpRoot = "public"

  /** Log level passed to Ktor's `CallLogging` plugin. */
  var callLoggingLogLevel = Level.INFO

  internal val port: Int get() = System.getenv("PORT")?.toInt() ?: httpPort

  internal val playgroundPath: String get() = listOf(outputDir, playgroundDir).toPath(false)
  internal val letsPlotPath: String get() = listOf(outputDir, letsPlotDir).toPath(false)
  internal val krokiPath: String get() = listOf(outputDir, krokiDir).toPath(false)
}
