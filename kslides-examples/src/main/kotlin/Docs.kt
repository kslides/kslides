import com.kslides.kslides

/**
 * Writes the example presentations to `docs/` and exits. [main] in `Slides.kt` writes the same
 * files but then starts the HTTP server and blocks. Run with `./gradlew :kslides-examples:writeDocs`,
 * or `make docs`.
 *
 * Note: the example deck renders its Kroki diagrams through the local Kroki server while writing
 * (`make kroki-start`); without it, the render fails.
 */
fun main() {
  kslides {
    exampleSlides()(this)

    // output{} replaces the deck's own output block rather than amending it, so this turns off
    // HTTP (and with it followAlong) instead of inheriting them.
    output {
      enableFileSystem = true
      enableHttp = false
    }
  }
}
