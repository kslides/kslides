package com.kslides.config

import com.kslides.KSlidesDslMarker

/**
 * Settings for PDF export, configured via the `pdf {}` block inside [com.kslides.KSlides.output].
 *
 * kslides-core only carries the configuration data; the export itself is performed by the
 * `exportPdf()` function in the `kslides-export` module, which serves the presentations from an
 * ephemeral-port HTTP server and prints each one through headless Chromium.
 */
@KSlidesDslMarker
class PdfConfig {
  /** Directory the exported PDF (and optional PNG) files are written to. Default `"build/pdf"`. */
  var outputDir = "build/pdf"

  /**
   * When `true`, also capture a PNG screenshot of each presentation's first slide next to the
   * PDF — useful as a social-preview / Open Graph image. Default `false`.
   */
  var previewPng = false

  /**
   * Browser channel passed to Playwright. Empty (the default) uses Playwright's bundled Chromium,
   * which is downloaded on first use. Set to an installed browser such as `"chrome"` or `"msedge"`
   * to skip the browser download entirely.
   */
  var browserChannel = ""

  /**
   * Maximum time to wait for a presentation to become ready for printing (reveal.js initialized
   * and all Mermaid diagrams rendered). Default 30 seconds.
   */
  var readyTimeoutMillis = 30_000L

  /**
   * Extra delay after the readiness checks pass, giving asynchronous content (iframes, fonts,
   * animations) time to settle before printing. Default 1 second.
   */
  var settleMillis = 1_000L

  /**
   * Explicit PDF page width as a CSS length (e.g. `"11in"`, `"297mm"`). When [pageWidth] and
   * [pageHeight] are both empty (the default), the page size comes from the presentation's own
   * print stylesheet (reveal.js sizes pages to match the configured slide dimensions).
   */
  var pageWidth = ""

  /** Explicit PDF page height as a CSS length. See [pageWidth]. */
  var pageHeight = ""

  /**
   * Presentations excluded from export. Entries match [com.kslides.Presentation.path] values,
   * with or without a leading `/` or `.html` suffix (`"demo"` and `"/demo.html"` are equivalent).
   * An explicit single-deck export request bypasses this list.
   */
  val excludes = mutableListOf<String>()

  /** Add one or more presentation paths to [excludes]. */
  fun exclude(vararg paths: String) {
    excludes += paths
  }
}
