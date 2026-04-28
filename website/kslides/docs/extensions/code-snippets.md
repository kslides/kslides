---
icon: lucide/code
---

# Code snippets

`codeSnippet { }` renders a syntax-highlighted code block with reveal.js's line-by-line highlighting plugin. It lives inside a `dslSlide`.

## Inline code

```kotlin
--8<-- "CodeSnippets.kt:inline"
```

Notes:

- `language` controls highlight.js classification.
- `highlightPattern` follows reveal.js's pattern syntax — `"1-3"`, `"1|3-5|7"`, `"|1|2"` for stepped highlights, etc.

## Pulling code from a URL

For long examples, keep the source in its own file and pull it in:

```kotlin
--8<-- "CodeSnippets.kt:from-url"
```

This is the same pattern this very documentation site uses — example sources live in `kslides-core/src/test/kotlin/website/`, and the docs reference them via `pymdownx.snippets`.

## When to use which

- **`codeSnippet { }`** — the code is part of the slide content and should be syntax-highlighted with line transitions.
- **`playground { }`** — viewers should be able to edit and run the code (see [Playground](playground.md)).
