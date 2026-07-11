# kslides Feature Ideas

Product proposals for kslides, ranked by expected impact. Each idea has a number
(referenced as F1–F6), a problem statement, a proposed design grounded in the current
architecture, an effort estimate, and open questions.

| #  | Feature                    | Primary user   | Effort | Theme               |
|----|----------------------------|----------------|--------|---------------------|
| F1 | Live-reload dev mode       | Deck authors   | M      | Developer experience |
| F2 | One-command PDF export     | Deck sharers   | M      | Distribution        |
| F3 | Type-safe theming DSL      | Teams/branding | L      | Customization       |
| F4 | Native Mermaid diagrams    | Deck authors   | S      | Content             |
| F5 | Follow-along presenting    | Presenters     | L      | Platform            |
| F6 | Scaffolding command        | New users      | S      | Adoption            |

---

## F1. Live-reload dev mode

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

1. **F4 + F6 first** (both S): quick wins, visible momentum, no architectural risk.
2. **F1 next** (M): highest sustained value for existing users; builds the websocket
   client-injection plumbing.
3. **F2** (M): unlocks CI-attached PDFs for releases.
4. **F3** (L): the team-adoption unlock; benefits from user feedback gathered above.
5. **F5 last** (L): reuses F1's infrastructure; ship when the server story is mature.
