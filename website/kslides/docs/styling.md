---
icon: lucide/palette
---

# Styling

Each presentation has its own CSS. You can attach styles either as a raw string or via the [kotlinx.css](https://github.com/Kotlin/kotlinx.html/wiki/Getting-started-with-kotlinx.css) DSL — and you can mix the two on the same presentation.

## Raw CSS

```kotlin
--8<-- "Styling.kt:string"
```

Anything that's valid CSS works. The string is appended to the presentation's stylesheet verbatim.

!!! warning "Whitespace matters"

    CSS in kslides content is space-sensitive. If you have a build step that auto-formats generated HTML, exclude the kslides output directory.

## kotlinx.css DSL

```kotlin
--8<-- "Styling.kt:dsl"
```

The DSL is type-safe and refactor-friendly — handy when sharing styles across presentations.

## Targeting specific slides

Set an `id` on the slide and write a rule against it:

```kotlin
markdownSlide {
  id = "title"
  content { "# Hello" }
}

// then
css += """
#title h1 { font-size: 4em; }
"""
```

`htmlSlide` exposes `classes` for the same purpose — see [HTML slides](slides/html.md).

## Font sizes

Slide and code font sizes are plain config values — no CSS required. All three cascade from `kslides { }` → `presentation { }` → slide, same as any other [configuration](configuration.md) property:

```kotlin
presentationConfig {
  slideConfig {
    codeFontSize = "0.60em"   // default for all slides
    codeWrap = true           // wrap long code lines
  }
}

markdownSlide {
  slideConfig {
    fontSize = "34px"         // this slide only
    codeFontSize = "0.40em"
  }
}
```

- `fontSize` — font size for all content on the slide (any CSS length). Themes size headings in `em`, so everything scales together.
- `codeFontSize` — font size for code blocks (reveal.js's default is `0.55em`).
- `codeWrap` — when `true`, long code lines wrap instead of overflowing horizontally. A slide can set `false` to override a presentation-wide `true`.

## Code font size

reveal.js renders code blocks — Markdown fences, [code snippets](extensions/code-snippets.md), and `htmlSlide` `<pre>` blocks — at `0.55em` relative to the slide. Because the slide canvas is a fixed size, long lines overflow with a scrollbar instead of shrinking to fit. For most cases, [`codeFontSize`](#font-sizes) above is simpler; override `.reveal pre` directly when you need a selector the config API doesn't expose.

### Every code block

Attach the rule to the `kslides {}` block (or a single `presentation {}`) so it applies everywhere:

```kotlin
--8<-- "Styling.kt:code-global"
```

Lower the value until the widest line fits; raise it to enlarge all code. To keep the rare over-long line inside the window instead of scrolling it, add `.reveal pre code { white-space: pre-wrap; }`.

### One slide only

Give the slide an `id` and scope the rule to it. An `id` selector (`#bigcode pre`) outranks the global `.reveal pre`, so it wins with no `!important` and independent of order:

```kotlin
--8<-- "Styling.kt:code-slide"
```

### Several slides

An `id` must be unique per page — reveal.js uses it for deep links (`#/bigcode`) and the slide menu, so the same `id` can't be reused on two slides. Use a **class** instead; a class is made to be shared, so one rule styles every slide tagged with it:

```kotlin
--8<-- "Styling.kt:code-shared"
```

`markdownSlide`, `htmlSlide`, and `dslSlide` expose `classes` directly, and `slideDefinition` accepts a `classes` argument for the same purpose.

!!! tip "Qualify with `.reveal` to beat the global rule"

    A bare `.big pre` has the *same* specificity as the global `.reveal pre`, so the tie is broken by whichever is declared last — reorder the blocks and the override silently stops working. Writing `.reveal .big pre` (two classes) always outranks `.reveal pre`, so it wins regardless of order. A single-slide `id` selector (`#bigcode pre`) already outranks both and needs no qualifier.
