---
icon: lucide/rocket
---

# kslides

**kslides** is a Kotlin DSL for the [reveal.js](https://revealjs.com) presentation framework.
Author your slides in Markdown, raw HTML, or the type-safe [kotlinx.html](https://github.com/Kotlin/kotlinx.html) DSL,
and ship them as a static site or live Ktor server.

## Why kslides?

- **One source, two output modes** — render to static HTML for Netlify or GitHub Pages, or serve dynamic content over HTTP with Ktor.
- **Mix authoring styles** — Markdown for prose, raw HTML for fine control, the Kotlin DSL for anything generated.
- **Hierarchical configuration** — set defaults globally, override per presentation, and tweak per slide.
- **Live reload while authoring** — with [dev mode](output.md#dev-mode-live-reload), the browser follows your edits and lands you back on the slide you were working on.
- **Type-safe theming** — [`customTheme { }`](styling.md#custom-themes) puts your brand colors, fonts, and logo on the deck without hand-writing CSS.
- **PDF in one command** — [`exportPdf()`](pdf-export.md) prints every deck through headless Chromium, so "send me the slides" is a build artifact.
- **Bring the audience with you** — [follow-along presenting](presenting.md) keeps every viewer on your slide over a websocket, with a break-away/rejoin toggle.
- **Batteries included** — embedded reveal.js, offline [Mermaid diagrams](extensions/mermaid.md), Kotlin Playground iframes, Kroki diagrams, and Lets-Plot charts.

## A taste

```kotlin
--8<-- "HelloWorld.kt:hello"
```

That's a complete kslides program. Run `main()`, open `docs/index.html`, and you've got a slide deck.

To skip the setup entirely, scaffold a ready-to-run project:

```bash
curl -fsSL https://raw.githubusercontent.com/kslides/kslides/master/kslides-init.sh | bash -s -- my-talk --title "My Talk"
```

## Where to next?

- [Installation](installation.md) — scaffold a project, or add kslides to an existing Gradle build.
- [Quickstart](quickstart.md) — build your first deck end-to-end.
- [Slides](slides/index.md) — Markdown, HTML, and DSL slide types.
- [Configuration](configuration.md) — the global → presentation → slide cascade.
- [Output modes](output.md) — static site, Ktor server, and live-reload dev mode.
- [PDF export](pdf-export.md) — one command from deck to PDF.
- [Presenting](presenting.md) — keep a remote audience on your slide.
- [Styling](styling.md) — custom themes, CSS, and slide/code font sizes.
- [KDocs](kdocs.md) — Dokka-generated API reference.
