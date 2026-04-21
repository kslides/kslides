# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

kslides is a Kotlin DSL for the [reveal.js](https://revealjs.com) presentation framework. Presentations are authored in Markdown, HTML, or the Kotlin HTML DSL (kotlinx.html). Output can be static HTML files (for Netlify/GitHub Pages) or a dynamic HTTP server (Ktor, for Heroku).

## Build Commands

```bash
./gradlew build -xtest     # Build without tests
./gradlew test              # Run tests (Kotest + JUnit 5)
./gradlew uberjar           # Build executable uber JAR (kslides-examples)
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

## Module Structure

Three Gradle modules defined in `settings.gradle`:

- **kslides-core** — Core DSL library: slide types, configuration, page rendering, Ktor server, filesystem output. This is what consumers depend on.
- **kslides-examples** — Example presentations with `main()` entry point in `Slides.kt`. Uses ShadowJar to build `kslides.jar`. Main class: `SlidesKt`.
- **kslides-letsplot** — Lets-Plot visualization integration (JetBrains Lets-Plot). Depends on kslides-core.

## Architecture

### DSL Structure

The core DSL nests as: `kslides{}` → `presentation{}` → slide blocks. Slide blocks are `markdownSlide{}`, `htmlSlide{}`, or `dslSlide{}`, optionally grouped in `verticalSlides{}`.

The `@KSlidesDslMarker` annotation restricts DSL scope to prevent incorrect nesting.

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

For testing, use `kslidesTest{}` instead of `kslides{}` — it suppresses output. Tests are in `kslides-core/src/test/kotlin/` using Kotest 6 with JUnit 5 runner.

## Tech Stack

- Kotlin 2.2.0, JVM 17 toolchain
- Gradle (Groovy DSL, not Kotlin DSL)
- Ktor 3.2.0 (server + client)
- kotlinx.html / kotlinx.css for HTML/CSS DSL
- Kotlinter for linting (ktlint-based)
- reveal.js embedded in `kslides-core/src/main/resources/revealjs/`
- Dependency versions centralized in `gradle.properties`

## Important Notes

- CSS in presentation content is space-sensitive — do not auto-reformat generated HTML files.
- Static content: HTTP mode serves from `src/main/resources/public/`; filesystem mode uses `/docs`.
- Run `./gradlew clean build` after changing `slides.css` or files in `src/main/resources/public/`.
- Slide functions (`dslSlide{}`, `markdownSlide{}`, `htmlSlide{}`) have two variants depending on whether they're inside a `VerticalSlidesContext` — see `ExtensionExample.kt`.
