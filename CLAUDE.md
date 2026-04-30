# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

kslides is a Kotlin DSL for the [reveal.js](https://revealjs.com) presentation framework. Presentations are authored in Markdown, HTML, or the Kotlin HTML DSL (kotlinx.html). Output can be static HTML files (for Netlify/GitHub Pages) or a dynamic HTTP server (Ktor, for Heroku).

## Build Commands

```bash
./gradlew build -xtest     # Build without tests
./gradlew test              # Run tests (Kotest + JUnit 5)
./gradlew buildFatJar       # Build executable fat JAR (kslides-examples)
./gradlew clean             # Clean build artifacts
./gradlew stage             # Heroku deployment build
./gradlew lintKotlin        # Lint with Kotlinter
./gradlew formatKotlin      # Auto-format with Kotlinter
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
make build                 # clean + gradle build -xtest
make lint                  # lintKotlinMain + lintKotlinTest
make tests                 # --rerun-tasks check
make uber                  # fatjar + run the example jar
make versioncheck          # dependencyUpdates (default target)
make publish-local         # publishToMavenLocal
make publish-snapshot      # publish -SNAPSHOT to Maven Central (signed)
make publish-maven-central # release to Maven Central (signed)
```

The `publish-snapshot` and `publish-maven-central` targets sign via `GPG_ENV`, which exports three vanniktech-maven-publish env vars: `ORG_GRADLE_PROJECT_signingInMemoryKey` (armored secret key from `gpg --armor --export-secret-keys $GPG_SIGNING_KEY_ID`), `ORG_GRADLE_PROJECT_signingInMemoryKeyId` (the same key id, needed when a subkey is selected), and `ORG_GRADLE_PROJECT_signingInMemoryKeyPassword` (read from the macOS Keychain via `security find-generic-password -a gpg-signing -s gradle-signing-password`). These publish targets only work on macOS with those credentials configured. Publishing uses the [vanniktech `maven-publish`](https://github.com/vanniktech/gradle-maven-publish-plugin) plugin; signing only runs when `signingInMemoryKey` is set, so `make publish-local` and snapshot builds work without the GPG env.

### CI

`.github/workflows/ci.yml` runs on PRs to `master` and on pushes to `master`. Expect green CI before merging.

`.github/workflows/docs.yml` builds the Zensical docs site under `website/kslides/` plus the Dokka HTML and publishes them to GitHub Pages. The published layout is: root → Zensical site, `/api-docs/` → Dokka HTML, `/docs/` → example slides.

### Releasing

The first stable release is `1.0.0` (tag `1.0.0`, GitHub release `v1.0.0`, published to Maven Central as `com.kslides:kslides-core` and `com.kslides:kslides-letsplot`). To cut a new release: bump `version` in `gradle.properties`, update `CHANGELOG.md` and `RELEASE_NOTES.md`, run `make publish-maven-central`, then create a GitHub release whose tag matches the version (no `v` prefix on the tag, `v` prefix on the title).

## Module Structure

Three Gradle modules defined in `settings.gradle.kts`:

- **kslides-core** — Core DSL library: slide types, configuration, page rendering, Ktor server, filesystem output. This is what consumers depend on.
- **kslides-examples** — Example presentations with `main()` entry point in `Slides.kt`. Uses ShadowJar to build `kslides.jar`. Main class: `SlidesKt`.
- **kslides-letsplot** — Lets-Plot visualization integration (JetBrains Lets-Plot). Depends on kslides-core.

### Build conventions

Shared build logic lives in `buildSrc/` as two precompiled-script convention plugins:

- `kslides.kotlin-module` — applies `kotlin("jvm")`, JVM 17 toolchain, Kotlinter, stable-versions, and the kotest/logback test dependencies. Honors `-PoverrideVersion=...` so snapshot builds can override the gradle.properties version.
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
- Config classes in `com.kslides.config.*`: `KSlidesConfig`, `PresentationConfig`, `SlideConfig`, `OutputConfig`, `PlaygroundConfig`, `MenuConfig`, `CopyCodeConfig`, `LetsPlotIframeConfig`, `DiagramConfig`.

### Dual Output System

`KSlides` supports two output modes (configured in `output{}`):
1. **Filesystem** — Writes static HTML to `/docs` directory. Playground/letsPlot/kroki content generates separate HTML files in `docs/playground/`, `docs/letsPlot/`, `docs/kroki/`.
2. **HTTP** — Ktor server with session-based iframe caching for dynamic content.

### DSL Extension Points

- `playground{}` — Embeds Kotlin Playground iframes (kslides-core, `PlaygroundDsl.kt`)
- `diagram{}` — Embeds Kroki diagrams (kslides-core, `DiagramDsl.kt`)
- `letsPlot{}` — Embeds Lets-Plot figures (kslides-letsplot, `LetsPlotDsl.kt`)
- `codeSnippet{}` — Syntax-highlighted code blocks
- `include()` — Loads content from files or URLs (preferred over inline code)
- Utility functions in `Utils.kt` and `KSlidesDsl.kt`

### Testing

For testing, use `kslidesTest{}` instead of `kslides{}` — it suppresses filesystem and HTTP output. Test classes use Kotest 6 (`StringSpec()` + `init {}` block) with the JUnit 5 runner. Tests live under:

- `kslides-core/src/test/kotlin/com/kslides/` — `UtilsTest`, `PresentationTest`, `ConfigsTest`, `OutputConfigTest`.
- `kslides-letsplot/src/test/kotlin/com/kslides/` — `LetsPlotTest` (renderer unit tests) and `LetsPlotDslTest` (full DSL → filesystem integration, writing to a temp `outputDir`). The letsplot test source set ships its own empty `src/test/resources/slides.css` so `Page.generateHead`'s classpath lookup succeeds without depending on kslides-core test resources.

## Tech Stack

- Kotlin 2.3.21, JVM 17 toolchain
- Gradle Kotlin DSL (`*.gradle.kts`), wrapper 9.5.0
- Ktor 3.4.3 (server + client)
- kotlinx.html / kotlinx.css for HTML/CSS DSL
- Lets-Plot Kotlin 4.13.0 for the `letsPlot{}` DSL (matched JS runtime v4.9.0, configurable via `KSlidesConfig.letsPlotJsVersion`)
- Kotlinter for linting (ktlint-based)
- reveal.js assets live at the repo root in `docs/revealjs/` (single source of truth, committed for GitHub Pages). `kslides-core/build.gradle.kts` grafts them onto the published JAR's classpath at `revealjs/**` via `processResources` so the Ktor static handler can serve them at runtime — there is no checked-in `kslides-core/src/main/resources/revealjs/` directory.
- Dependency versions centralized in `gradle/libs.versions.toml`

## Important Notes

- CSS in presentation content is space-sensitive — do not auto-reformat generated HTML files.
- Static content: HTTP mode serves from `src/main/resources/public/`; filesystem mode uses `/docs`.
- Run `./gradlew clean build` after changing `slides.css` or files in `src/main/resources/public/`.
- Slide functions (`dslSlide{}`, `markdownSlide{}`, `htmlSlide{}`) have two variants depending on whether they're inside a `VerticalSlidesContext` — see `ExtensionExample.kt`.
