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
- **Batteries included** — embedded reveal.js, Kotlin Playground iframes, Kroki diagrams, and Lets-Plot charts.

## A taste

```kotlin
--8<-- "HelloWorld.kt:hello"
```

That's a complete kslides program. Run `main()`, open `docs/index.html`, and you've got a slide deck.

## Where to next?

- [Installation](installation.md) — add kslides to a Gradle project.
- [Quickstart](quickstart.md) — build your first deck end-to-end.
- [Slides](slides/index.md) — Markdown, HTML, and DSL slide types.
- [Configuration](configuration.md) — the global → presentation → slide cascade.
- [Output modes](output.md) — static site vs Ktor server.
- [KDocs](kdocs.md) — Dokka-generated API reference.
