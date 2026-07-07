---
icon: lucide/workflow
---

# Mermaid

`mermaid( )` embeds a [Mermaid](https://mermaid.js.org) diagram that is rendered client-side by the Mermaid runtime bundled with kslides. No network access or external service is required, so decks keep working offline — on a plane, behind a proxy, or when a diagram service hiccups mid-talk.

## Usage

```kotlin
--8<-- "Mermaid.kt:basic"
```

The helper emits a `<pre class="mermaid">` element containing the diagram source. The Mermaid runtime (bundled with the reveal.js assets, currently Mermaid 11.16.0) and a small init snippet are added automatically — but only to presentations that contain at least one `mermaid( )` block, so other decks pay nothing.

## Loading source from a file or URL

Because `include()` returns a `String`, diagram source can live in a separate file (or at a URL):

```kotlin
--8<-- "Mermaid.kt:include"
```

## Rendering behavior

- Diagrams render lazily as their slide becomes visible (hidden reveal.js slides are `display:none`, which breaks Mermaid's size calculations), and all at once in print/PDF view.
- Mermaid's `dark` theme is selected automatically when the presentation's reveal.js theme is dark (`BLACK`, `MOON`, `DRACULA`, …); light themes get Mermaid's `default` theme.
- HTML-sensitive characters in diagram source (`<`, `&`, quotes) are escaped in the generated HTML and decoded by Mermaid before parsing, so labels like `A["x < y"]` just work.

## Mermaid vs. Kroki

`mermaid( )` is the zero-dependency default for Mermaid syntax. For other diagram languages — PlantUML, Graphviz, D2, BlockDiag, and the rest of the Kroki catalog — or for pre-rendered static images, use [`diagram { }`](diagrams.md).
