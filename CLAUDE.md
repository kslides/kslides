# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

kslides is a Kotlin DSL for the [reveal.js](https://revealjs.com) presentation framework. Presentations are authored in Markdown, HTML, or the Kotlin HTML DSL (kotlinx.html). Output can be static HTML files (for Netlify/GitHub Pages) or a dynamic HTTP server (Ktor, for Heroku).

## Build Commands

```bash
./gradlew build -x test     # Build without tests
./gradlew test              # Run tests (Kotest + JUnit 5)
./gradlew buildFatJar       # Build executable fat JAR (kslides-examples)
./gradlew exportPdf         # Print the example decks to build/pdf (-Pdeck=<name> for one)
./gradlew clean             # Clean build artifacts
./gradlew stage             # Heroku deployment build
./gradlew lintKotlin        # Lint with Kotlinter
./gradlew formatKotlin      # Auto-format with Kotlinter
./gradlew detekt            # Static analysis with Detekt
./gradlew dependencyUpdates # Check for dependency updates
```

Run a single test class:
```bash
./gradlew :kslides-core:test --tests "com.kslides.PresentationTest"
```

Run the example presentation locally:
```bash
java -jar build/libs/kslides.jar
```

### Makefile Shortcuts

Common wrappers in `Makefile`:

```bash
make help                  # list all targets (default target)
make build                 # clean + gradle build -x test
make lint                  # lintKotlinMain + lintKotlinTest
make detekt                # Detekt static analysis (fails on findings)
make tests                 # cleanTest test
make uber                  # fatjar + run the example jar
make versions              # dependencyUpdates
make dev-server            # live-reload dev loop (kslides-dev.sh: watch, rebuild, restart)
make kroki-start           # start the local Kroki diagram server (docker-compose, port 8000)
make kroki-stop            # stop the local Kroki diagram server
make pdf                   # export the example decks to build/pdf (DECK=<name> for one; kroki decks need kroki-start)
make clean-pdf             # remove build/pdf
make check-site            # uv lock --upgrade --dry-run for the docs site
make upgrade-site          # uv lock --upgrade for the docs site
make site                  # clean-site + serve the Zensical docs site
make upgrade-wrapper       # upgrade the Gradle wrapper to the libs.versions.toml version
make publish-local         # publishToMavenLocal
make publish-snapshot      # publish -SNAPSHOT to Maven Central (signed)
make publish-maven-central # release to Maven Central (signed)
```

The `publish-snapshot` and `publish-maven-central` targets sign via `GPG_ENV`, which exports three vanniktech-maven-publish env vars: `ORG_GRADLE_PROJECT_signingInMemoryKey` (armored secret key from `gpg --armor --export-secret-keys $GPG_SIGNING_KEY_ID`), `ORG_GRADLE_PROJECT_signingInMemoryKeyId` (the same key id, needed when a subkey is selected), and `ORG_GRADLE_PROJECT_signingInMemoryKeyPassword` (read from the macOS Keychain via `security find-generic-password -a gpg-signing -s gradle-signing-password`). These publish targets only work on macOS with those credentials configured. Publishing uses the [vanniktech `maven-publish`](https://github.com/vanniktech/gradle-maven-publish-plugin) plugin; signing only runs when `signingInMemoryKey` is set, so `make publish-local` and snapshot builds work without the GPG env.

### Shell scripts

Two POSIX-ish bash scripts live at the repo root (both target macOS's default bash 3.2 as well as modern Linux bash, so no `declare -A`, no `${var^^}`, and no GNU-only `sed -i`):

- `kslides-init.sh` — scaffolds a new presentation project by cloning `kslides-template`, stripping its git history, renaming the project name and presentation title, and initializing a fresh repo. Renames are guarded so a drifted template warns instead of silently no-op'ing.
- `kslides-dev.sh` — the live-reload supervisor: recompiles and restarts the app on source changes. Takes `--task` / `--watch` / `--port` (or positionals, with env vars as fallback) and defaults to the root `run` task watching `src`, matching a single-module kslides-template project.

`code-reviews/FEATURE_IDEAS.md` holds ranked product proposals (F1–F7; F1–F6 are shipped or merged, F7 — arbitrary `<head>` content — is proposed). Shipped ones carry a Status block recording what actually landed and how the open questions resolved; check it before designing a feature that might already have a written design.

### CI

`.github/workflows/ci.yml` runs on PRs to `master` and on pushes to `master`. Expect green CI before merging.

`.github/workflows/docs.yml` builds the Zensical docs site under `website/kslides/` plus the Dokka HTML and publishes them to GitHub Pages, split into a `build` job and a `deploy` job behind a `pages` concurrency group that queues rather than cancels an in-progress deployment. The published layout is: root → Zensical site, `/api-docs/` → Dokka HTML, `/docs/` → example slides.

### Releasing

The current version is `1.3.0` (tag `1.3.0`, GitHub release `v1.3.0`, published to Maven Central as `com.kslides:kslides-core`, `com.kslides:kslides-letsplot`, and — new in 1.3.0 — `com.kslides:kslides-export`); `1.0.0` was the first stable tag. To cut a new release: bump `version` in `gradle.properties`, update `CHANGELOG.md`, `RELEASE_NOTES.md`, `README.md`, `llms.txt`, and the docs site (`website/kslides/docs/installation.md`), run `make publish-maven-central`, then create a GitHub release whose tag matches the version (no `v` prefix on the tag, `v` prefix on the title).

## Module Structure

Four Gradle modules defined in `settings.gradle.kts`:

- **kslides-core** — Core DSL library: slide types, configuration, page rendering, Ktor server, filesystem output. This is what consumers depend on.
- **kslides-examples** — Example presentations with `main()` entry point in `Slides.kt` (the deck definition lives in `exampleSlides()` so the PDF-export entry point `Export.kt` — in its own `export` source set, keeping Playwright out of the fat JAR — can reuse it). Uses ShadowJar to build `kslides.jar`. Main class: `SlidesKt`; `./gradlew exportPdf [-Pdeck=<name>]` runs `ExportKt`.
- **kslides-export** — One-command PDF export (F2): `exportPdf()` serves the built presentations from an ephemeral-port Ktor server and prints each deck via headless Chromium (Playwright), waiting for reveal.js' `pdf-ready` event and Mermaid completion. Depends on kslides-core.
- **kslides-letsplot** — Lets-Plot visualization integration (JetBrains Lets-Plot). Depends on kslides-core.

### Build conventions

Shared build logic lives in `buildSrc/` as two precompiled-script convention plugins:

- `kslides.kotlin-module` — applies `kotlin("jvm")`, the JVM toolchain (read from `libs.versions.toml` via the `jvm` version key), Kotlinter, [Detekt](https://detekt.dev) (group `dev.detekt`, plugin id `dev.detekt`), the `kslides.stable-versions` convention plugin (which applies [Ben-Manes versions](https://github.com/ben-manes/gradle-versions-plugin) and rejects non-stable `dependencyUpdates` candidates), and the kotest/logback test dependencies. Detekt fails the build on findings by default (the config is valid and the tree is violation-free); pass `-Pdetekt.ignoreFailures=true` to downgrade to report-only while iterating. Honors `-PoverrideVersion=...` so snapshot builds can override the gradle.properties version.
- `kslides.published-module` — extends `kslides.kotlin-module` with `java-library`, Dokka, and `com.vanniktech.maven.publish`. Sets up the POM, `KotlinJvm` artifact (sources + Dokka HTML javadoc jar), Maven Central publication, and conditional `signAllPublications()`.

`kslides-core` and `kslides-letsplot` apply `kslides.published-module`; `kslides-examples` applies `kslides.kotlin-module` plus the Ktor plugin (which provides `application{}` and `buildFatJar`). The Heroku `stage` task lives in the root build (`build.gradle.kts`) and depends on `:kslides-examples:build` and `:kslides-examples:buildFatJar`.

## Architecture

### DSL Structure

The core DSL nests as: `kslides{}` → `presentation{}` → slide blocks. Slide blocks are `markdownSlide{}`, `htmlSlide{}`, or `dslSlide{}`, optionally grouped in `verticalSlides{}`.

The `@KSlidesDslMarker` annotation is applied to the DSL receiver types (not to functions — that's a no-op per [KT-81567](https://youtrack.jetbrains.com/issue/KT-81567)) to restrict scope and prevent incorrect nesting. `VerticalSlidesContext` is intentionally left unmarked so that `verticalSlides { markdownSlide { ... } }` / `dslSlide { ... }` / `slideDefinition(...)` resolve without needing `this@Presentation` qualifiers.

### Configuration Cascade

Configuration merges hierarchically: **global** (`kslides.presentationConfig{}`) → **presentation** (`presentation.presentationConfig{}`) → **slide** (`slideConfig{}`). Each level overrides the parent. This is implemented via `ConfigProperty` delegates in `AbstractConfig` with automatic caching and separate tracking of reveal.js vs kslides-managed values.

### Key Classes (kslides-core)

- `KSlides` — Root orchestrator. Manages presentations, output modes, and iframe content caching.
- `Presentation` — A single presentation, holds slides and config.
- `Slide` (abstract) → `MarkdownSlide`, `HtmlSlide`, `DslSlide` — Three slide types with unified interface.
- `Page` — HTML page generation and rendering for both filesystem and HTTP.
- `VerticalSlidesContext` — Context for vertically-grouped slides.
- `Mermaid` / `mermaid()` (`MermaidDsl.kt`) — Bundled-runtime Mermaid support: asset path, theme-aware init snippet, head CSS.
- `LiveReload` (`LiveReload.kt`) — `devMode` websocket route and the injected reload client.
- `FollowAlong` (`FollowAlong.kt`) — `followAlong` presenting: `/kslides-follow` websocket route (per-deck presenter/viewers/last-state, token auth, read-only viewers) and the injected presenter/viewer client with break-away/rejoin badge. Client scripts (live reload + follow-along) share one injection point in `Page`. Inline page scripts pass through an XML parser during DOM serialization — they must contain no bare `&` or `<` (see the `AMP` workaround in the follow-along client).
- `buildKSlides()` / `KSlides.startHttpServer()` / `KSlidesHttpServer` — tooling entry points: evaluate the DSL without emitting output, then serve it programmatically (port `0` = ephemeral). Used by kslides-export.
- `PresentationTheme.isDark` (`Enums.kt`) — Exhaustive dark/light theme classification; adding a theme forces a dark/light decision at compile time.
- Config classes in `com.kslides.config.*`: `KSlidesConfig`, `PresentationConfig`, `SlideConfig`, `OutputConfig`, `PdfConfig`, `ThemeConfig` (the `customTheme {}` typed-theming block: `--r-*` overrides, `baseTheme`, corner `logo()`), `PlaygroundConfig`, `MenuConfig`, `CopyCodeConfig`, `LetsPlotIframeConfig`, `DiagramConfig`.

### Dual Output System

`KSlides` supports two output modes (configured in `output{}`):
1. **Filesystem** — Writes static HTML to `/docs` directory. Playground/letsPlot/kroki content generates separate HTML files in `docs/playground/`, `docs/letsPlot/`, `docs/kroki/`.
2. **HTTP** — Ktor server with session-based iframe caching for dynamic content.

`OutputConfig.devMode` (HTTP only) adds live reload for local authoring: a `/kslides-reload` websocket plus a client script injected per-render from `Page.generateBody`. The server sends a per-JVM boot epoch on connect, so a restarted app makes the reconnecting client reload, restoring slide/fragment position from `sessionStorage` (not the URL hash — it does not depend on `hash = true`). The route and script are never installed for filesystem output or non-devMode HTTP. `KSlides` warns if `devMode` is set without `enableHttp`.

Because slide content is compiled Kotlin, a content edit requires restarting the JVM. `./gradlew -t run` does **not** work for this — Gradle continuous build cannot restart a long-running blocking server task. Use `kslides-dev.sh` (or `make dev-server`), which recompiles and restarts on source changes.

### DSL Extension Points

- `playground{}` — Embeds Kotlin Playground iframes (kslides-core, `PlaygroundDsl.kt`)
- `diagram{}` — Embeds Kroki diagrams (kslides-core, `DiagramDsl.kt`)
- `mermaid()` — Client-side Mermaid diagrams from a bundled runtime, no external service (kslides-core, `MermaidDsl.kt`)
- `letsPlot{}` — Embeds Lets-Plot figures (kslides-letsplot, `LetsPlotDsl.kt`)
- `codeSnippet{}` — Syntax-highlighted code blocks
- `include()` — Loads content from files or URLs (preferred over inline code)
- Utility functions in `Utils.kt` and `KSlidesDsl.kt`

`mermaid()` and `diagram("mermaid")` coexist deliberately: `mermaid()` is the zero-dependency default (offline-safe, renders client-side), while Kroki covers the long tail of diagram formats. The bundled Mermaid version is a checked-in browser asset under `docs/revealjs/plugin/mermaid/`, recorded as a constant in `MermaidDsl.kt` rather than in `libs.versions.toml` — bumping it means re-downloading the UMD build.

Per-render assets (the Mermaid runtime, generated code font-size classes) are emitted only for presentations that actually use the feature, tracked with per-render flags on `Presentation`. Follow that pattern when adding a new asset-bearing extension.

### Testing

For testing, use `kslidesTest{}` instead of `kslides{}` — it suppresses filesystem and HTTP output. Test classes use Kotest 6 (`StringSpec()` + `init {}` block) with the JUnit 5 runner. Tests live under:

- `kslides-core/src/test/kotlin/com/kslides/` — `UtilsTest`, `PresentationTest`, `ConfigsTest`, `OutputConfigTest`, `MermaidTest`, `LiveReloadTest`, `FollowAlongTest`, `FollowAlongServerTest` (real websocket integration: presenter/viewer sync, token auth, takeover, abrupt disconnect, 40-viewer broadcast), `SlideFontSizeTest`, and others.
- `kslides-core/src/test/kotlin/website/` — compilable sources for the docs-site snippets, pulled into the Zensical pages via `--8<--` includes (e.g. `Mermaid.kt`, `Output.kt`, `Styling.kt`). Adding a docs snippet means adding it here so it stays compiler-checked.
- `kslides-letsplot/src/test/kotlin/com/kslides/` — `LetsPlotTest` (renderer unit tests) and `LetsPlotDslTest` (full DSL → filesystem integration, writing to a temp `outputDir`). The letsplot test source set ships its own empty `src/test/resources/slides.css` so `Page.generateHead`'s classpath lookup succeeds without depending on kslides-core test resources.
- `kslides-export/src/test/kotlin/` — `PdfExportUtilsTest` (deck naming/filtering and PDF options) plus `PdfExportIntegrationTest`, an opt-in end-to-end browser test (`KSLIDES_EXPORT_TEST=true`; skipped by default since it needs a headless browser). Ships its own empty `src/test/resources/slides.css` (letsplot pattern), and `src/test/kotlin/website/` holds the PDF-export docs snippets (the Zensical snippets `base_path` lists both this dir and kslides-core's).

## Tech Stack

- Kotlin 2.4.10, JVM 17 toolchain
- Gradle Kotlin DSL (`*.gradle.kts`), wrapper 9.7.0
- Ktor 3.5.2 (server + client)
- kotlinx.html / kotlinx.css for HTML/CSS DSL
- Lets-Plot Kotlin 4.15.0 for the `letsPlot{}` DSL (JS runtime version configurable via `KSlidesConfig.letsPlotJsVersion`, default `4.10.1`)
- Playwright Java (kslides-export only) for driving headless Chromium during PDF export
- Kotlinter for linting (ktlint-based) and Detekt 2.0.0-alpha.6 for static analysis (`dev.detekt`), fatal on findings by default (`-Pdetekt.ignoreFailures=true` to downgrade)
- reveal.js assets live at the repo root in `docs/revealjs/` (single source of truth, committed for GitHub Pages). `kslides-core/build.gradle.kts` grafts them onto the published JAR's classpath at `revealjs/**` via `processResources` so the Ktor static handler can serve them at runtime — there is no checked-in `kslides-core/src/main/resources/revealjs/` directory.
- All versions — including the JVM toolchain (`jvm`) and the Gradle wrapper distribution (`gradle-wrapper`) — are centralized in `gradle/libs.versions.toml`. The convention plugin reads `jvm` via `VersionCatalogsExtension`, and the `Makefile`'s `upgrade-wrapper` target reads `gradle-wrapper` from the same file.

## Important Notes

- CSS in presentation content is space-sensitive — do not auto-reformat generated HTML files.
- Static content: HTTP mode serves from `src/main/resources/public/`; filesystem mode uses `/docs`.
- Run `./gradlew clean build` after changing `slides.css` or files in `src/main/resources/public/`.
- Slide functions (`dslSlide{}`, `markdownSlide{}`, `htmlSlide{}`) have two variants depending on whether they're inside a `VerticalSlidesContext` — see `ExtensionExample.kt`.
