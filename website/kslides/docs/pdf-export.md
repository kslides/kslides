---
icon: lucide/printer
---

# PDF export

"Send me the deck" used to mean a manual ritual: start the server, open Chrome, append `?print-pdf`, print to file, check the margins. The `kslides-export` module turns that into one command.

## One command

```bash
./gradlew exportPdf                 # every presentation -> build/pdf/<deck>.pdf
./gradlew exportPdf -Pdeck=demo     # just one presentation
```

or, in the kslides repo itself:

```bash
make pdf                # all decks
make pdf DECK=demo      # one deck
```

Under the hood, `exportPdf()` builds the presentations with [`buildKSlides`](kdocs.md), serves them from the same Ktor server HTTP mode uses (on an ephemeral port), and drives headless Chromium through [Playwright](https://playwright.dev/java/): each deck is loaded with reveal.js' `?print-pdf` parameter, the export waits for reveal's `pdf-ready` event (plus any Mermaid diagrams), and prints the page with the deck's own print stylesheet controlling the page size.

## Configuration

PDF export is configured in the `pdf { }` block inside `output { }`:

```kotlin
--8<-- "PdfExport.kt:config"
```

Two of these are easy to miss. `outputDir` is where the PDFs land (`build/pdf` by default), and
`previewPng` additionally captures a PNG of each deck's first slide — the image you want for a
social-media / Open Graph preview. kslides does not yet write the `<meta property="og:image">` tag
that would point at it, so for now reference the file from whatever publishes the deck.

All settings are optional. By default the page size comes from the presentation's own print CSS (reveal.js sizes pages to match the slide dimensions); set `pageWidth` / `pageHeight` (e.g. `"11in"`, `"297mm"`) to force a paper size instead.

Two timing knobs are available for decks that need them:

- `readyTimeoutMillis` (default 30 s) caps how long the export waits for a deck to become printable — reveal.js initialized and every Mermaid diagram rendered. Raise it for very large decks.
- `settleMillis` (default 1 s) is an extra pause after those checks pass, giving asynchronous content such as `playground { }` iframes time to finish. Set it to `0` to skip the pause on decks that have none.

## Wiring it into a deck project

Add the dependency and a small entry point that reuses your deck definition:

```kotlin
dependencies {
    implementation("com.kslides:kslides-export:$version")
}
```

```kotlin
--8<-- "PdfExport.kt:export"
```

`exportPdf()` accepts the same block as `kslides { }`, so the cleanest setup is to extract your deck definition into a function that both `main()` and the export entry point call — see `Export.kt` in `kslides-examples`, which also keeps the Playwright dependency in a separate source set so it stays out of the runnable fat JAR.

## Browsers

On first use Playwright downloads its own browsers (cached per user, so this happens once). Two ways to avoid the download entirely:

- Set `browserChannel = "chrome"` (or `"msedge"`) to drive an installed browser.
- Pre-install just Chromium in CI: `mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"` (or the Gradle equivalent).

## Caveats

- Content fetched at render time still needs its backing service: Kroki `diagram { }` slides need the configured Kroki server (the kslides example decks use the local docker-compose server from `make kroki-start`), and `playground { }` iframes need network access. Native [`mermaid()`](extensions/mermaid.md) diagrams render offline.
- A deck that fails to render is reported (with the failing deck named) after the remaining decks have been attempted.
