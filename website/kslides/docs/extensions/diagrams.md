---
icon: lucide/git-branch
---

# Diagrams

`diagram { }` embeds diagrams rendered by [Kroki](https://kroki.io). It supports every Kroki backend — PlantUML, Mermaid, Graphviz, BlockDiag, Vega, and many more.

## PlantUML

```kotlin
--8<-- "Diagram.kt:plantuml"
```

## Mermaid

```kotlin
--8<-- "Diagram.kt:mermaid"
```

## Output type

Set `outputType` to `DiagramOutputType.SVG` (recommended), `PNG`, or `PDF`. SVG scales cleanly with reveal.js zoom transitions.

## Output behavior

- **Static site mode** — diagrams are pre-rendered and written to `docs/kroki/`.
- **HTTP mode** — the Ktor server caches each rendered diagram in-memory.

## Other backends

Anything Kroki understands works: `c4plantuml`, `dot`, `ditaa`, `erd`, `nomnoml`, `nwdiag`, `seqdiag`, `wavedrom`, … see the [Kroki docs](https://kroki.io/#support) for the full list. Set `type` to the backend name.
