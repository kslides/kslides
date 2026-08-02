import com.kslides.export.exportPdf

/**
 * PDF-export entry point for the example presentations: prints each deck to `build/pdf` via
 * headless Chromium. Run with `./gradlew exportPdf` (optionally `-Pdeck=<name>`), or `make pdf`.
 *
 * Lives in the `export` source set so the Playwright dependency stays out of the runnable fat JAR.
 *
 * Note: the example deck renders its Kroki diagrams through the same local Kroki server that
 * `run` uses (`make kroki-start`); without it, the Kroki-based decks fail to export.
 */
fun main() {
  val deck: String? = System.getProperty("kslides.export.deck")
  exportPdf(deck, exampleSlides())
}
