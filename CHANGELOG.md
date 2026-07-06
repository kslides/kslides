# Changelog

All notable changes to kslides are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Entries for releases prior to 1.0.0 are reconstructed from the git history.

## [Unreleased]

### Added

- `slideConfig { }` gained three cascading font properties: `fontSize` (inline
  `font-size` on the slide's `<section>` — any CSS length), `codeFontSize`
  (font size for `<pre>` code blocks, emitted as a generated CSS class + head
  rule), and `codeWrap` (wrap long code lines instead of overflowing). All
  three resolve through the global → presentation → slide cascade, replacing
  the raw-CSS approach previously needed for code sizing.
- `slideDefinition()` (both overloads) gained a trailing
  `configBlock: SlideConfig.() -> Unit` parameter for per-slide configuration,
  e.g. `slideDefinition(source, token) { codeFontSize = "0.40em" }`.

## [1.1.1] — 2026-07-05

Expands the CopyCode plugin configuration and rebuilds it on a typed,
serializable model.

### Added

- `copyCodeConfig { }` gained `button` (`CopyCodeButton`: `ALWAYS` / `HOVER` /
  `FALSE`), `display` (`CopyCodeDisplay`: `TEXT` / `ICONS` / `BOTH`),
  `plaintextonly`, `copyborder` / `copiedborder`, `scale` / `offset` / `radius`,
  `window`, and `tooltip` — on top of the existing `copy` / `copied` / `timeout`
  and button-color options.
- `CopyCodeButton` and `CopyCodeDisplay` enums (in `com.kslides.config`) whose
  serialized form is the plugin's wire value (`"always"`, `"icons"`, …) rather
  than the enum constant name.

### Changed

- The `copycode: { … }` reveal.js block is now produced from a typed,
  `@Serializable` model via `kotlinx.serialization` instead of a hand-built
  string map. `scale` / `offset` / `radius` are `Double`, so fractional em/scale
  values work (the plugin's own default `scale` is `0.8`).
- `copyCodeConfig { }` no longer accepts the raw `revealjsOption(key, value)`
  escape hatch — it exposes every copycode option as a typed property, so a
  `revealjsOption(…)` call inside a `copyCodeConfig { }` block is now a
  compile-time error. Other config blocks are unaffected.

## [1.1.0] — 2026-07-03

Quality release from a full code-review pass: correctness and error-handling
fixes, thread-safe rendering, new type-safe DSL options, and a tightened public
API. Two breaking changes (config-cascade visibility, `VerticalSlide`) — see
below.

### Added

- `AbstractConfig.revealjsOption(key, value)` — set a raw
  [reveal.js option](https://revealjs.com/config/) that kslides does not model
  as a typed property (e.g. the menu plugin's `themes` array). It is emitted
  verbatim into `Reveal.initialize({…})` and participates in the config cascade.
  Replaces the previously-public map-mutation escape hatch.
- `DiagramType` enum (28 Kroki types) and a `diagram(DiagramType, …)` overload,
  so a mistyped diagram type is a compile error instead of a far-away Kroki 400.
  The raw-`String` overload is retained for types not in the enum.
- `letsPlot { … }` gained a `configBlock: LetsPlotIframeConfig.() -> Unit`
  overload, symmetric with `playground{}` / `diagram{}`.
- `slideDefinition(…)` gained optional `githubAccount` / `githubRepo` /
  `githubPath` / `githubBranch` parameters so the "GitHub Source" link points at
  a consumer's own repository (`githubPath` defaults to `source`).
- `CodeSnippetConfig.lineOffset` — canonical spelling of the starting line
  number.
- `KSlides` now implements `AutoCloseable`, closing the lazily-created Ktor
  `HttpClient`; usable with `kslides { … }.use { }`.
- Detekt static analysis wired into the `kslides.kotlin-module` convention
  plugin (Detekt `2.0.0-alpha.5`, group `dev.detekt`, plugin id `dev.detekt`).
  All Kotlin modules expose `detekt*` tasks; `make detekt` shortcut.

### Changed

- **BREAKING** — the config-cascade internals are no longer public:
  `AbstractConfig.revealjsManagedValues` / `kslidesManagedValues` and the
  `ConfigProperty` delegate are now `internal`. Configure options through the
  typed properties, and use `revealjsOption(…)` for un-modeled raw options.
  `AbstractConfig.merge(…)` remains public.
- **BREAKING** — `VerticalSlide` is now `abstract` (the marker base for the
  slides inside a `verticalSlides{}` block). The stack wrapper is a new concrete
  `VerticalSlideStack`; vertical child slides no longer allocate an unused
  `VerticalSlidesContext`. Rendered output is byte-identical.
- Page rendering is now thread-safe: `generatePage` holds a per-`KSlides`
  `renderLock`, so the concurrent renders a Ktor server performs can no longer
  interleave the shared per-render state (slide counter, reconstructed vertical
  stacks, iframe counters). Output is unchanged.
- Detekt now **fails the build on findings by default** (was non-fatal). Pass
  `-Pdetekt.ignoreFailures=true` to downgrade to report-only while iterating.
- `include()` now fails loudly on authoring errors (a missing begin/end token,
  a malformed `linePattern`, a bad token) instead of silently rendering a blank
  slide; only genuine I/O failures (missing file, unreachable URL) recover to
  `""` with a logged warning.
- `KSlidesConfig.letsPlotJsVersion` default is `4.10.1`, matching the Lets-Plot
  JS runtime the `letsPlot{}` DSL loads.
- Deduplicated the horizontal/vertical slide renderers into shared
  `renderMarkdownSlide` / `renderDslSlide` / `renderHtmlSlide` helpers; the
  `appModule` Ktor lambda was decomposed into `installPlugins` plus per-route
  functions. Output byte-identical.
- Published JARs are now reproducible (stable file order, zeroed timestamps) and
  no longer bundle ~5 MB of unused reveal.js demo media (`assets/video.mp4`,
  `beeping.*`).
- Centralized the Gradle wrapper version and JVM toolchain version in
  `gradle/libs.versions.toml` (new `gradle-wrapper` / `jvm` keys), read by the
  convention plugin and the `Makefile`'s `upgrade-wrapper` target.
- Bumped Kotlin to `2.4.0`, the Gradle wrapper to `9.6.1`, Ktor to `3.5.1`,
  Lets-Plot Kotlin to `4.15.0`, kotlin-css to `2026.7.0`, Detekt to
  `2.0.0-alpha.5`, logback to `1.5.37`, common-utils to `2.9.3`, srcref to
  `2.1.1`, and maven-publish to `0.37.0`.

### Deprecated

- `CodeSnippetConfig.lineOffSet` — use `lineOffset` (`@Deprecated` with
  `ReplaceWith`).
- The `letsPlot(iframeConfig = …)` overload — use the `configBlock` overload.

### Fixed

- The Google Analytics loader hardcoded the maintainer's property id
  (`?id=G-Z6YBNZS12K`) while only `gtag('config', …)` used the configured
  `gaPropertyId`; the loader now uses the consumer's id.
- `hidden` and `uncounted` both wrote `data-visibility`, so `uncounted`
  overwrote `hidden`; `hidden` now wins.
- `include()` compiled begin/end tokens as raw regex, so metacharacter tokens
  (`items[0]`, `foo(x)`) threw or mismatched; tokens are now matched literally.
- Single-level `mkdir()` no-op'd for nested output paths; switched to `mkdirs()`
  with a logged failure.
- `fromTo` threw an opaque `subList` exception when the begin token occurred
  after the end token; it now fails with a clear message naming both tokens.
- `toIntList` threw inconsistent exceptions on malformed ranges; unified on
  `IllegalArgumentException("Invalid line range: …")`.
- `diagram()` / `playground()` silently emitted empty embeds on blank source;
  they now warn and skip.
- `letsPlot{}` validates its `dimensions` (`require(width > 0 && height > 0)`),
  wraps render failures with the filename and resolved JS version, and warns
  when a dev/SNAPSHOT version resolves to a `127.0.0.1` script URL that would
  not load off the build machine.
- `generatePage`'s `<pre>`/`<code>` line merge could strip whitespace from an
  unrelated later `<code>`; the flag is now scoped to the adjacent line.
- Missing-`content{}` validation messages now name the offending slide by id.
- The lazily-created Ktor `HttpClient` was never closed in filesystem mode
  (leaked engine threads); `KSlides.close()` now releases it.
- `Page.kt`'s markdown-section attribute pass iterated `0..nodeList.length`
  (inclusive); converted to `for (i in 0..<nodeList.length)`.

### Build / tooling

- New `Makefile` targets for the docs site dependencies: `check-site`,
  `upgrade-site`, and a renamed `clean-site` that the `site` target depends on;
  all run under `env -u VIRTUAL_ENV`. `default` is now the `help` target; site
  paths factored into `WEBSITE_DIR` / `SITE_DIR` variables.
- Sharpened the stable-versions `dependencyUpdates` filter: a delimiter-anchored
  regex no longer misflags stable classifiers (`-jre`, `.Final`, `-macos`), and
  a pre-release candidate is rejected only when the current version is itself
  stable.
- Consolidated repeated build-script literals (`repoSlug` / `repoUrl` in the
  published-module plugin; an `examples` constant in the root build).

## [1.0.0] — 2026-04-29

First stable release. Ground-up modernization of the build, the
chart-embedding integration, and the test/CI story
([#32](https://github.com/kslides/kslides/pull/32)).

### Added

- `kslides-letsplot` module: new `letsPlot { ... }` DSL that embeds
  [Lets-Plot](https://lets-plot.org/kotlin/) figures via iframe,
  backed by `org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.13.0`.
- `KSlidesConfig.letsPlotJsVersion` (default `"4.9.0"`) controls the
  Lets-Plot JS runtime CDN version without requiring a library
  release.
- `LetsPlotIframeConfig` with the same three-level cascade
  (global → presentation → per-call) that `PlotlyIframeConfig` had.
- GitHub Actions CI workflow (`.github/workflows/ci.yml`) runs
  `./gradlew build` on every pull request and every push to `master`,
  with Gradle build/dependency caching and test-report uploads on
  failure.
- `buildSrc/` precompiled-script convention plugins
  (`kslides.kotlin-module`, `kslides.published-module`) centralize
  Kotlin/Dokka/Kotlinter/publishing config.
- Dokka 2.x HTML API documentation.
- `kslides-core` test coverage: `ConfigsTest`, `OutputConfigTest`,
  `CssValueTest`, `DiagramOutputTypeTest`, `EnumsTest`,
  `UtilFunctionsTest`, `ConfigPropertyTest`, `RecordIframeContentTest`.
- `kslides-letsplot` test suite: `LetsPlotTest` (renderer unit tests)
  and `LetsPlotDslTest` (full DSL → filesystem integration).
- Zensical-based documentation site under `website/kslides/` with
  dark mode as the default and curated navigation. Code examples
  live in `kslides-core/src/test/kotlin/website/` and are pulled
  into the markdown via `pymdownx.snippets` so the docs and the
  compilable sources stay in sync
  ([#33](https://github.com/kslides/kslides/pull/33)).
- `.github/workflows/docs.yml` — GitHub Actions docs workflow that
  generates Dokka HTML, runs the Zensical build, and publishes both
  the site and `api-docs/` to GitHub Pages.
- `Makefile`: `clean-docs`, `kdocs`, `site`, `serve` targets for the
  documentation site.
- `.gitignore` entries for Python and Zensical output.
- Example slides published to GitHub Pages
  ([#35](https://github.com/kslides/kslides/pull/35)).

### Changed

- **BREAKING**: `plotly { ... }` replaced by `letsPlot { ... }`.
  Existing presentations calling `plotly { ... }`, using
  `PlotlyIframeConfig`, `plotlyIframeConfig { }`, or importing
  `com.kslides.config.PlotlyIframeConfig` must migrate to the
  Lets-Plot equivalents. Lets-Plot uses algebraic plot composition
  (`letsPlot(data) + geomPoint { ... }`) rather than plotly-kt's
  lambda-receiver DSL, and has no 3D geoms — presentations using
  `"type": "surface"` / `"scatter3d"` need 2D replacements (e.g.
  `geomTile`, `geomRaster`).
- **BREAKING**: `kslides-plotly` Gradle module renamed to
  `kslides-letsplot`. Update consumer dependencies accordingly.
- **BREAKING**: `DslSlide.plotlyFilename()`,
  `DslSlide.globalPlotlyConfig`, `DslSlide.presentationPlotlyConfig`,
  and `OutputConfig.plotlyPath` renamed to the `letsPlot`-prefixed
  equivalents. `OutputConfig.letsPlotDir` defaults to `letsPlot` and
  the filesystem output directory is now `docs/letsPlot/`.
- **BREAKING**: Maven coordinates change from `com.github.kslides:*`
  (JitPack) to `com.kslides:*` (Maven Central). Publishing migrated
  to the
  [vanniktech maven-publish](https://github.com/vanniktech/gradle-maven-publish-plugin)
  plugin.
- `@KSlidesDslMarker` applied to the DSL receiver *types* rather
  than to function declarations (a no-op per
  [KT-81567](https://youtrack.jetbrains.com/issue/KT-81567)). Scope
  isolation now actually applies inside slide-content and config
  blocks. `VerticalSlidesContext` intentionally left unmarked so
  that `verticalSlides { markdownSlide { ... } }` etc. still resolve.
- `kslidesTest { ... }` changed from `internal` to `public`.
- Test source tree moved under package directories
  (`kslides-core/src/test/kotlin/com/kslides/`).
- `UtilsTest` and `PresentationTest` converted to
  `StringSpec() { init { ... } }` style.
- Build scripts migrated from Groovy DSL to Kotlin DSL
  (`*.gradle.kts`). Gradle wrapper bumped to 9.5.0.
- Kotlin bumped to 2.3.x; Ktor to 3.4.x; Kotest to 6.x.
- `kotlin-css` bumped to `2026.4.14`.
- `kslides-core` promotes the `srcref` dependency from
  `implementation` to `api`.
- README, `llms.txt`, and `CLAUDE.md` rewritten.

### Removed

- **BREAKING**: `kslides-plotly` module, `Plotly.kt`, `PlotlyDsl.kt`,
  `PlotlyIframeConfig`, `plotlyIframeConfig { ... }`,
  `plotlyFilename()`, and the `plotlykt-core` dependency.
- Redundant `@HtmlTagMarker` annotations on top-level helpers in
  `KSlidesDsl.kt`.
- Commented-out `KSlidesConfig.plotlyUrl` field.
- Legacy CI configs (`.travis.yml`, `jitpack.yml`).
- Stale `docs/plotly/` generated output; replaced by `docs/letsPlot/`.
- Duplicate Makefile targets.
- Unused plugin entries from `gradle/libs.versions.toml`
  (`dokka`, `kotlin-jvm`, `maven-publish`, and the `pambrose-*`
  convention plugins) — these are applied via `buildSrc` precompiled
  scripts and don't need version-catalog entries.

### Fixed

- `ConcurrentModificationException` during filesystem rendering that
  surfaced when an internal DSL qualifier resolved to the wrong
  `markdownSlide` overload and appended to the top-level `slides`
  list while it was being iterated.
- `PresentationConfig.assignDefaults` now initializes `topRightTitle`
  (it was overwriting `topLeftTitle`), so reads of `topRightTitle`
  no longer throw.
- `kslidesTest { }` now replays the user-supplied `output { }` block
  before forcing `enableFileSystem` / `enableHttp` to false, so user
  settings such as `outputDir` are preserved in tests.
- `include()` validates `"../"` before `runCatching`, so the
  documented `IllegalArgumentException` actually propagates instead
  of being silently demoted to an empty string.
- `KSlides` iframe / Kroki content maps switched to
  `ConcurrentHashMap` so Ktor handler reads are safe alongside
  DSL-time writes.

## [0.24.0] — 2024-12-11

### Changed

- Converted several `implementation`-scoped dependencies to `api`
  so downstream consumers get transitive access to types they
  already compile against.
- Updated shipped jars (reveal.js, supporting libs).

## [0.23.0] — 2024-12-11

### Changed

- Dependency jar updates.

## [0.22.0] — 2024-06-14

### Changed

- Release cut from a new `0.22.0` branch.
- Jar updates.

## [0.21.0] — 2023-11-01

### Changed

- Version bump; carries forward the accumulated post-0.20.1 work.

## [0.20.1] — 2023-10-30

### Added

- Picked up miscellaneous uncommitted fixes and release version
  alignment.

## [0.19.0] — 2023-05-23

### Changed

- Updated to reveal.js 4.5.0 ([#26](https://github.com/kslides/kslides/pull/26)).

## [0.18.2] — 2023-05-16

### Changed

- Jar updates.

## [0.18.1] — 2023-04-10

### Changed

- Updated the `srcref` jar.

## [0.18.0] — 2023-04-10

### Changed

- Updated to Kotlin 1.8.20.

## [0.17.0] — 2022-12-31

### Changed

- Upgraded to Kotlin 1.8.0 ([#25](https://github.com/kslides/kslides/pull/25)).

### Fixed

- GitHub language-type detection.

## [0.16.0] — 2022-12-03

### Changed

- Refreshed demo slides and embedded reveal.js static content.

## [0.15.3] — 2022-12-03

### Changed

- Release-maintenance bump ([#22](https://github.com/kslides/kslides/pull/22)).

## [0.15.2] — 2022-10-17

### Changed

- Jar updates.

## [0.15.1] — 2022-10-02

### Changed

- Reverted the plotly-kt jar to a previously known-good version.

## [0.15.0] — 2022-10-02

### Changed

- Version bump and demo-content refresh.

## [0.14.1] — 2022-09-19

### Changed

- **BREAKING**: `diagram.content` renamed to `diagram.source`.
- Refreshed demo content.

## [0.14.0] — 2022-09-18

### Changed

- Release cut ([#20](https://github.com/kslides/kslides/pull/20)).

## [0.13.3] — 2022-09-10

### Changed

- Release-maintenance bump ([#19](https://github.com/kslides/kslides/pull/19)).

## [0.13.2] — 2022-08-30

### Added

- `topLeftSvgClass`, `topLeftSvgStyle`, `topRightSvgClass`,
  `topRightSvgStyle` attributes for the corner-link SVGs
  ([#18](https://github.com/kslides/kslides/pull/18)).

## [0.13.1] — 2022-08-30

### Changed

- Release bump ([#17](https://github.com/kslides/kslides/pull/17)).

## [0.13.0] — 2022-08-30

### Changed

- Release cut ([#15](https://github.com/kslides/kslides/pull/15)).

## [0.12.2] — 2022-08-10

### Fixed

- ktlint issue.

## [0.12.1] — 2022-08-10

### Fixed

- Mermaid rendering: stripped leading/trailing newlines, which
  the Mermaid parser was rejecting.

## [0.12.0] — 2022-08-09

### Changed

- Split Plotly integration out into its own `kslides-plotly` library
  so it could be depended on independently
  ([#14](https://github.com/kslides/kslides/pull/14)).

## [0.11.0] — 2022-07-16

### Changed

- Version bump.

## [0.10.7] — 2022-07-16

### Fixed

- Version-metadata fix; tuned JitPack build args
  ([#11](https://github.com/kslides/kslides/pull/11)).

## [0.10.6] — 2022-06-11

### Fixed

- "peer not authenticated" TLS failure
  ([#10](https://github.com/kslides/kslides/pull/10)).

## [0.10.5] — 2022-06-11

### Changed

- Upgraded to Kotlin 1.7.0.

## [0.10.4] — 2022-06-05

### Fixed

- TLS protocol configuration adjustments
  ([#9](https://github.com/kslides/kslides/pull/9)).

## [0.10.3] — 2022-05-25

### Added

- `favicon.ico`.

## [0.10.2] — 2022-05-25

### Added

- GitHub source-link buttons on slide definitions via the `srcref`
  utility.

### Changed

- Upgraded `srcref`.
- README example-link cleanup.

## [0.10.1] — 2022-05-19

### Added

- Optional CSS for Kotlin Playground slides.
- `cssSrc` property on several enums.
- `id` applied to plotly slides in the demo.

### Fixed

- Theme path issue in the demo.

## [0.10.0] — 2022-05-18

### Added

- Plotly integration (`plotly { ... }` DSL)
  ([#8](https://github.com/kslides/kslides/pull/8)).
  _(Removed in 1.0.0 in favor of Lets-Plot.)_

## [0.9.0] — 2022-05-14

### Changed

- Refreshed example slides.

## [0.8.13] — 2022-05-14

### Changed

- README cleanup ([#7](https://github.com/kslides/kslides/pull/7)).

## [0.8.12] — 2022-05-11

### Fixed

- reveal.js content path under HTTP mode.

## [0.8.11] — 2022-05-10

### Changed

- `codeSnippet()` converted to a lambda-with-config shape.
- Cleaned up CSS handling.

### Fixed

- CSS ordering issue.

## [0.8.10] — 2022-05-07

### Added

- `target` attribute for top-corner links.

## [0.8.9] — 2022-05-07

### Changed

- Demo-content updates.

## [0.8.8] — 2022-05-06

### Fixed

- CopyCode plugin buttons not wiring up correctly.

## [0.8.7] — 2022-05-05

### Fixed

- `codeSnippet()` line-pattern handling.

## [0.8.6] — 2022-05-05

### Changed

- Combined `includeFile()` and `includeUrl()` into a single
  `include()` function that dispatches on URL shape.

## [0.8.5] — 2022-05-05

### Fixed

- Kotlin compiler receiver workaround
  ([#6](https://github.com/kslides/kslides/pull/6)).

## [0.8.4] — 2022-05-05

### Fixed

- Kotlin compiler compatibility issue.

## [0.8.3] — 2022-05-04

### Added

- Kotlin Playground slide support
  ([#5](https://github.com/kslides/kslides/pull/5)).

## [0.8.2] — 2022-05-02

### Added

- `codeSnippet()` DSL for syntax-highlighted code blocks.

### Changed

- README image refresh.

## [0.8.1] — 2022-05-02

### Added

- `slideConfig { }` block inside `verticalSlides { }`.

## [0.8.0] — 2022-05-01

### Added

- `topLeft*` and `topRight*` corner-link options.
- Layouts demo presentation.
- `include()` support inside `dslSlide { }` blocks.

### Changed

- Renamed `custom.css` to `slides.css`.
- CSS cleanup across the demo.

## [0.7.8] — 2022-04-29

### Added

- `linkHref()` helper.

## [0.7.7] — 2022-04-26

### Fixed

- Incorrect token-match regression (second pass).

## [0.7.6] — 2022-04-26

### Fixed

- Incorrect token-match in `include(beginToken, endToken)`.

## [0.7.5] — 2022-04-26

### Added

- HTML list helper functions.

### Changed

- Cleaner file sub-range handling in `include()`.

## [0.7.4] — 2022-04-19

### Changed

- Maintenance version bump.

## [0.7.3] — 2022-04-11

### Changed

- Renamed the `@DslMarker` annotation.
- Removed the global `KSlides` value.

## [0.7.2] — 2022-04-07

### Added

- Global CSS value support.

## [0.7.1] — 2022-04-07

### Changed

- Group ID changed to `com.github.kslides` (now `com.kslides` as of
  1.0.0).

### Fixed

- Test break.

## [0.7.0] — 2022-04-05

### Changed

- **BREAKING**: Repository moved to the `kslides` GitHub organization.

## [0.6.8] — 2022-04-05

### Added

- `slideSource()` for horizontal slide context.
- `NavigationMode` support.

### Fixed

- Intro-slide rendering; GitHub-URL generation.

## [0.6.7] — 2022-04-03

### Added

- `notes()` for DslSlides.
- Fragment support (multi-step iterations).
- `trimIndent` option on `includeFile()` / `includeURL()`.
- `lineNumbers()` helper.

### Changed

- Moved `include*` helpers to top level.
- README refresh.

## [0.6.6] — 2022-03-29

### Added

- `PORT` environment-variable support for Heroku.
- `includeUrl()` example.

## [0.6.5] — 2022-03-29

### Added

- netlify.toml and docs for static-content deployment
  ([#4](https://github.com/kslides/kslides/pull/4)).

### Fixed

- Unformatted `index.html` issue.
- Script-loading issue.
- Lint violations.

### Changed

- Converted some `println` calls to structured logging.

## [0.6.4] — 2022-03-21

### Changed

- Config-object structure reworked.

## [0.6.3] — 2022-03-18

### Fixed

- Enum `.toLower()` serialization issue.

## [0.6.2] — 2022-03-18

### Changed

- Cleaner `trimIndent` handling for markdown slides.

## [0.6.1] — 2022-03-18

### Changed

- Moved `title`, `theme`, and `highlight` into the presentation
  config.

## [0.6.0] — 2022-03-18

### Added

- Kotlin Playground slide support (first pass)
  ([#3](https://github.com/kslides/kslides/pull/3)).
- Menu-config options.

### Changed

- Consolidated and capitalized enum definitions.
- `markdownSlide` content now auto-`trimIndent`ed.

## [0.5.2] — 2022-03-13

### Added

- Multi-depth output path support.

## [0.5.1] — 2022-03-13

### Added

- `id` on CSS files.

### Fixed

- `maven-publish` configuration.

## [0.4.0] — 2022-03-13

### Added

- `trimIndentWithInclude()`.
- Copy-button for code snippets.

### Changed

- Reorganized package structure.
- CSS cleanup.

## [0.3.0] — 2022-03-10

### Changed

- **BREAKING**: `htmlSlide` content takes a lambda returning a
  string rather than a plain string.

## [0.2.0] — 2022-03-10

### Changed

- Upgraded to Ktor 2.0.
- Indented presentation-config options.
- Added `ConfigOptions.kt`.

## [0.1.0] — 2021-02-15

### Added

- Initial release. Core DSL: `kslides { presentation { markdownSlide
  { } / htmlSlide { } / dslSlide { } } }` with reveal.js rendering,
  filesystem and Ktor-server output modes, and configurable
  per-slide / per-presentation overrides.

[Unreleased]: https://github.com/kslides/kslides/compare/1.1.1...HEAD
[1.1.1]: https://github.com/kslides/kslides/releases/tag/1.1.1
[1.1.0]: https://github.com/kslides/kslides/releases/tag/1.1.0
[1.0.0]: https://github.com/kslides/kslides/releases/tag/1.0.0
[0.24.0]: https://github.com/kslides/kslides/releases/tag/0.24.0
[0.23.0]: https://github.com/kslides/kslides/releases/tag/0.23.0
[0.22.0]: https://github.com/kslides/kslides/releases/tag/0.22.0
[0.21.0]: https://github.com/kslides/kslides/releases/tag/0.21.0
[0.20.1]: https://github.com/kslides/kslides/releases/tag/0.20.1
[0.19.0]: https://github.com/kslides/kslides/releases/tag/0.19.0
[0.18.2]: https://github.com/kslides/kslides/releases/tag/0.18.2
[0.18.1]: https://github.com/kslides/kslides/releases/tag/0.18.1
[0.18.0]: https://github.com/kslides/kslides/releases/tag/0.18.0
[0.17.0]: https://github.com/kslides/kslides/releases/tag/0.17.0
[0.16.0]: https://github.com/kslides/kslides/releases/tag/0.16.0
[0.15.3]: https://github.com/kslides/kslides/releases/tag/0.15.3
[0.15.2]: https://github.com/kslides/kslides/releases/tag/0.15.2
[0.15.1]: https://github.com/kslides/kslides/releases/tag/0.15.1
[0.15.0]: https://github.com/kslides/kslides/releases/tag/0.15.0
[0.14.1]: https://github.com/kslides/kslides/releases/tag/0.14.1
[0.14.0]: https://github.com/kslides/kslides/releases/tag/0.14.0
[0.13.3]: https://github.com/kslides/kslides/releases/tag/0.13.3
[0.13.2]: https://github.com/kslides/kslides/releases/tag/0.13.2
[0.13.1]: https://github.com/kslides/kslides/releases/tag/0.13.1
[0.13.0]: https://github.com/kslides/kslides/releases/tag/0.13.0
[0.12.2]: https://github.com/kslides/kslides/releases/tag/0.12.2
[0.12.1]: https://github.com/kslides/kslides/releases/tag/0.12.1
[0.12.0]: https://github.com/kslides/kslides/releases/tag/0.12.0
[0.11.0]: https://github.com/kslides/kslides/releases/tag/0.11.0
[0.10.7]: https://github.com/kslides/kslides/releases/tag/0.10.7
[0.10.6]: https://github.com/kslides/kslides/releases/tag/0.10.6
[0.10.5]: https://github.com/kslides/kslides/releases/tag/0.10.5
[0.10.4]: https://github.com/kslides/kslides/releases/tag/0.10.4
[0.10.3]: https://github.com/kslides/kslides/releases/tag/0.10.3
[0.10.2]: https://github.com/kslides/kslides/releases/tag/0.10.2
[0.10.1]: https://github.com/kslides/kslides/releases/tag/0.10.1
[0.10.0]: https://github.com/kslides/kslides/releases/tag/0.10.0
[0.9.0]: https://github.com/kslides/kslides/releases/tag/0.9.0
[0.8.13]: https://github.com/kslides/kslides/releases/tag/0.8.13
[0.8.12]: https://github.com/kslides/kslides/releases/tag/0.8.12
[0.8.11]: https://github.com/kslides/kslides/releases/tag/0.8.11
[0.8.10]: https://github.com/kslides/kslides/releases/tag/0.8.10
[0.8.9]: https://github.com/kslides/kslides/releases/tag/0.8.9
[0.8.8]: https://github.com/kslides/kslides/releases/tag/0.8.8
[0.8.7]: https://github.com/kslides/kslides/releases/tag/0.8.7
[0.8.6]: https://github.com/kslides/kslides/releases/tag/0.8.6
[0.8.5]: https://github.com/kslides/kslides/releases/tag/0.8.5
[0.8.4]: https://github.com/kslides/kslides/releases/tag/0.8.4
[0.8.3]: https://github.com/kslides/kslides/releases/tag/0.8.3
[0.8.2]: https://github.com/kslides/kslides/releases/tag/0.8.2
[0.8.1]: https://github.com/kslides/kslides/releases/tag/0.8.1
[0.8.0]: https://github.com/kslides/kslides/releases/tag/0.8.0
[0.7.8]: https://github.com/kslides/kslides/releases/tag/0.7.8
[0.7.7]: https://github.com/kslides/kslides/releases/tag/0.7.7
[0.7.6]: https://github.com/kslides/kslides/releases/tag/0.7.6
[0.7.5]: https://github.com/kslides/kslides/releases/tag/0.7.5
[0.7.4]: https://github.com/kslides/kslides/releases/tag/0.7.4
[0.7.3]: https://github.com/kslides/kslides/releases/tag/0.7.3
[0.7.2]: https://github.com/kslides/kslides/releases/tag/0.7.2
[0.7.1]: https://github.com/kslides/kslides/releases/tag/0.7.1
[0.7.0]: https://github.com/kslides/kslides/releases/tag/0.7.0
[0.6.8]: https://github.com/kslides/kslides/releases/tag/0.6.8
[0.6.7]: https://github.com/kslides/kslides/releases/tag/0.6.7
[0.6.6]: https://github.com/kslides/kslides/releases/tag/0.6.6
[0.6.5]: https://github.com/kslides/kslides/releases/tag/0.6.5
[0.6.4]: https://github.com/kslides/kslides/releases/tag/0.6.4
[0.6.3]: https://github.com/kslides/kslides/releases/tag/0.6.3
[0.6.2]: https://github.com/kslides/kslides/releases/tag/0.6.2
[0.6.1]: https://github.com/kslides/kslides/releases/tag/0.6.1
[0.6.0]: https://github.com/kslides/kslides/releases/tag/0.6.0
[0.5.2]: https://github.com/kslides/kslides/releases/tag/0.5.2
[0.5.1]: https://github.com/kslides/kslides/releases/tag/0.5.1
[0.4.0]: https://github.com/kslides/kslides/releases/tag/0.4.0
[0.3.0]: https://github.com/kslides/kslides/releases/tag/0.3.0
[0.2.0]: https://github.com/kslides/kslides/releases/tag/0.2.0
[0.1.0]: https://github.com/kslides/kslides/releases/tag/0.1.0
