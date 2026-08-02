# kslides Feature Ideas

Product proposals for kslides, ranked by expected impact. Each idea has a number
(referenced as F1–F6), a problem statement, a proposed design grounded in the current
architecture, an effort estimate, and open questions.

Proposals are kept as originally written. Shipped features carry a **Status** block
recording what actually landed, where it diverged from the proposal, and how the open
questions were resolved — read the Status block first, since the proposal text below it
describes the plan, not the code.

| #  | Feature                    | Primary user   | Effort | Theme                | Status              |
|----|----------------------------|----------------|--------|----------------------|---------------------|
| F1 | Live-reload dev mode       | Deck authors   | M      | Developer experience | ✅ Shipped in 1.2.0 |
| F2 | One-command PDF export     | Deck sharers   | M      | Distribution         | ✅ Merged (PR #60)  |
| F3 | Type-safe theming DSL      | Teams/branding | L      | Customization        | ✅ Merged (PR #61)  |
| F4 | Native Mermaid diagrams    | Deck authors   | S      | Content              | ✅ Shipped in 1.2.0 |
| F5 | Follow-along presenting    | Presenters     | L      | Platform             | Proposed            |
| F6 | Scaffolding command        | New users      | S      | Adoption             | ✅ Shipped in 1.2.0 |

---

## F1. Live-reload dev mode

### Status: ✅ Shipped in 1.2.0 (PR #56)

`OutputConfig.devMode` (`config/OutputConfig.kt`) gates a `/kslides-reload` websocket
route (`KSlides.kt`) and the injected client script (`LiveReload.kt`, injected from
`Page.generateBody`). The reload signal is the proposed one: a per-JVM boot epoch sent on
connect, so a reconnecting client that sees a new epoch reloads.

Two things landed differently from the proposal below:

- **The rebuild story was wrong.** "kslides itself does not need a file watcher — Gradle
  already provides one" does not hold: `./gradlew -t run` cannot restart a long-running
  *blocking* server task, so continuous build never re-serves the deck. kslides ships
  `kslides-dev.sh` instead — a supervisor that recompiles and restarts the app on source
  changes, taking `--task` / `--watch` / `--port` and defaulting to the root `run` task
  watching `src`. See `website/kslides/docs/output.md`.
- **Position restore does not use the URL hash.** It records slide + fragment in
  `sessionStorage` and replays via `Reveal.getState()` / `setState()`, so it works without
  `hash = true` and does not touch `PresentationConfig`.

Open questions resolved:

- *Implied by `enableHttp`, or always opt-in?* → **Always opt-in.** `KSlides` logs a
  warning when `devMode` is set without `enableHttp`.
- *URL hash vs. server-remembered position?* → **Neither** — client-side `sessionStorage`.
  The server holds no per-session position state.

### Problem

The edit loop for a Kotlin DSL deck is: edit `.kt` file → recompile → rerun `main()` →
switch to the browser → refresh → navigate back to the slide being worked on. For an
authoring tool this is the dominant friction — every visual tweak (font size, layout,
fragment order) pays the full loop. Markdown-first competitors (Slidev, Marp) ship hot
reload out of the box, and it is the first thing an evaluating user notices.

### Proposal

A first-class dev mode that makes the browser follow the code:

```kotlin
kslides {
  output {
    enableHttp = true
    devMode = true   // injects the reload client, enables the watch endpoint
  }
}
```

- **Rebuild**: document `./gradlew -t run` (Gradle continuous build) as the companion
  command; kslides itself does not need a file watcher — Gradle already provides one.
  *(Superseded — see Status above: `-t` cannot restart a blocking server task, so
  `kslides-dev.sh` does the watching.)*
- **Reload**: the existing Ktor server (kslides-core already ships Ktor 3.5.1 with
  websockets available) exposes a `/kslides-reload` websocket. When `devMode = true`,
  `Page.generateHead` injects a small client script that connects to it.
- **State restoration**: on reconnect after a server restart, the client re-navigates to
  the slide/fragment recorded in the URL hash (kslides already supports `hash = true` and
  `fragmentInURL`), so the author lands back on the slide they were editing.
- **Signal**: the simplest robust signal is "connection dropped, then reconnected" — a
  server restart *is* the rebuild notification. No file-watching code needed in kslides.

### User value

Cuts the iteration loop from ~15–30 seconds of manual steps to "save the file, glance at
the browser." This is the single highest-leverage DX investment available.

### Effort: M

Server side is small (one websocket route behind a flag). Client script is ~30 lines.
Most of the work is polish: making sure the injected script never ships in filesystem
output, and documenting the `-t` workflow.

### Open questions

- Should `devMode` be implied by `enableHttp` when running from Gradle, or always opt-in?
- Is slide-position restoration via URL hash sufficient, or should the server remember
  the last-known position per session?

---

## F2. One-command PDF export

### Status: ✅ Merged for the next release (PR #60)

`exportPdf(deck?) { … }` in the new **`kslides-export`** module (`com.kslides.export`,
`PdfExport.kt`) — the separate-module option the effort estimate recommended. It accepts
the same block as `kslides {}`, builds the presentations with the new core `buildKSlides()`
(DSL evaluation with no output), serves them from an ephemeral-port Ktor server via
`KSlides.startHttpServer(port = 0)` / `KSlidesHttpServer`, and prints each deck through
headless Chromium (Playwright). The `pdf {}` block in `output {}` (`PdfConfig`) covers the
proposed knobs — output directory, explicit page size (defaulting to the deck's own print
CSS), per-presentation `exclude()` — plus `browserChannel` (`"chrome"`/`"msedge"` drives an
installed browser and skips the Chromium download) and readiness/settle timeouts. The
first-slide-PNG bonus shipped as `previewPng`. In this repo: `./gradlew exportPdf
[-Pdeck=<name>]` or `make pdf [DECK=<name>]` → `build/pdf/<deck>.pdf`; docs at
`website/kslides/docs/pdf-export.md`.

Two things landed differently from the proposal below:

- **"Wait for reveal's `ready` event" was wrong.** reveal 6's print view assembles
  *asynchronously after* `ready` — printing on `Reveal.isReady()` produced Letter-portrait
  pages because the `@page` size rule didn't exist yet. The exporter instead records
  reveal's **`pdf-ready`** event via an init script installed before any page script runs,
  and also waits for Mermaid `data-processed` completion. Navigation waits for
  `DOMContentLoaded`, not `load` — decks with external iframes (Kotlin Playground) can keep
  the window load event pending indefinitely.
- **It surfaced a pre-existing kslides print bug.** Every generated `<style>` block (the
  inlined `slides.css`, `css {}` rules, code-size/Mermaid rules) was scoped
  `media="screen"`, so all author styling vanished in print — the unstyled corner-link SVG
  rendered full-width in flow and pushed every deck down one page, leaving a blank leading
  PDF page. Fixed in core (`Page.kt`, `CssValue.kt`), which also fixes manual
  `?print-pdf` printing.

Open questions resolved:

- *Separate `kslides-export` module vs. dev-only dependency?* → **Both.** kslides-export is
  a separate published module, and the consumer wiring keeps it dev-only in effect:
  `Export.kt` lives in a dedicated `export` source set in kslides-examples, so Playwright
  never reaches the runnable fat JAR (the deck definition is shared via `exampleSlides()`).
- *Bundle fonts for identical CI rendering?* → **Not needed.** The reveal.js theme fonts
  are checked-in assets served by the same ephemeral server, so PDFs render identically on
  CI. Only render-time external content needs its backing service at export time (Kroki
  decks need a reachable Kroki server; native `mermaid()` renders offline).

### Problem

reveal.js supports print-to-PDF via the `?print-pdf` query parameter, and kslides already
exposes the related tuning knobs (`pdfMaxPagesPerSlide`, `pdfSeparateFragments`,
`pdfPageHeightOffset` in `PresentationConfig`). But actually producing a PDF is a manual
ritual: start the server, open Chrome, append `?print-pdf`, print to file, check margins.
"Send me the deck" is the most common request a presenter gets, and today kslides has no
answer short of a wiki-style instruction list.

### Proposal

A Gradle task (or `KSlides` output option) that drives headless Chromium:

```bash
./gradlew exportPdf                # all presentations
./gradlew exportPdf -Pdeck=slides  # one presentation
```

- **Mechanism**: launch the existing Ktor server on an ephemeral port, drive headless
  Chromium via Playwright-Java (or Chrome DevTools Protocol directly to avoid the
  dependency), load `http://localhost:<port>/<deck>?print-pdf`, wait for reveal's
  `ready` event, call `Page.printToPDF`, write `build/pdf/<deck>.pdf`.
  *(Partly superseded — see Status above: the readiness signal is `pdf-ready`, not
  `ready`, which fires before the print view has assembled.)*
- **Config**: a `pdf {}` block in `OutputConfig` for page size, output directory, and
  per-presentation opt-out.
- **Bonus**: the same headless session can capture a first-slide PNG for use as a social
  preview / Open Graph image — near-zero marginal cost once the browser plumbing exists.

### User value

Turns distribution from a documented workaround into a build artifact. Also enables CI
to attach PDFs to GitHub releases automatically.

### Effort: M

The browser automation is well-trodden; the main decisions are dependency weight
(Playwright downloads a browser — should live behind an optional module or plugin,
e.g. `kslides-export`, following the `kslides-letsplot` precedent) and CI ergonomics.

### Open questions

- Separate `kslides-export` module vs. dev-only dependency in the consumer's build?
- Bundle fonts/assets so the PDF renders identically on CI runners without system fonts?

---

## F3. Type-safe theming DSL

### Status: ✅ Merged for the next release (PR #61)

`customTheme {}` inside `presentationConfig {}` — backed by `ThemeConfig`
(`config/ThemeConfig.kt`) — shipped with the block name and property names the proposal
sketched. Eighteen typed properties (brand colors, `mainFont`/`headingFont`/`codeFont`,
`headingTextTransform`, per-level heading sizes, selection colors, `blockMargin`) each map
1:1 to a reveal.js `--r-*` variable: the `ConfigProperty` key (the Kotlin property name) is
kebab-cased and `--r-`-prefixed at emission time, so only assigned properties are emitted
and the cascade works by plain map merge like every other config. `baseTheme` picks the
stock theme to layer on and takes precedence over `theme` via the new
`PresentationConfig.effectiveTheme`, which also drives theme-derived behavior such as
Mermaid's dark/light selection. `logo()` pins a corner logo/watermark to every slide (and
every exported PDF page). Docs: the "Custom themes" section of
`website/kslides/docs/styling.md`, plus the `theme.html` example deck.

Two things landed differently from the proposal below:

- **No `theme/<name>.css` output file.** The overrides are inlined as a
  `<style id="custom-theme">` block in `Page.generateHead`, layered after the base theme's
  stylesheet link. That matches how kslides already inlines `slides.css` and `css {}`
  rules, keeps filesystem and HTTP output byte-identical, and needs no new route or output
  path — at the cost of not being separately cacheable.
- **Print required a z-index decision the proposal didn't anticipate.** reveal.js' print
  view wraps each slide in a `.pdf-page` stacking context at `z-index: 1` whose opaque
  background painted over the fixed-position logo. The logo sits at `z-index: 5` — above
  the print pages, below reveal's progress bar (10) and controls (11).

Open questions resolved:

- *Raw `--r-*` passthrough?* → **Yes.** `customProperty("--r-heading-letter-spacing",
  "0.02em")` covers the variables the DSL doesn't model; the name is validated to start
  with `--`, and values cascade per property name like the typed ones.
- *`slideConfig`-level overrides in v1?* → **No**, per the proposal's own recommendation.
  Theming is global/presentation-level; per-slide styling stays with `classes`/CSS.

### Problem

Theming today means picking from the stock reveal.js themes via the `PresentationTheme`
enum, or hand-writing CSS overrides in `slides.css` / `css {}` blocks. Individuals accept
stock themes; **teams do not** — a company deck must carry brand colors, fonts, and logo.
Right now that requires reverse-engineering reveal's theme SCSS variables by hand, which
is exactly the kind of stringly-typed escape hatch kslides exists to eliminate.

### Proposal

A `theme {}` DSL that generates a reveal.js-compatible theme stylesheet from typed
properties, using the kotlinx.css dependency kslides already ships:

```kotlin
presentationConfig {
  customTheme {
    baseTheme = PresentationTheme.WHITE       // start from a stock theme
    mainColor = Color("#1a1a2e")
    backgroundColor = Color("#f5f5f5")
    linkColor = Color("#0f4c81")
    headingFont = "Inter, sans-serif"
    codeFont = "JetBrains Mono, monospace"
    headingTextTransform = TextTransform.none  // kill reveal's default UPPERCASE
    logo("assets/logo.svg", position = TopRight, size = 80.px)
  }
}
```

- **Mechanism**: reveal.js themes are ultimately a set of CSS custom properties plus
  rules; the DSL emits an override stylesheet layered after the base theme in
  `Page.generateHead`. No SCSS compilation needed — target the compiled custom
  properties (`--r-main-color`, `--r-heading-font`, etc.) that modern reveal.js exposes.
- **Cascade**: follows the existing config cascade (global → presentation), implemented
  with the same `ConfigProperty` delegate pattern as other config classes.
- **Output**: filesystem mode writes `theme/<name>.css` next to the deck; HTTP mode
  serves it from the same route tree as `slides.css`.
  *(Superseded — see Status above: the overrides are inlined into the page head instead,
  so both output modes emit identical HTML and no new route or output path is needed.)*

### User value

"Make it look like our brand" goes from an afternoon of CSS archaeology to five typed
lines with IDE completion. This is the feature that converts team/corporate adoption.

### Effort: L

The DSL and CSS emission are straightforward; the effort is in coverage decisions (which
of reveal's ~30 theme variables to expose), the logo/watermark placement feature, and
documentation with visual examples.

### Open questions

- Expose raw `--r-*` custom-property passthrough for variables the DSL doesn't model?
- Should `slideConfig`-level overrides (per-slide accent color) be in scope for v1?
  (Recommendation: no — presentation-level only, per-slide via existing `classes`/CSS.)

---

## F4. Native Mermaid diagrams

### Status: ✅ Shipped in 1.2.0 (PR #55)

`DslSlide.mermaid(source)` in `MermaidDsl.kt`, backed by a Mermaid 11.16.0 UMD build
checked in at `docs/revealjs/plugin/mermaid/` and grafted onto the published JAR classpath
by `processResources` — the asset-bundling pattern the proposal called for. The runtime,
its init snippet, and the head CSS are emitted only for presentations containing at least
one mermaid block (a per-render flag on `Presentation`, mirroring `codeStyleClasses`).

Shipped as a function taking the source string, `mermaid("…")`, rather than the
`mermaid { }` block sketched below. Theme selection is driven by
`PresentationTheme.isDark` (`Enums.kt`), which is exhaustive, so adding a reveal.js theme
forces a dark/light decision at compile time instead of silently defaulting.

Open questions resolved:

- *Pin the bundled Mermaid version in `libs.versions.toml`?* → **No.** It is a checked-in
  browser asset, not a Gradle dependency, so the version lives as a documented constant in
  `MermaidDsl.kt` beside the CDN URL it came from.
- *Eager vs. lazy render?* → **Lazy**, as each slide becomes visible. Hidden reveal.js
  sections are `display:none`, which breaks Mermaid's size calculation; print view renders
  the whole deck up front.

`include()` support came for free, since `mermaid()` takes a `String` — documented on the
Mermaid docs page. Kroki `diagram()` is retained for the long tail, as planned.

### Problem

`diagram {}` (DiagramDsl.kt) renders through Kroki, which is a strong multi-format story
but has two costs: it requires a network round-trip to `kroki.io` (or a self-hosted
instance) at render time, and it renders to a static image. Decks fail to render offline
— on a plane, behind a corporate proxy, or when kroki.io hiccups during a live talk.
Meanwhile Mermaid has become the de facto diagram syntax developers already know from
GitHub READMEs.

### Proposal

A `mermaid {}` slide helper that renders client-side, no external service:

```kotlin
dslSlide {
  content {
    mermaid("""
      sequenceDiagram
        Browser->>Ktor: GET /slides
        Ktor->>KSlides: render()
        KSlides-->>Browser: HTML
    """)
  }
}
```

- **Mechanism**: bundle the Mermaid JS distribution alongside the reveal.js assets in
  `docs/revealjs/` (same single-source-of-truth pattern — grafted onto the JAR classpath
  via `processResources`, served by the existing static handler). The helper emits a
  `<pre class="mermaid">` block; a small init script (theme-aware: pick Mermaid's
  dark/light theme from the reveal theme) runs `mermaid.run()` on slide load.
- **Coexistence**: Kroki stays for the long tail (PlantUML, GraphViz, D2, …); docs
  position Mermaid as the zero-dependency default and Kroki as the power option.
- **`include()` support**: like code snippets, diagram source should be loadable from a
  file or URL via the existing `include()` mechanism.

### User value

Offline-safe, third-party-free diagrams in the syntax users already write elsewhere.
Removes a live-demo failure mode (external service down mid-talk).

### Effort: S

Asset bundling follows an established pattern; the DSL helper mirrors existing ones.
Main work is the theme integration and a fragment-interaction test (diagrams inside
`fragment`s re-rendering correctly).

### Open questions

- Pin the bundled Mermaid version in `libs.versions.toml` like `letsPlotJsVersion`?
- Render eagerly on deck load vs. lazily on slide-visible (memory vs. first-show delay)?

---

## F5. Follow-along presenting mode

### Problem

When presenting remotely or to a large room, the audience either watches a screen-share
(low fidelity, no ability to look back) or opens the deck URL themselves (and is
immediately lost, on the wrong slide). reveal.js used to solve this with the multiplex
plugin, which is retired — the niche is currently unfilled across the ecosystem. kslides
is uniquely positioned because it already ships a real server (HTTP mode via Ktor); every
static-site competitor would have to bolt one on.

### Proposal

Presenter-to-audience slide sync over websockets:

- **Presenter** opens `/deck?present=<token>` — their navigation events (slide + fragment
  index) are published to the server.
- **Audience** opens the plain deck URL — a client script subscribes and follows the
  presenter's position. A "break away / rejoin" toggle lets a viewer scroll back to a
  previous slide and then snap back to live.
- **Server**: one websocket route + an in-memory broadcast channel per presentation;
  fits naturally beside the existing session-based iframe caching in HTTP mode.
- **Auth**: presenter token generated at startup and printed to the console — no account
  system, no persistence. Read-only for the audience by construction.

```kotlin
output {
  enableHttp = true
  followAlong = true   // prints the presenter URL + token at startup
}
```

### User value

Turns kslides from "renders reveal.js decks" into a lightweight presentation *platform*:
run the fat JAR on a laptop or a $5 VPS and every attendee is on the right slide. Strong
differentiator; great conference demo (which is itself marketing — kslides talks given
*in* kslides).

### Effort: L

The sync core is modest (Ktor websockets + broadcast channel), but resilience is the real
work: reconnection, late joiners, many concurrent viewers, mobile browsers backgrounding
tabs. Needs load testing before it's honest to advertise.

### Open questions

- Scope v1 to a single presenter per presentation? (Recommendation: yes.)
- Does F1's websocket infrastructure share code with this? (Build F1 first; reuse the
  client-injection and route plumbing.)

---

## F6. Scaffolding command

### Status: ✅ Shipped in 1.2.0 (PR #54)

`kslides-init.sh` — the "shell script wrapping `git clone` + `sed`" that the effort
estimate called a legitimate v1. It clones `kslides-template`, strips the git history,
renames the project name and presentation title, and initializes a fresh repository. It
runs on macOS's default bash 3.2 and modern Linux bash, uses `set -euo pipefail`, validates
the project name, refuses to overwrite an existing directory, cleans up a partially created
directory on failure, guards the renames so a drifted template warns instead of silently
no-op'ing, and sidesteps the BSD/GNU `sed -i` split.

Scoped down from the proposal: **there are no interactive prompts.** Output mode and extras
(playground, mermaid/kroki, lets-plot) are not asked for — whatever `kslides-template`
carries is what you get, including its Pages workflow and version catalog. That keeps the
template as the single source of truth, which was the point, but it means "extras" is still
a manual edit after scaffolding.

Open questions resolved:

- *Where does the generator live?* → **This repo**, invoked by `curl … | bash -s -- my-talk
  --title "My Talk"` or run from a local clone.
- *A `kslides.dev` short domain for the curl entry point?* → **Not pursued.** The one-liner
  points at `raw.githubusercontent.com`.

### Problem

Getting to a first slide today means cloning the `kslides-template` repo or copying from
`kslides-examples`, then hand-editing project names, versions, and deck metadata. The
first five minutes decide whether an evaluating developer stays; every manual rename in
that window is a drop-off risk.

### Proposal

A guided generator producing a ready-to-run deck project:

```bash
# lowest-friction entry points, in preference order:
curl -s https://kslides.dev/init | bash        # or:
gradle init --type kslides                      # or a downloadable CLI:
kslides init my-talk
```

- Prompts for: project name, deck title, output mode (GitHub Pages / server / both),
  extras (playground, mermaid/kroki, lets-plot).
- Emits: Gradle wrapper + version catalog pinned to the current kslides release, a
  minimal `Slides.kt` with one markdown and one DSL slide, the GitHub Pages workflow
  (mirroring the existing `docs.yml` pattern), and a README with the three commands that
  matter (`run`, `build`, deploy).
- Implementation detail: the generator can simply template the existing
  `kslides-template` repo rather than generating files from scratch — the template stays
  the single source of truth, the command removes the manual renaming.

### User value

First slide on screen in under two minutes, correct CI/CD from day one. Adoption
features compound: every retained user is a potential deck-in-the-wild backlink.

### Effort: S

Mostly templating and docs. The main decision is the delivery vehicle (script vs. CLI
binary vs. Gradle init plugin) — a shell script wrapping `git clone` + `sed` is a
legitimate v1.

### Open questions

- Where does the generator live — this repo, or `kslides-template` itself?
- Is a `kslides.dev`-style short domain available/desired for the curl entry point?

---

## Suggested sequencing

### Done — shipped in 1.2.0

1. ~~**F4 + F6 first** (both S): quick wins, visible momentum, no architectural risk.~~
2. ~~**F1 next** (M): highest sustained value for existing users; builds the websocket
   client-injection plumbing.~~

The sequencing held: F4 and F6 landed as small, self-contained additions, and F1 followed
with the websocket route and client-injection plumbing it predicted.

### Done — merged for the next release

3. ~~**F2** (M): unlocks CI-attached PDFs for releases.~~ Merged in PR #60. The
   CI-attach payoff is unlocked but not yet claimed — see the F2 follow-up below.
4. ~~**F3** (L): the team-adoption unlock; benefits from user feedback gathered above.~~
   Merged in PR #61. It landed ahead of the "user feedback gathered above" the sequencing
   assumed, so the coverage decision (which reveal.js variables to model) rests on the
   stock themes' own variable set rather than on reported demand — the
   `customProperty()` passthrough is the release valve if that guessed wrong.

### Remaining

5. **F5 last** (L): reuses F1's infrastructure; ship when the server story is mature.
   **Its prerequisite is now met** — `LiveReload.kt` establishes the pattern F5 needs
   (a `devMode`-style output flag gating a websocket route, plus per-render client-script
   injection from `Page`), so the sync core is mostly a second route and a broadcast
   channel. The resilience work the estimate flags (reconnection, late joiners, mobile
   tab backgrounding, load testing) is unchanged and remains the bulk of the effort.

### Follow-ups from shipped work

- **F1**: `devMode` currently warns when set without `enableHttp`. If F5 lands, both flags
  will gate websocket routes and the two client scripts will coexist on the same page —
  worth a shared injection point rather than a second special case in `Page`.
- **F2**: the motivating "CI attaches PDFs to GitHub releases" use case is not wired up —
  it needs a release-workflow step (pre-install Chromium via the Playwright CLI, run
  `exportPdf`, upload the artifacts). The readiness wait is tied to reveal 6's print-view
  internals (`pdf-ready`, Mermaid `data-processed`), so re-verify it when bumping the
  bundled reveal.js. `kslides-export` is unpublished until the next Maven Central release,
  and `installation.md` already lists it — the release checklist covers the version bump.
- **F3**: the typed properties track reveal.js' `--r-*` variable names by convention (the
  Kotlin property name is kebab-cased and prefixed at emission), so a reveal.js upgrade
  that renames or drops a variable would silently stop applying — worth a check when
  bumping the bundled reveal.js, alongside the F2 print-view check. The proposal's
  "documentation with visual examples" is covered by the `theme.html` example deck rather
  than by screenshots on the docs page. Fonts are named but not loaded: `headingFont`/
  `codeFont` assume the family is already available (system font or a webfont the deck
  pulls in itself); a `webFont()` helper would close that gap.
- **F4**: the bundled Mermaid version is a checked-in asset with no upgrade automation;
  bumping it is a manual re-download. Worth a Makefile target if it drifts.
- **F6**: no prompts means "add lets-plot / playground" is still a manual post-scaffold
  edit. If the template grows variants, revisit the guided-generator design above.
