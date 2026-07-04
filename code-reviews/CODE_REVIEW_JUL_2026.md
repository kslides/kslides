# kslides Code Review

_Scope: `kslides-core` + `kslides-letsplot` (the published library) and the Gradle/convention-plugin layer. The `kslides-examples` deck was treated as illustrative content, not a quality target. Reviewed at commit `77c0e26` (branch `master`)._

## Methodology

A multi-agent review pass:

1. **Find** — 11 parallel reviewers: 7 by code area (config system, core orchestration, slide types, DSL surface, utils/internal, letsplot, build) + 4 cross-cutting lenses (Kotlin idioms, error handling / silent failures, API ergonomics, test coverage).
2. **Triage** — overlapping findings deduped and clustered into distinct candidates.
3. **Verify** — one adversarial verifier per candidate read the real code to confirm or refute it (filters hallucinated/taste-based findings).
4. **Synthesize** — prioritized report.

**Funnel:** 91 raw findings → 65 candidates → **53 confirmed**, 12 rejected.

> **One verifier was itself wrong.** The render-mutation finding (#7 below) was reported as "delete the dead `slideCount = 0` line." Tracing the HTTP path disproved it: `generatePage` runs per request and `verticalSlides` children are reconstructed at render time, drawing ids from that shared counter — so the reset is load-bearing (it keeps iframe filenames stable). Deleting it would break the HTTP iframe cache. Always re-verify before acting on a review.

**Status legend:** ✅ fixed (with tests) · ⚠️ partially addressed (follow-up noted) · ⬜ open backlog.

**Progress:** ~49 of the 53 confirmed findings are fixed with tests, across four merged PRs. **PR #45** (squash-merged as `765614b`) landed eight batches — (1) the 8 top items, (2) the Kotlin idiom/simplification cleanup, (3) build-hardening + diagnostics, (4) the letsplot/testing/API/docs batch, (5) the low-risk quick-wins, (6) the self-contained refactors, (7) the additive APIs, and (8) the slide-type dedup (verified byte-identical). **PR #46** (`9f0df1c`) added the render thread-safety fix (per-`KSlides` render lock). **PR #47** (`2d6ee86`) narrowed the config-cascade internals out of the public API. **PR #48** (`0f8d3f1`) split `VerticalSlide` into an abstract base + concrete `VerticalSlideStack`. Detekt now fails the build on findings (was non-fatal). No partials remain. The one substantive item still open is the deeper **pure-read render refactor** (would replace #46's lock and churn every iframe filename); the rest are minor doc nits.

---

## Top recommendations

1. ✅ **GA loader hardcodes the maintainer's analytics id** — leaked `G-Z6YBNZS12K` into every generated deck.
2. ✅ **`include()` swallows all failures into blank slides** — authoring errors now fail the build.
3. ✅ **`include()` was untested** — added `IncludeTest`.
4. ✅ **Single-level `mkdir()` breaks nested output paths** — switched to `mkdirs()`.
5. ✅ **`hidden` + `uncounted` collide on `data-visibility`** — `hidden` now wins.
6. ✅ **Lets-Plot JS runtime (4.9.0) stale vs. 4.14.0 catalog** — bumped to `4.10.1` + pin test.
7. ✅ **Render mutates shared state** — made thread-safe via a per-`KSlides` render lock (**PR #46**); the deeper pure-read refactor is an optional follow-up.
8. ✅ **`fromTo` compiles include tokens as raw regex** — tokens now `Regex.escape`'d.

---

## Correctness & robustness

- ✅ **GA loader hardcodes the maintainer's GA property id** — `Page.kt:131`. Loader URL hardcoded `?id=G-Z6YBNZS12K` while only `gtag('config', …)` used the consumer's id (proven in committed `docs/*.html`). _Fix: `src = "…/gtag/js?id=${config.gaPropertyId}"` + render test._
- ✅ **`hidden`/`uncounted` both write `data-visibility`; `uncounted` overwrites** — `Slide.kt:104-108`. _Fix: `if/else`, `hidden` wins + tests._
- ✅ **Stale Lets-Plot JS runtime version** — `KSlidesConfig.kt:49`. Catalog pins `lets-plot 4.14.0` → core `4.10.1`; default was `4.9.0`, KDoc referenced an old mapping. _Fix: bump + KDoc + `CLAUDE.md` + pin test._
- ✅ **`fromTo` compiles begin/end tokens as raw `Regex`** — `InternalUtils.kt:152-153,168-169`. Metacharacter tokens (`foo(`, `items[0]`) throw/mis-match, then get swallowed. _Fix: `Regex.escape(...)` + test._
- ✅ **Render mutates shared `KSlides`/slide state — thread-unsafe under concurrent HTTP** — `Page.kt`, `KSlides.kt`. Rendering resets `slideCount`, reconstructs each `verticalSlides{}` stack (drawing child ids from that shared counter), and bumps per-slide iframe counters; Ktor serves concurrently, so two renders could interleave and corrupt each other's output (or throw `ConcurrentModificationException`). _Fix (**PR #46**, separate from PR #45): added `KSlides.renderLock` and hold it for the whole of `generatePage`. Minimal, **output-preserving** (iframe filenames byte-identical) — it serializes concurrent renders rather than parallelizing them. `PageConcurrencyTest` renders a vertical-stack deck from 64 threads and asserts each equals the serial render (verified it fails without the lock). The deeper pure-read refactor (render-local counters / build-time materialization, which would churn every iframe filename) is documented as follow-up._
- ✅ **`fromTo` can throw on begin-after-end token order** — `InternalUtils.kt`. `subList(beginIndex, endIndex)` lacked a `beginIndex <= endIndex` check → opaque exception. _Fix: `require(beginIndex <= endIndex)` with a diagnostic naming both tokens + indices, before the `subList`. Test asserts the clear message fires (not the JDK one)._
- ✅ **`generatePage` post-processes serialized DOM with fragile regex surgery** — `Page.kt`. The `<pre>/<code>` merge used an unscoped `preFound` flag that could strip whitespace from an unrelated later `<code>`. _Fix: extracted the merge into the testable `Page.mergePreAndCode` and reset `preFound` in the `else` branch so it only bridges a `<pre>` to the `<code>` on the very next line. `PageTest` covers the merge and the regression (a non-adjacent `<code>` keeps its indentation)._

## Error handling & diagnostics

- ✅ **`include()` swallows every read/parse failure into `""`** — `Utils.kt:143-161`. Conflated recoverable I/O with authoring errors (missing token, bad `linePattern`, bad regex token). _Fix: catch only `IOException` for warn-and-`""`; authoring errors propagate; KDoc tightened. **Behavior change — release note.**_
- ✅ **Single-level `mkdir()` no-ops for nested paths** — `KSlides.kt:245,264`; `InternalUtils.kt:231`. _Fix: `mkdirs()` + logged failure + test._
- ✅ **Lazy Ktor `HttpClient` never closed (filesystem-mode leak)** — `KSlides.kt`. Each `kslides{}` touching `diagram{}` leaked engine threads for the JVM's life. _Fix: `KSlides` now implements `AutoCloseable`; `close()` closes the client only if the lazy was initialized; `kslides{}` calls it after output unless HTTP mode keeps it alive. Test: `close()`/`use {}` are safe no-ops when no client was built._
- ✅ **`letsPlot{}` has no error handling / dimension validation** — `LetsPlotDsl.kt`. Raw `0`/negative `Dimensions` silently clip the plot. _Fix: `resolvePlotSize` validates `require(width>0 && height>0)`; `renderLetsPlotContent` wraps the render and rethrows with filename + JS version (re-raising `Error`). Tests: non-positive dims throw, width/height merge appears on the iframe._
- ✅ **`toIntList` throws inconsistent, undocumented exceptions on malformed ranges** — `InternalUtils.kt`. `-5` → `NumberFormatException`; `1-2-3` → message interpolates the split list. _Fix: wrap parsing to unify on `IllegalArgumentException("Invalid line range: '$splitElem'")`; added KDoc `@throws` + tests for `-5`/`a`/`1-b` and the `:`/`;` separators._
- ✅ **`diagram()`/`playground()` silently emit empty embeds on blank source** — `DiagramDsl.kt`; `PlaygroundDsl.kt`. _Fix: blank-source/`srcName` guard warns and returns before any Kroki POST or iframe; `BlankSourceGuardTest` proves render emits no `<img>`/`<iframe>` and makes no network call._
- ✅ **`buildJsonObjectFromMap` shadows `(k,v)` and throws context-free errors** — `DiagramDsl.kt`. _Fix: renamed the inner pair to `(optionKey, optionValue)` and the outer to `(field, value)`; error messages now name the offending field/key + filename._
- ✅ **`ConfigProperty` getter throws for any property not seeded by `assignDefaults` — implicit, untested invariant** — `ConfigProperty.kt`. _Fix: added a `ConfigsTest` reflection check over `PresentationConfig().apply { assignDefaults() }` that reads back every `kslidesManagedValues` key through its delegate (a missing seed would throw), plus throw-on-unset and two-map-split tests._
- ✅ **Required `content{}` validated deep in the renderer with no slide id** — `Presentation.kt`. _Fix: all four `require` messages now name the slide via `(id <private_slideId>)` and the markdown ones mention the filename alternative; exposed `private_slideId` on the `HtmlSlide` interface. Test asserts the dsl/html messages carry the id._
- ✅ **Dev/SNAPSHOT `letsPlotJsVersion` bakes a `127.0.0.1` URL into output** — `LetsPlot.kt`, `LetsPlotDsl.kt`. _Fix: `LetsPlot.isLocalScriptUrl` + `warnIfLocalScriptUrl` log a warning when a static page would bake in a localhost script URL; added a logger to the letsplot module. Test: `isLocalScriptUrl` flags 127.0.0.1/localhost, clears CDN._

## Design & architecture

- ✅ **Slide types duplicate a Horizontal/Vertical class + DSL function per type** — `Presentation.kt`, `MarkdownSlide.kt`. Concrete source of the vertical-markdown separator divergence. _Fix: extracted `DIV.renderMarkdownSlide(slide, config, vertical)` / `renderDslSlide` / `renderHtmlSlide`; the six public function pairs now delegate (receiver scoping kept). Made the `MarkdownSlide` interface "fat" (adds `private_slideId`/`markdownAssigned`/`processSlide`, mirroring `DslSlide`/`HtmlSlide`) so a single interface-typed helper covers both variants. **Verified byte-identical**: a 6-slide deck (md/html/dsl × horizontal/vertical) rendered identically before/after (`diff` clean); `SlideRenderingTest` pins the section markup._
- ✅ **`slideDefinition` (public `Presentation` API) hardcodes the kslides repo** — `Presentation.kt`. `srcref` hardcoded `account/repo/path`, so the "GitHub Source" link was wrong for any other consumer. _Fix (non-breaking): added `githubAccount`/`githubRepo`/`githubPath`/`githubBranch` params to both `slideDefinition` overloads, threaded through `srcref`; `githubPath` defaults to `source` so the link follows the excerpted file. Verified the example deck's `source` already equals the old hardcoded path → byte-identical output._
- ✅ **`VerticalSlide` is `open` and plays both marker-base and concrete-wrapper roles** — `Slide.kt`, `Presentation.kt`. Every vertical child allocated an unused context. _Fix (**PR #48**, breaking): `VerticalSlide` is now `abstract` (marker base for children, no context); a new concrete `VerticalSlideStack : VerticalSlide` holds the `verticalContext` and is what `verticalSlides{}` constructs. Behavior byte-identical (existing vertical-stack tests unchanged); `VerticalSlideSplitTest` pins the new structure._
- ✅ **`AbstractConfig.merge` mutates in place, aliases collection refs, name-collides with `Map.merge`** — `AbstractConfig.kt`. _Fix: documented the in-place mutation, the by-reference (non-deep-copied) collection sharing — safe because config collections are only replaced, never mutated — and the distinction from the `Map.merge` extension. Rename to `mergeFrom` skipped to avoid churning the many call sites._
- ✅ **`DiagramConfig.options` replaces (not merges) across the cascade, undocumented** — `DiagramConfig.kt`. _Fix: documented the replace semantics on `options` (a per-diagram map fully overrides the presentation/global one; set the complete map at the level you want)._
- ✅ **`appModule` is a single ~75-line lambda mixing install + 5 route families** — `KSlides.kt`. _Fix: extracted `Application.installPlugins` + `Route.iframeRoutes`/`krokiRoute`/`staticRoutes`/`presentationRoutes` (and a `REVEAL_ROOT_DIR` const); `appModule` is now a short composition and the `CyclomaticComplexMethod`/`LongMethod` suppressions are gone._

## API & DSL ergonomics

- ✅ **Cascade internals are public, with a documented map-mutation escape hatch** — `ConfigProperty.kt`, `AbstractConfig.kt`, `MenuConfig.kt`. _Fix (**PR #47**, breaking): narrowed both `AbstractConfig` value maps and `ConfigProperty` to `internal`; added a public `revealjsOption(key, value)` helper to replace the escape hatch (raw reveal.js option, cascades via `merge`); rewrote the `MenuConfig.themes` KDoc to point at it. `merge()` stays public (cross-module caller). Test: `revealjsOption` survives `merge`._
- ✅ **`letsPlot{}` takes a pre-built config instead of a config lambda** — `LetsPlotDsl.kt`. Asymmetric with `playground{}`/`diagram{}`. _Fix (additive): new `configBlock: LetsPlotIframeConfig.() -> Unit = {}` overload (both delegate to a shared private `letsPlotImpl`); the prebuilt-`iframeConfig` overload is now `@Deprecated`. Migrated the example deck + tests to `configBlock`._
- ✅ **`CodeSnippetConfig.lineOffSet` mid-word capitalization** — `CodeSnippetConfig.kt`. The lone camelCase outlier. _Fix: added canonical `lineOffset`; `lineOffSet` is now a `@Deprecated(ReplaceWith("lineOffset"))` alias delegating to it; the internal reader in `KSlidesDsl` uses `lineOffset`._
- ✅ **`diagram()` takes `diagramType` as a free String** — `DiagramDsl.kt`. A typo surfaced only as a far-away Kroki 400. _Fix (additive): added a `DiagramType` enum (28 Kroki types, `krokiName = name.lowercase()`) and a `diagram(DiagramType, …)` overload that delegates to the `String` overload (kept for unlisted types). Tests: `krokiName` mapping + the enum overload routes to the same blank-source guard._
- ✅ **`staticRootDir` silently ignored in HTTP mode** — `OutputConfig.kt`. _Fix: KDoc now states it is the filesystem-mode `srcPrefix`; HTTP mode serves from the classpath via `defaultHttpRoot` and ignores it._

## Kotlin idioms & simplifications

All `low`/`small`, idiom/cleanup only — **all fixed** (idiom batch):

- ✅ `require(...) { throw ... }` no-op message lambda — `Presentation.kt:112-114`. _Fix: return the message string; `require` already throws `IllegalArgumentException`._
- ✅ `ConfigProperty.getValue` double map lookup; `configName` needless mutable state with misleading `toString` — `ConfigProperty.kt:22-46`. _Fix: single `?:` lookup (map values are non-null); dropped `configName` + the custom `toString`._
- ✅ `InternalUtils.Map.merge` reimplements stdlib `+` (no production callers) — `InternalUtils.kt:15-20`. _Fix: deleted the extension + its 5 `UtilsTest` "Map merge" cases (the only callers)._
- ✅ Dead `indentFirstLine` (zero callers) — `InternalUtils.kt:48-69`. _Fix: deleted._
- ✅ Dead imports + unused `Utils` logger; redundant default-imported stdlib symbols — `Page.kt:6` (`kotlinx.coroutines.async`), `Utils.kt:11/20` (logger + `KotlinLogging`), `KSlides.kt:35-52` (18 `kotlin.collections.*`/`kotlin.text.*` defaults). _Fix: removed._
- ✅ Deprecated single-String `java.net.URL` constructor — `Utils.kt:145` → `URI(src).toURL()`.
- ✅ `permuteBy`/`toIntList` over-use scope functions — `Utils.kt:58-68` (collapsed to `orders.asSequence().map { … }`), `InternalUtils.kt:108-142` (dropped nested `.also`/`.let`, destructure directly). _Note: the `.also`→`apply` for `Slide.mergedSlideConfig` was **not** applied — an `apply{}` receiver shadows the private `presentation`/`slideConfig` members the merge args reference, so `.also` is the correct form here (commented)._
- ✅ `CssValue` vararg ctor redundant `.toList()` — `CssValue.kt:24`. _Fix: `Array.joinToString` directly._
- ✅ `TargetPlatform`/`PlaygroundMode` undocumented positional `s` ctor param — `Enums.kt:123-166`. _Fix: renamed `s`→`queryString` + `@param` doc._

## Build

- ✅ **Published JAR bundles ~5MB unused reveal.js demo media** — `kslides-core/build.gradle.kts`. _Fix: `exclude("assets/video.mp4", "assets/beeping.wav", "assets/beeping.txt")` in `processResources` (images retained). Verified: runtime jar keeps 195 revealjs entries incl. `image1.png`, drops the 3 media files._
- ✅ **Detekt non-fatal with no committed baseline** — `kslides.kotlin-module.gradle.kts`. _Fix: fixed the stale `Deprecation>excludeImportStatements` config key, fixed the 3 real `MaxLineLength` findings, then flipped the default to `ignoreFailures = false` (opt out with `-Pdetekt.ignoreFailures=true`). No baseline needed — the tree is violation-free. Disabled Detekt's `MaxLineLength` (layout is owned by ktlint, whose own max-line-length rule is intentionally off) to remove the ktlint↔Detekt conflict. CLAUDE.md/Makefile notes updated._
- ✅ **`processResources` revealjs graft has no missing-dir guard** — `kslides-core/build.gradle.kts`. _Fix: `doFirst { require(revealjsDir.isDirectory) { … } }` so a sparse checkout fails loudly instead of publishing a runtime-less JAR._
- ✅ **Published JARs lack reproducible-archive settings** — `kslides.published-module.gradle.kts`. _Fix: `tasks.withType<AbstractArchiveTask> { isReproducibleFileOrder = true; isPreserveFileTimestamps = false }`. Verified: jar entry timestamps are zeroed (`02-01-1980`)._
- ✅ **Dead `overrideVersion` handling in the root build** — `build.gradle.kts`. _Fix: removed; the load-bearing per-module copy lives in `kslides.kotlin-module`. Replaced with a comment noting the root project is not published._

## Testing

- ✅ **`include()` failure/recovery contract untested** — added `IncludeTest` (guard, URL guard-skip, recover-empty, fail-loud, token slice, line pattern).
- ✅ **Idiom-batch regression tests** — added/strengthened alongside the idiom cleanup to prove it is behavior-preserving: `permuteBy` edge cases (empty orders, duplicate/reordered indices) in `UtilFunctionsTest`; `toIntList`'s >2-endpoint throw in `UtilsTest`; `CssValue` vararg-constructor ordering + single-element in `CssValueTest`; and the `verticalSlides{}` empty-block message assertion in `PresentationTest`. The `ConfigProperty` refactor and the enum `queryVal` rename were already fully covered by the existing `ConfigPropertyTest` / `EnumsTest`.
- ✅ **Error-handling regression tests** — `toIntList` malformed-endpoint (`-5`/`a`/`1-b`) and `:`/`;` separator cases in `UtilsTest`; new `BlankSourceGuardTest` covering the `diagram()`/`playground()` blank-source guards (no embed, no network call).
- ✅ **Config cascade / throw-on-unset / two-map split lack `PresentationConfig`-level tests** — `ConfigsTest.kt`. _Added four tests: two-map routing, throw-on-unset, the `kslidesManagedValues`-scoped assignDefaults-completeness reflection check, and a reveal.js-option cascade._
- ✅ **No tests for `autoAnimate` emission or vertical separators** — `PresentationTest.kt`. _Added `data-auto-animate` (dslSlide) and `data-separator-vertical` (markdown slide) render assertions (hidden/uncounted already covered)._
- ✅ **`fixIndents`/`indentInclude` (include indentation pipeline) untested** — `UtilsTest.kt`. _Added cases: token prefix, `trimIndent`, HTML-escape; and `indentInclude` marker re-indentation across lines._
- ✅ **`letsPlot` iframe-config merge test omits width/height override** — `LetsPlotDslTest.kt`. _Added a width/height override test asserting both appear on the iframe._
- ✅ **"Code fence test" asserts stdlib `trimIndent`, exercising no kslides code** — `UtilsTest.kt`. _Repurposed to exercise `trimIndentWithInclude` with a `~~~` fence (indentation preserved inside the fence)._

## Documentation accuracy

- ✅ **`recordIframeContent` dynamic-mode KDoc overstates "regenerated on every request"** — `KSlidesDsl.kt`. _Fix: the `@param staticContent` KDoc now notes the lambda is re-invoked per request but its captured inputs are frozen at build time, so output is identical unless the lambda reads mutable external state (e.g. `include()`)._

---

## Rejected during verification (12)

Most rested on inaccurate premises — behaviorally-identical "fixes," misread control flow, or changes that would break compilation, the config cascade, or reveal.js's documented attribute fallback. A few were correct observations whose proposed remedy was a no-op or a regression. The render-mutation "delete the dead line" suggestion (#7) was confirmed *wrong* by tracing and re-scoped accordingly.

## Status

**PR #45** (`improve/review-quick-wins`) squash-merged to `master` as `765614b`, bundling the eight batches below (each landed with tests; full `clean build` — tests + Kotlinter + fatal Detekt — green). The individual per-batch commit hashes were collapsed by the squash.

- **The 8 top items.**
- **The Kotlin idiom & simplification batch** — commit `c636f9a`. One sub-item (the `.also`→`apply` swap on `Slide.mergedSlideConfig`) was deliberately *not* applied: an `apply{}` receiver shadows the private `presentation`/`slideConfig` members the merge args reference, so `.also` is the correct form (now commented inline). All other idiom items are done.
- **The build hardening + diagnostics batch** — commit `21e3334`. All five build items (media exclusion, fatal Detekt, missing-dir guard, reproducible archives, dead `overrideVersion`) plus three diagnostics items (`toIntList` unified exceptions, `diagram()`/`playground()` blank-source guards, `buildJsonObjectFromMap` shadowing + error context).
- **The letsplot / testing / API / docs batch.** `letsPlot{}` dimension validation + render error-wrapping + localhost script-URL warning, the `lineOffset` rename, `staticRootDir` + `recordIframeContent` KDoc clarifications, and the `ConfigsTest`/`PresentationTest`/`UtilsTest`/`LetsPlotDslTest` coverage gaps (config cascade, `autoAnimate`/separators, `fixIndents`/`indentInclude`, letsplot width/height merge, `~~~` fence).
- **The low-risk quick-wins batch** — commit `b9502eb`. `fromTo` begin-after-end `require` guard, slide-id (and filename hint) in the four `content{}` validation messages, configurable `githubAccount`/`githubRepo`/`githubPath`/`githubBranch` on `slideDefinition` (path defaults to `source`), and documented `DiagramConfig.options` replace semantics.
- **The self-contained refactors batch** — commit `880b062`. `generatePage`'s pre/code merge extracted to `Page.mergePreAndCode` with the unscoped-`preFound` fix + `PageTest`; `KSlides` made `AutoCloseable` with a closable lazy client; `AbstractConfig.merge` semantics documented; `appModule` decomposed into `installPlugins` + per-route-family functions.
- **The additive-APIs batch** — commit `a96cb3f`. `letsPlot{}` gains a `configBlock` lambda overload (prebuilt-`iframeConfig` deprecated); `diagram()` gains a `DiagramType` enum overload alongside the kept `String` overload. Example deck + tests migrated to `configBlock`.
- **The slide-type dedup batch.** Extracted `renderMarkdownSlide`/`renderDslSlide`/`renderHtmlSlide`; made the `MarkdownSlide` interface consistent with `DslSlide`/`HtmlSlide`. Verified byte-identical via a before/after render diff; `SlideRenderingTest` guards the section markup.

Then, one per separate PR (each breaking, so isolated for maintainer sign-off):

- **Render thread-safety** — **PR #46** (`9f0df1c`). `KSlides.renderLock` serializes concurrent renders; output-preserving; `PageConcurrencyTest` proves the race is closed.
- **Config-map visibility narrowing** — **PR #47** (`2d6ee86`). The two `AbstractConfig` value maps + `ConfigProperty` are now `internal`; public `revealjsOption(key, value)` replaces the escape hatch.
- **VerticalSlide split** — **PR #48** (`0f8d3f1`). `VerticalSlide` → abstract base; concrete `VerticalSlideStack` wraps the stack and owns the context; children no longer allocate an unused one. Byte-identical.

The one substantive item still open is the deeper **pure-read render refactor** (render-local counters / build-time materialization of vertical stacks) — it would replace #46's lock and allow parallel renders, at the cost of churning every iframe filename, so it's not byte-identical. A separate in-flight dependency/wrapper upgrade (Kotlin 2.4.0-RC2 → 2.4.0, Gradle 9.5.1 → 9.6.1, Ktor/Detekt/letsplot bumps) is intentionally kept out of these review commits.
