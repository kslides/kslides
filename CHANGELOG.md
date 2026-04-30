# Changelog

All notable changes to kslides are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Entries for releases prior to 1.0.0 are reconstructed from the git history.

## [Unreleased]

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

[Unreleased]: https://github.com/kslides/kslides/compare/1.0.0...HEAD
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
