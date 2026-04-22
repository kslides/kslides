package com.kslides.config

import com.kslides.CssFile
import com.kslides.JsFile
import com.kslides.KSlidesDslMarker
import com.kslides.StaticRoot
import kotlin.time.Duration.Companion.seconds

/**
 * Cross-cutting configuration applied to every presentation. Set via [com.kslides.KSlides.kslidesConfig].
 */
@KSlidesDslMarker
class KSlidesConfig {
  /**
   * Static asset directories served from reveal.js's bundled content. Additional entries here
   * expose more subdirectories of `src/main/resources/revealjs/` over HTTP.
   */
  val httpStaticRoots =
    mutableListOf(
      StaticRoot("assets"),
      StaticRoot("css"),
      StaticRoot("dist"),
      StaticRoot("js"),
      StaticRoot("plugin"),
    )

  /**
   * Base stylesheets injected into every presentation's `<head>`. Theme and highlight
   * stylesheets are appended separately based on [PresentationConfig.theme] / `highlight`.
   */
  val cssFiles =
    mutableListOf(
      CssFile("dist/reveal.css"),
      CssFile("dist/reset.css"),
    )

  /** Base JavaScript files injected into every presentation. Reveal.js plugin scripts are appended separately. */
  val jsFiles =
    mutableListOf(
      JsFile("dist/reveal.js"),
    )

  /** CSS class used by Kotlin Playground to mark editable code blocks. */
  var playgroundSelector = "playground-code"

  /** URL of the Kotlin Playground runtime loaded inside generated Playground iframes. */
  var playgroundUrl = "https://unpkg.com/kotlin-playground@1"

  /**
   * Lets-Plot JS runtime version loaded by generated `letsPlot{}` iframes. Must be compatible
   * with the `lets-plot-kotlin-jvm` artifact on the classpath (e.g. `lets-plot-kotlin-jvm:4.13.0`
   * → Lets-Plot core `4.9.0`). Overriding here avoids a library release when a compatible JS
   * runtime is published upstream.
   */
  var letsPlotJsVersion = "4.9.0"

  /** Kroki server URL used by [com.kslides.diagram] to render diagrams. */
  var krokiUrl = "https://kroki.io"

  /**
   * Per-request timeout for the Ktor HTTP client used for outbound calls (Kroki, other diagram
   * services). Increase if Kroki takes longer than 30 s on your network.
   */
  var clientHttpTimeout = 30.seconds
}