package com.kslides.export

import com.kslides.KSlides
import com.kslides.buildKSlides
import com.kslides.config.PdfConfig
import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.PlaywrightException
import com.microsoft.playwright.options.WaitUntilState
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

// Records reveal.js' pdf-ready event, which fires once the print view is fully assembled —
// including the injected @page size rule that page.pdf()'s preferCSSPageSize picks up.
// Installed before any page script runs so the event can never be missed. (reveal dispatches
// its events with bubbling enabled, so a capturing window listener sees it.)
private const val INIT_SCRIPT =
  """window.__kslidesPdfReady = false;
     window.addEventListener('pdf-ready', () => { window.__kslidesPdfReady = true; }, true);"""

private const val PRINT_READY_FUNCTION = "() => window.__kslidesPdfReady === true"

// Screen-view readiness (used for the preview PNG, where pdf-ready never fires).
private const val VIEW_READY_FUNCTION = "() => window.Reveal !== undefined && Reveal.isReady()"

// Every Mermaid diagram has rendered (Mermaid marks each processed element with data-processed).
// Mirrors the markup and marker kslides-core emits in MermaidDsl.kt; core re-runs its own sweep on
// pdf-ready, so the export only has to wait for the result. Vacuously true on Mermaid-free decks.
private const val MERMAID_DONE_FUNCTION =
  "() => document.querySelectorAll('pre.mermaid:not([data-processed])').length === 0"

/**
 * Builds the presentations defined in [kslidesBlock] and prints each one to PDF via headless
 * Chromium — kslides' one-command PDF export.
 *
 * The presentations are served from the same Ktor server that HTTP output mode uses, bound to an
 * ephemeral port, and each deck is loaded with reveal.js' `?print-pdf` query parameter before
 * being printed with [Playwright](https://playwright.dev/java/). Output location, page size,
 * per-presentation opt-out, and browser selection are configured via the `pdf {}` block inside
 * `output {}` (see [PdfConfig]).
 *
 * On first use, Playwright downloads its bundled browsers (cached per user); set
 * [PdfConfig.browserChannel] to `"chrome"` or `"msedge"` to use an installed browser instead and
 * skip the download.
 *
 * @param deck when non-null, export only the presentation whose path matches this name
 *   (`"demo"`, `"demo.html"`, and `"/demo.html"` are equivalent; the root presentation is
 *   `"index"`). Bypasses [PdfConfig.excludes]. When null, all non-excluded presentations are
 *   exported.
 * @param kslidesBlock the same configuration block accepted by [com.kslides.kslides].
 * @return the files written, one PDF per exported presentation (plus one PNG per presentation
 *   when [PdfConfig.previewPng] is enabled).
 * @throws IllegalArgumentException if no presentation matches [deck], or all are excluded.
 * @throws IllegalStateException if one or more presentations fail to export; the remaining
 *   presentations are still attempted first.
 */
fun exportPdf(
  deck: String? = null,
  kslidesBlock: KSlides.() -> Unit,
): List<File> = buildKSlides(kslidesBlock).use { kslides -> exportPresentations(kslides, deck) }

private fun exportPresentations(
  kslides: KSlides,
  deck: String?,
): List<File> {
  val pdfConfig = kslides.outputConfig.pdfConfig
  val paths = selectDecks(kslides.presentationPaths, pdfConfig.excludes, deck)

  require(paths.isNotEmpty()) {
    val available = kslides.presentationPaths.joinToString { deckName(it) }
    if (deck.isNullOrBlank())
      "All presentations are excluded from PDF export (available: $available)"
    else
      "No presentation matches deck \"$deck\" (available: $available)"
  }

  val outputDir = File(pdfConfig.outputDir).apply { mkdirs() }

  // The browser starts first: on a cold run Playwright downloads Chromium here, and there is no
  // point holding a bound port open for that.
  return createPlaywright(pdfConfig).use { playwright ->
    launchBrowser(playwright, pdfConfig).use { browser ->
      kslides.startHttpServer(port = 0).use { server ->
        logger.info { "Exporting ${paths.size} presentation(s) to $outputDir" }
        val page = browser.newPage().apply { addInitScript(INIT_SCRIPT) }
        exportDecks(page, "http://localhost:${server.port}", paths, pdfConfig, outputDir)
      }
    }
  }
}

private fun exportDecks(
  page: Page,
  baseUrl: String,
  paths: List<String>,
  config: PdfConfig,
  outputDir: File,
): List<File> {
  val written = mutableListOf<File>()
  val failed = mutableListOf<String>()

  paths.forEach { path ->
    try {
      written += exportDeck(page, baseUrl, path, config, outputDir)
    } catch (e: PlaywrightException) {
      val name = deckName(path)
      logger.error(e) { "Failed to export presentation \"$name\"" }
      failed += name
    }
  }

  check(failed.isEmpty()) { "PDF export failed for: ${failed.joinToString()}" }
  return written
}

private fun exportDeck(
  page: Page,
  baseUrl: String,
  path: String,
  config: PdfConfig,
  outputDir: File,
): List<File> {
  val name = deckName(path)
  val deckUrl = "$baseUrl$path"
  val files = mutableListOf<File>()

  if (config.previewPng) {
    navigateAndAwait(page, deckUrl, config, printView = false)
    val png = File(outputDir, "$name.png")
    page.screenshot(Page.ScreenshotOptions().setPath(png.toPath()))
    logger.info { "Wrote $png" }
    files += png
  }

  navigateAndAwait(page, deckUrl, config, printView = true)
  val pdf = File(outputDir, "$name.pdf")
  page.pdf(pdfOptions(config, pdf))
  logger.info { "Wrote $pdf" }
  files += pdf

  return files
}

private fun navigateAndAwait(
  page: Page,
  deckUrl: String,
  config: PdfConfig,
  printView: Boolean,
) {
  // Wait for DOMContentLoaded rather than load: decks with external iframes (e.g. Kotlin
  // Playground) may keep the window load event pending indefinitely, while reveal.js
  // initializes as soon as the document itself is parsed.
  page.navigate(
    if (printView) "$deckUrl?print-pdf" else deckUrl,
    Page
      .NavigateOptions()
      .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
      .setTimeout(config.readyTimeoutMillis.toDouble()),
  )
  page.awaitFunction(if (printView) PRINT_READY_FUNCTION else VIEW_READY_FUNCTION, config)
  page.awaitFunction(MERMAID_DONE_FUNCTION, config)
  page.evaluate("() => document.fonts.ready")
  if (config.settleMillis > 0)
    page.waitForTimeout(config.settleMillis.toDouble())
}

private fun Page.awaitFunction(
  function: String,
  config: PdfConfig,
) {
  waitForFunction(
    function,
    null,
    Page.WaitForFunctionOptions().setTimeout(config.readyTimeoutMillis.toDouble()),
  )
}

private fun createPlaywright(config: PdfConfig): Playwright =
  if (config.browserChannel.isEmpty()) {
    Playwright.create() // downloads the bundled browsers on first use
  } else {
    // An installed browser was requested — skip Playwright's browser download entirely.
    Playwright.create(
      Playwright.CreateOptions().setEnv(mapOf("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD" to "1")),
    )
  }

private fun launchBrowser(
  playwright: Playwright,
  config: PdfConfig,
): Browser =
  playwright
    .chromium()
    .launch(
      BrowserType
        .LaunchOptions()
        .apply { if (config.browserChannel.isNotEmpty()) setChannel(config.browserChannel) },
    )

/**
 * The output/filter name for a presentation path: `"/"` → `"index"`, `"/demo.html"` → `"demo"`,
 * `"/sub/deck"` → `"sub-deck"`.
 */
internal fun deckName(path: String): String =
  path
    .trim()
    .removePrefix("/")
    .removeSuffix(".html")
    .replace('/', '-')
    .ifEmpty { "index" }

/**
 * The presentation paths to export: with an explicit [deck] request, exactly the matching paths
 * (ignoring [excludes]); otherwise all paths minus [excludes].
 */
internal fun selectDecks(
  paths: List<String>,
  excludes: List<String>,
  deck: String?,
): List<String> {
  val wanted = deck?.trim()?.takeUnless { it.isEmpty() }?.let(::deckName)
  return if (wanted != null) {
    paths.filter { deckName(it) == wanted }
  } else {
    val excluded = excludes.map(::deckName).toSet()
    paths.filterNot { deckName(it) in excluded }
  }
}

/** Playwright PDF options for [config]: explicit page size when given, else the deck's own print CSS. */
internal fun pdfOptions(
  config: PdfConfig,
  output: File,
): Page.PdfOptions =
  Page
    .PdfOptions()
    .setPath(output.toPath())
    .setPrintBackground(true)
    .apply {
      if (config.pageWidth.isEmpty() && config.pageHeight.isEmpty()) {
        setPreferCSSPageSize(true)
      } else {
        if (config.pageWidth.isNotEmpty()) setWidth(config.pageWidth)
        if (config.pageHeight.isNotEmpty()) setHeight(config.pageHeight)
      }
    }
