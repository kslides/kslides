# Release Notes

This document collects narrative release notes for each published
kslides version. See [CHANGELOG.md](CHANGELOG.md) for the
structured, category-grouped log.

---

## 1.1.0 — 2026-07-03

A quality release from a full multi-agent code review. It fixes a batch of
correctness and error-handling issues, makes page rendering thread-safe, adds
type-safe DSL options, and tightens the public API — with two intentional
breaking changes. It also folds in the build-infrastructure work that had been
queued for an (unreleased) 1.0.1.

### Breaking changes

- **Config-cascade internals are now `internal`.** `AbstractConfig`'s two value
  maps (`revealjsManagedValues`, `kslidesManagedValues`) and the `ConfigProperty`
  delegate are no longer part of the public API. Configure options through the
  typed properties as before; for a reveal.js option kslides doesn't model, use
  the new `revealjsOption(key, value)` helper instead of poking the map. The
  documented `MenuConfig.themes` escape hatch now points at `revealjsOption`.
- **`VerticalSlide` is now abstract.** It was an `open` class doing double duty —
  the concrete wrapper for a whole vertical stack *and* the base class for the
  individual vertical child slides, so every child allocated a
  `VerticalSlidesContext` it never used. The wrapper is now a separate concrete
  `VerticalSlideStack`; children extend the abstract `VerticalSlide`. Rendered
  HTML is byte-identical. Both types are constructed internally, so the practical
  break is limited to code that instantiated `VerticalSlide` directly.

### Correctness & error handling

- **Google Analytics id leak.** The GA loader `<script src>` hardcoded the
  maintainer's property id (`G-Z6YBNZS12K`) while only the `gtag('config', …)`
  call used your configured `gaPropertyId`. Every generated deck now loads GA
  with your id.
- **`include()` no longer swallows authoring errors.** A missing begin/end token,
  a malformed `linePattern`, or a bad token used to collapse to an empty string
  and render a blank slide. Those now fail the build; only genuine I/O failures
  (missing file, unreachable URL, 404) recover to `""` with a logged warning.
  `include()` tokens are also matched literally now, so metacharacters like
  `items[0]` work instead of being compiled as a regex. **Behavior change** — a
  deck that was silently hiding a broken `include()` will now fail loudly.
- `hidden` + `uncounted` on the same slide both wrote `data-visibility`, so
  `hidden` now wins. Nested output directories are created with `mkdirs()`.
  `fromTo`, `toIntList`, `diagram()` / `playground()` (blank source), and
  `letsPlot{}` (non-positive dimensions, dev/localhost script URLs) now report
  clear errors or warnings instead of throwing opaque exceptions or emitting
  empty embeds.

### Thread-safe rendering

Page rendering mutates shared per-`KSlides` state (a slide-id counter,
reconstructed vertical stacks, per-slide iframe counters). A Ktor server renders
pages concurrently, so those mutations could interleave and corrupt output.
`generatePage` now holds a per-`KSlides` render lock for the duration of a
render; output is unchanged and iframe filenames are stable. `KSlides` also
implements `AutoCloseable` now, releasing the lazily-created Ktor `HttpClient`
(a filesystem-mode thread leak) — usable as `kslides { … }.use { }`.

### New DSL options

- `diagram(DiagramType.PLANTUML) { … }` — a type-safe enum overload of
  `diagram()`; a mistyped type is a compile error rather than a far-away Kroki
  400. The raw-`String` overload remains for types not in the enum.
- `letsPlot(configBlock = { … }) { … }` — a config-lambda overload symmetric with
  `playground{}` / `diagram{}`; the old `iframeConfig = …` overload is deprecated.
- `revealjsOption(key, value)` on any config — set a raw reveal.js option.
- `slideDefinition(…)` takes optional `githubAccount` / `githubRepo` /
  `githubPath` / `githubBranch`, so the "GitHub Source" link works for your own
  repository (previously hardcoded to `kslides/kslides`).
- `CodeSnippetConfig.lineOffset` replaces the misspelled `lineOffSet` (kept as a
  deprecated alias).

### Detekt

[Detekt](https://detekt.dev) `2.0.0-alpha.5` is now wired into the
`kslides.kotlin-module` convention plugin, so every Kotlin module
exposes `detekt`, `detektMain`, `detektTest`, and the
`detektBaseline*` tasks. Note that Detekt 2.x relocates to the
`dev.detekt` Maven group and uses the matching `dev.detekt` plugin
id (1.x lived under `io.gitlab.arturbosch.detekt`).

Detekt now **fails the build on findings by default** (the config is
valid and the tree is violation-free, so no baseline is needed). Pass
`-Pdetekt.ignoreFailures=true` to downgrade to report-only while
iterating. Line-length is left to Kotlinter/ktlint (Detekt's
`MaxLineLength` is disabled) to avoid the two formatters conflicting.
A `make detekt` shortcut wraps `./gradlew detekt`.

### Build hygiene

- The Gradle wrapper distribution version and the JVM toolchain
  version are now declared in `gradle/libs.versions.toml` as the
  `gradle-wrapper` and `jvm` version keys, alongside the rest of the
  version catalog. The `kslides.kotlin-module` convention plugin
  reads `jvm` via `VersionCatalogsExtension` for `jvmToolchain(...)`,
  and the `Makefile`'s `upgrade-wrapper` target reads `gradle-wrapper`
  from the catalog (with a missing-value guard mirroring the existing
  `VERSION` extraction). To roll the project forward to a new Gradle
  version, bump `gradle-wrapper` in `libs.versions.toml` and run
  `make upgrade-wrapper`.
- Repeated string literals in the build scripts were collapsed:
  `kslides.published-module` extracts `repoSlug` / `repoUrl` so the
  GitHub URL appears once across `pom.url`, `scm.url`,
  `scm.connection`, and `scm.developerConnection`; the root `stage`
  task uses a local `examples` constant for the `:kslides-examples`
  module path.

### Dependency bumps

The version catalog rolled forward to:

- Gradle wrapper `9.6.1`
- Kotlin `2.4.0`
- Ktor `3.5.1`
- Lets-Plot Kotlin `4.15.0`
- kotlin-css `2026.7.0`
- Detekt `2.0.0-alpha.5`
- logback `1.5.37`
- kotlin-logging `8.0.4`
- common-utils `2.9.3`, srcref `2.1.1`, maven-publish `0.37.0`

The Kotlin 2.4.0 bump prompted a sweep of `kslides-core` to
replace wildcard imports (`io.ktor.*`, `kotlinx.html.*`, stdlib
collection/text packages) with explicit per-symbol imports. IDEA's
project code style was updated to keep `io.ktor` out of the
star-import package list. No public DSL or runtime API changed.

### Bug fix

`Page.kt`'s markdown-section attribute escaping pass iterated the DOM
`NodeList` as `(0..nodeList.length).forEach`, which is **inclusive**
of `length` — one past the valid index range. A `node.isNotNull()`
guard was silently swallowing the final out-of-range iteration. The
loop is now `for (i in 0..<nodeList.length)` and the redundant null
guard is gone. The behavior change is invisible to users; it
eliminates a per-page no-op DOM lookup and also clears Detekt's
`ForEachOnRange` performance finding.

### Detekt hygiene

A handful of file- and declaration-level `@Suppress` annotations
were added where Detekt's default thresholds don't fit the shape of
the DSL surface (overload-heavy aggregator files such as
`KSlidesDsl.kt`, the `*Dsl.kt` helpers, and the config classes that
own many small properties). `CssValue.cssError`'s diagnostic
message was hoisted to a `const val` so the expression body fits
on one line — resolving a long-standing conflict between Detekt's
`MaxLineLength` (120 chars) and ktlint's `function-signature` rule.

### Makefile polish

New shortcuts for the docs site's `uv` lockfile:

- `make check-site` — `uv lock --upgrade --dry-run`
- `make upgrade-site` — `uv lock --upgrade`
- `make clean-site` — clears generated docs and the Zensical site
  cache (the old `clean-docs` target was renamed; `make site` now
  depends on it)

Each `uv` invocation runs under `env -u VIRTUAL_ENV` so a stale
workspace virtualenv doesn't shadow `website/.venv`. The default
target is now `make help` (printing a list of every documented
target); the previous `versioncheck` target was renamed to
`versions`. The `WEBSITE_DIR` / `SITE_DIR` variables factor the
docs paths so they appear once at the top of the file.

---

## 1.0.0 — 2026-04-29

First stable release. The project graduates from `0.x` after a
ground-up modernization of the build, the chart-embedding
integration, and the test/CI story. See PR
[#32](https://github.com/kslides/kslides/pull/32).

### Highlights

#### Lets-Plot replaces plotly-kt

The chart-embedding integration has been rewritten around
[Lets-Plot](https://lets-plot.org/kotlin/). The old `plotly { ... }`
DSL is gone; use `letsPlot { ... }` instead:

```kotlin
dslSlide {
  content {
    letsPlot(
      dimensions = 800 by 400,
      iframeConfig = LetsPlotIframeConfig {
        style = "width: 85%; border: 2px solid #586E75;"
        height = "415px"
      },
    ) {
      val data = mapOf("x" to (0..100).toList(), "y" to (0..100).map { it * it })
      letsPlot(data) +
        geomPoint { x = "x"; y = "y" } +
        labs(title = "Squared")
    }
  }
}
```

Key differences from plotly-kt:

- **Plot composition** is algebraic (`letsPlot(data) + geomPoint +
  labs`) rather than a lambda-receiver DSL. Each slide returns a
  `Figure`.
- **No 3D geoms** in Lets-Plot v4. Examples that used
  `"type": "surface"` / `"scatter3d"` were rewritten as 2D heatmaps
  (`geomTile`) and color-encoded scatters.
- **JS runtime** is loaded from the Lets-Plot CDN. The version is
  controlled by `KSlidesConfig.letsPlotJsVersion` (defaults to
  `4.9.0`, matching `lets-plot-kotlin-jvm:4.13.0`).

#### Stricter DSL scope

`@KSlidesDslMarker` now actually enforces DSL scope isolation. Prior
Kotlin versions silently ignored the marker when applied to
functions; Kotlin 2.3 (see
[KT-81567](https://youtrack.jetbrains.com/issue/KT-81567)) both
warns and makes function-level markers a no-op. The marker now
lives on the receiver *types* — `KSlides`, `Presentation`, `Slide`
(and its concrete subclasses), and every DSL-facing config class.

Practical impact: inside slide-content and config blocks, the outer
Presentation/KSlides receivers are no longer silently accessible.
If you have code reaching out from `dslSlide { content { ... } }`
to call presentation-level methods implicitly, you'll need either
to qualify with `this@Presentation.` / `this@kslides.` or
restructure. `verticalSlides { ... }` is intentionally left with
its surrounding `Presentation` receiver accessible, so common
patterns like `verticalSlides { markdownSlide { ... } }` continue
to work unchanged.

#### Build, tooling, CI

- Build scripts fully migrated from Groovy DSL to Kotlin DSL
  (`*.gradle.kts`).
- Shared build logic centralized in `buildSrc/` precompiled-script
  convention plugins (`kslides.kotlin-module`,
  `kslides.published-module`).
- Gradle wrapper bumped to 9.5.0.
- Kotlin 2.3.20+, Ktor 3.4.x, Kotest 6.x.
- `kotlin-css` bumped to `2026.4.14`.
- Dependency versions centralized in `gradle/libs.versions.toml`.
- Removed duplicate Makefile targets and unused plugin entries from
  `gradle/libs.versions.toml` (dokka, kotlin-jvm, maven-publish, and
  the `pambrose-*` convention plugins are now applied via `buildSrc`
  precompiled scripts and don't need version-catalog entries).
- New GitHub Actions CI (`.github/workflows/ci.yml`) runs
  `./gradlew build` on every PR and every push to `master`; failing
  runs upload a `test-reports` artifact.
- Publishing migrated to the
  [vanniktech maven-publish](https://github.com/vanniktech/gradle-maven-publish-plugin)
  plugin. Group ID is now `com.kslides` (Maven Central), replacing
  the old JitPack `com.github.kslides`.
- Dokka 2.x configured for HTML API docs.

#### Documentation

- New [Zensical](https://zensical.org/) documentation site under
  `website/kslides/`, with dark mode as the default and a curated
  navigation (Getting started, Slides, Configuration, Output, Styling,
  Include, Extensions, Markdown reference, KDocs). Code examples live
  in `kslides-core/src/test/kotlin/website/` and are pulled into the
  markdown via `pymdownx.snippets`, so the docs and the compilable
  sources stay in sync
  ([#33](https://github.com/kslides/kslides/pull/33)).
- New `.github/workflows/docs.yml` builds the Dokka HTML output and
  the Zensical site, then publishes both (site + `api-docs/`) to
  GitHub Pages.
- Example slides published to GitHub Pages
  ([#35](https://github.com/kslides/kslides/pull/35)).
- `kslides-core` promotes `srcref` from `implementation` to `api`;
  `Makefile` gains `clean-docs` / `site` targets; `.gitignore`
  covers Python + Zensical output.

#### Test coverage

- New suites in `kslides-core`: `ConfigsTest`, `OutputConfigTest`,
  `CssValueTest`, `DiagramOutputTypeTest`, `EnumsTest`,
  `UtilFunctionsTest`, `ConfigPropertyTest`,
  `RecordIframeContentTest`.
- New suites in `kslides-letsplot`: `LetsPlotTest` (renderer unit
  tests against `PlotHtmlExport`) and `LetsPlotDslTest` (full DSL →
  filesystem integration, writing to a temp `outputDir`).
- Existing tests converted to the `StringSpec() + init { ... }`
  style.

### Bug fixes

- `PresentationConfig.assignDefaults` now initializes
  `topRightTitle` (it was overwriting `topLeftTitle`), so reads of
  `topRightTitle` no longer throw.
- `kslidesTest { }` replays the user-supplied `output { }` block
  before forcing `enableFileSystem` / `enableHttp` to false, so
  settings like `outputDir` are preserved in tests.
- `include()` validates `"../"` before `runCatching`, so the
  documented `IllegalArgumentException` propagates instead of being
  silently demoted to an empty string.
- `KSlides` iframe / Kroki content maps switched to
  `ConcurrentHashMap` so Ktor handler reads are safe alongside
  DSL-time writes.
- Fixed a `ConcurrentModificationException` during filesystem
  rendering that surfaced when an internal DSL qualifier resolved
  to the wrong `markdownSlide` overload and appended to the
  top-level `slides` list while it was being iterated.

### Breaking changes

- `plotly { ... }` → `letsPlot { ... }` (different plot API shape).
- `PlotlyIframeConfig` → `LetsPlotIframeConfig`;
  `plotlyIframeConfig { }` → `letsPlotIframeConfig { }`.
- Gradle module `kslides-plotly` → `kslides-letsplot`.
- `OutputConfig.plotlyPath` → `OutputConfig.letsPlotPath`;
  `docs/plotly/` → `docs/letsPlot/`.
- `DslSlide.plotlyFilename()`, `globalPlotlyConfig`,
  `presentationPlotlyConfig` → `letsPlot`-prefixed equivalents.
- Maven coordinates change from `com.github.kslides:*` (JitPack) to
  `com.kslides:*` (Maven Central).
- Newly-enforced DSL scope may require `this@Presentation.`
  qualifiers in downstream code — see "Stricter DSL scope" above.

### Migration

```kotlin
// Before (plotly-kt)
plotly(dimensions = 800 by 400) {
  layout { title = "Demo" }
  scatter {
    x.numbers = 0..100
    y.numbers = x.numbers.map { it * 2.0 }
  }
}

// After (Lets-Plot)
letsPlot(dimensions = 800 by 400) {
  val data = mapOf("x" to (0..100).toList(), "y" to (0..100).map { it * 2.0 })
  letsPlot(data) +
    geomPoint { x = "x"; y = "y" } +
    ggtitle("Demo")
}
```

Gradle:

```kotlin
// Before
implementation("com.kslides:kslides-plotly:0.24.0")

// After
implementation("com.kslides:kslides-letsplot:1.0.0")
```

Full diff:
[`0.24.0...1.0.0`](https://github.com/kslides/kslides/compare/0.24.0...1.0.0).

---

## 0.24.0 — 2024-12-11

Dependency-maintenance release. Several internal dependencies
that consumers were already compiling against were re-exposed from
`implementation` to `api`, so downstream users no longer need to
re-declare them. Shipped jars refreshed.

## 0.23.0 — 2024-12-11

Jar-refresh release.

## 0.22.0 — 2024-06-14

Branch-rebase release cut from a new `0.22.0` branch, carrying the
accumulated jar updates
([#30](https://github.com/kslides/kslides/pull/30)).

## 0.21.0 — 2023-11-01

Version bump covering incremental upstream updates
([#29](https://github.com/kslides/kslides/pull/29)).

## 0.20.1 — 2023-10-30

Folded in several uncommitted fixes and re-aligned the published
version.

## 0.19.0 — 2023-05-23

Updated to reveal.js 4.5.0
([#26](https://github.com/kslides/kslides/pull/26)).

## 0.18.2 — 2023-05-16

Jar refresh.

## 0.18.1 — 2023-04-10

Updated the `srcref` utility jar.

## 0.18.0 — 2023-04-10

Bumped to Kotlin 1.8.20.

## 0.17.0 — 2022-12-31

Upgraded to Kotlin 1.8.0
([#25](https://github.com/kslides/kslides/pull/25)) and fixed a
GitHub language-detection issue.

## 0.16.0 — 2022-12-03

Refreshed the embedded reveal.js static content and the demo slides.

## 0.15.3 — 2022-12-03

Release-maintenance bump
([#22](https://github.com/kslides/kslides/pull/22)).

## 0.15.2 — 2022-10-17

Jar refresh.

## 0.15.1 — 2022-10-02

Reverted the plotly-kt jar to a previous known-good version after a
regression.

## 0.15.0 — 2022-10-02

Version bump and demo content refresh.

## 0.14.1 — 2022-09-19

**Breaking rename**: `diagram.content` → `diagram.source`. Plus demo
cleanup.

## 0.14.0 — 2022-09-18

Release cut ([#20](https://github.com/kslides/kslides/pull/20)).

## 0.13.3 — 2022-09-10

Release-maintenance bump
([#19](https://github.com/kslides/kslides/pull/19)).

## 0.13.2 — 2022-08-30

Added `topLeftSvgClass`, `topLeftSvgStyle`, `topRightSvgClass`, and
`topRightSvgStyle` attributes for the corner-link SVGs
([#18](https://github.com/kslides/kslides/pull/18)).

## 0.13.1 — 2022-08-30

Release bump ([#17](https://github.com/kslides/kslides/pull/17)).

## 0.13.0 — 2022-08-30

Release cut ([#15](https://github.com/kslides/kslides/pull/15)).

## 0.12.2 — 2022-08-10

Fixed a ktlint issue.

## 0.12.1 — 2022-08-10

Fixed Mermaid rendering by stripping leading/trailing newlines that
the Mermaid parser was rejecting.

## 0.12.0 — 2022-08-09

Split the Plotly integration out of `kslides-core` into its own
`kslides-plotly` library so users who don't want the plotly-kt
dependency can opt out
([#14](https://github.com/kslides/kslides/pull/14)).

## 0.11.0 — 2022-07-16

Version bump.

## 0.10.7 — 2022-07-16

Version-metadata fix; tuned JitPack build arguments
([#11](https://github.com/kslides/kslides/pull/11)).

## 0.10.6 — 2022-06-11

Fixed a "peer not authenticated" TLS failure
([#10](https://github.com/kslides/kslides/pull/10)).

## 0.10.5 — 2022-06-11

Upgraded to Kotlin 1.7.0.

## 0.10.4 — 2022-06-05

TLS protocol configuration adjustments
([#9](https://github.com/kslides/kslides/pull/9)).

## 0.10.3 — 2022-05-25

Added `favicon.ico`.

## 0.10.2 — 2022-05-25

Added GitHub source-link buttons on slide definitions (via the
`srcref` utility), upgraded `srcref`, cleaned up the README's
example links.

## 0.10.1 — 2022-05-19

Added optional CSS for Kotlin Playground slides, the `cssSrc`
property on several enums, and an `id` on plotly slides in the
demo. Fixed a theme-path issue.

## 0.10.0 — 2022-05-18

First Plotly integration — `plotly { ... }` DSL for embedding
plotly-kt charts
([#8](https://github.com/kslides/kslides/pull/8)). _(Deprecated
and removed in 1.0.0 in favor of Lets-Plot.)_

## 0.9.0 — 2022-05-14

Refreshed example slides.

## 0.8.13 — 2022-05-14

README cleanup
([#7](https://github.com/kslides/kslides/pull/7)).

## 0.8.12 — 2022-05-11

Fixed the reveal.js content path under HTTP mode.

## 0.8.11 — 2022-05-10

Converted `codeSnippet()` to a lambda-with-config shape and cleaned
up CSS handling.

## 0.8.10 — 2022-05-07

Added a `target` attribute for top-corner links.

## 0.8.9 — 2022-05-07

Demo content updates.

## 0.8.8 — 2022-05-06

Fixed the CopyCode plugin buttons not wiring up correctly.

## 0.8.7 — 2022-05-05

Fixed `codeSnippet()` line-pattern handling.

## 0.8.6 — 2022-05-05

Combined `includeFile()` and `includeUrl()` into a single
`include()` function that dispatches on URL shape.

## 0.8.5 — 2022-05-05

Kotlin compiler receiver workaround
([#6](https://github.com/kslides/kslides/pull/6)).

## 0.8.4 — 2022-05-05

Fixed a Kotlin compiler compatibility issue.

## 0.8.3 — 2022-05-04

Added Kotlin Playground slide support
([#5](https://github.com/kslides/kslides/pull/5)).

## 0.8.2 — 2022-05-02

Added the `codeSnippet()` DSL for syntax-highlighted code blocks;
refreshed the README image.

## 0.8.1 — 2022-05-02

Added a `slideConfig { }` block inside `verticalSlides { }`.

## 0.8.0 — 2022-05-01

Added `topLeft*` / `topRight*` corner-link options, a layouts demo
presentation, and `include()` support inside `dslSlide { }` blocks.
Renamed `custom.css` to `slides.css` and cleaned up the demo CSS.

## 0.7.8 — 2022-04-29

Added a `linkHref()` helper.

## 0.7.7 — 2022-04-26

Second-pass fix for an incorrect token match in `include()`.

## 0.7.6 — 2022-04-26

Fixed an incorrect token-match issue in
`include(beginToken, endToken)`.

## 0.7.5 — 2022-04-26

Added HTML list helper functions; cleaner file sub-range handling
in `include()`.

## 0.7.4 — 2022-04-19

Maintenance version bump.

## 0.7.3 — 2022-04-11

Renamed the `@DslMarker` annotation and removed the global
`KSlides` value.

## 0.7.2 — 2022-04-07

Added support for a global CSS value.

## 0.7.1 — 2022-04-07

Changed group ID to `com.github.kslides` (now `com.kslides` as of
1.0.0). Fixed a test break.

## 0.7.0 — 2022-04-05

**Breaking**: repository moved to the `kslides` GitHub organization.

## 0.6.8 — 2022-04-05

Added `slideSource()` for horizontal context, `NavigationMode`
support, and fixed intro-slide + GitHub-URL rendering.

## 0.6.7 — 2022-04-03

Added `notes()` for DslSlides, fragment support (multi-step
iterations), a `trimIndent` option on the include helpers, and a
`lineNumbers()` helper. Moved `include*` helpers to top level.

## 0.6.6 — 2022-03-29

Added `PORT` environment-variable support for Heroku, plus an
`includeUrl()` example.

## 0.6.5 — 2022-03-29

Added `netlify.toml` and docs for static-content deployment
([#4](https://github.com/kslides/kslides/pull/4)). Fixed unformatted
`index.html`, script-loading, and lint issues; converted some
`println` calls to structured logging.

## 0.6.4 — 2022-03-21

Reworked the config-object structure.

## 0.6.3 — 2022-03-18

Fixed an enum `.toLower()` serialization issue.

## 0.6.2 — 2022-03-18

Cleaner `trimIndent` handling for markdown slides.

## 0.6.1 — 2022-03-18

Moved `title`, `theme`, and `highlight` into the presentation
config.

## 0.6.0 — 2022-03-18

Added Kotlin Playground slide support (first pass)
([#3](https://github.com/kslides/kslides/pull/3)) and menu-config
options. Consolidated and capitalized enum definitions; made
`markdownSlide` content auto-`trimIndent`ed.

## 0.5.2 — 2022-03-13

Added multi-depth output path support.

## 0.5.1 — 2022-03-13

Added `id` on CSS files; fixed the `maven-publish` configuration.

## 0.4.0 — 2022-03-13

Added `trimIndentWithInclude()` and a copy button for code
snippets. Reorganized the package structure; CSS cleanup.

## 0.3.0 — 2022-03-10

**Breaking**: `htmlSlide` content takes a lambda returning a string
rather than a plain string.

## 0.2.0 — 2022-03-10

Upgraded to Ktor 2.0; indented presentation-config options; added
`ConfigOptions.kt`.

## 0.1.0 — 2021-02-15

Initial release. Core DSL
(`kslides { presentation { markdownSlide / htmlSlide / dslSlide } }`)
with reveal.js rendering, filesystem and Ktor-server output modes,
and configurable per-slide / per-presentation overrides.
