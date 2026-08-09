---
icon: lucide/palette
---

# Styling

Each presentation has its own CSS. You can attach styles either as a raw string or via the [kotlinx.css](https://github.com/Kotlin/kotlinx.html/wiki/Getting-started-with-kotlinx.css) DSL — and you can mix the two on the same presentation. For brand-level styling, start with the typed `customTheme {}` block below before reaching for raw CSS.

## Custom themes

"Make it look like our brand" doesn't require reverse-engineering reveal.js theme variables. The `customTheme {}` block (in `presentationConfig {}`, globally or per presentation, cascading like every other config) layers typed overrides on top of a stock theme:

```kotlin
--8<-- "Styling.kt:custom-theme"
```

Each property maps to one of the CSS custom properties (`--r-*`) that reveal.js themes expose, and only the properties you assign are emitted — as a `<style id="custom-theme">` block after the base theme's stylesheet, so your values win the cascade while `css {}` rules and `slides.css` can still override them. The overrides also apply in `?print-pdf` view and [PDF export](pdf-export.md).

A few details worth knowing:

- `baseTheme` picks the stock theme to start from and takes precedence over `theme` — it also drives theme-derived behavior like [Mermaid's](extensions/mermaid.md) dark/light selection.
- `logo()` pins a brand image to a corner of every slide (and every exported PDF page). Without an `href` it ignores pointer events, so it never blocks slide interaction. A relative `src` resolves against the output root, so the same path works from a deck at any depth; absolute, external, and `data:` values are used as written.
- `headingTextTransform` controls the stock themes' forced UPPERCASE headings. Eleven of the fourteen
  bundled themes set `--r-heading-text-transform: uppercase` (all but `dracula`, `night`, and
  `serif`), so headings render shouting no matter how you typed them — `TextTransform.none` gets back
  exactly what you wrote.
- `customProperty("--r-…", …)` passes through any reveal.js theme variable the DSL doesn't model. Values are emitted verbatim, so they are checked for characters that would end the declaration (`;`, `{`, `}`, `<`) and rejected at the assignment site rather than corrupting the stylesheet.

The [theme example deck](https://kslides.github.io/kslides/docs/theme.html) shows the result, with its own `customTheme {}` source on the second slide.

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

## Your own stylesheet file

For CSS you'd rather keep in a file than inline, add it to `cssFiles`. Say where it lives with
`origin` — the default looks inside the bundled reveal.js asset directory, which is rarely what you
want for your own file:

```kotlin
kslidesConfig {
  cssFiles += CssFile("css/site.css", origin = AssetOrigin.OUTPUT_ROOT)
  jsFiles += JsFile("js/site.js", origin = AssetOrigin.OUTPUT_ROOT)
}
```

`OUTPUT_ROOT` publishes the file alongside your decks and resolves it from whatever depth a deck
sits at — see [asset paths](output.md#asset-paths). A site-root-absolute `/css/site.css` also works
but breaks if you publish under a path prefix, which a GitHub Pages project site does.

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

These values are interpolated into generated CSS, so they are checked where you assign them: a malformed length like `"0.6em;}"` throws `IllegalArgumentException` naming the property rather than silently breaking every rule after it. Units, a bare `0`, `calc()`/`var()`/`clamp()` expressions, and a blank "unset" are all accepted.

The generated `codeFontSize`/`codeWrap` rules (`fontSize` renders as an inline style, not a head rule) are emitted into the document head after any presentation `css` additions, so on an equal-specificity tie the config-driven value wins over legacy hand-written rules.

### `fontSize` and `codeFontSize` compound

Code blocks are content, so `fontSize` scales them too — and it does so *before* `codeFontSize` applies. `fontSize` renders as an inline `font-size` on the slide's `<section>`; `<pre>` sits inside that section, so an `em` code size resolves against the scaled section rather than the theme base, and the two multiply. On a 42px theme:

| Config | `<section>` | rendered `<pre>` |
|--------|-------------|------------------|
| neither set | 42px | 0.55 × 42 = **23.1px** |
| `fontSize = "0.65em"` | 27.3px | 0.55 × 27.3 = **15.0px** |
| `fontSize = "0.65em"` + `codeFontSize = "0.60em"` | 27.3px | 0.60 × 27.3 = **16.4px** |

Setting `codeFontSize = "0.55em"` to "restore the default" is therefore a no-op — it is already the default, in the same relative units. To size code independently of `fontSize`, give it an absolute unit; px is immune to the section's `em` and still scales with the deck, since reveal.js zooms the whole canvas with a CSS transform:

```kotlin
slideConfig {
  fontSize = "0.65em"
  codeFontSize = "23px"   // reveal.js's default rendered size, unaffected by fontSize
}
```

Or divide the factor out and stay in `em`: under `fontSize = "0.65em"`, `codeFontSize = "0.85em"` lands back at roughly the `0.55em` default.

## Code font size

reveal.js renders code blocks — Markdown fences, [code snippets](extensions/code-snippets.md), and `htmlSlide` `<pre>` blocks — at `0.55em` relative to the slide's `<section>`, which [`fontSize`](#fontsize-and-codefontsize-compound) scales. Because the slide canvas is a fixed size, long lines overflow with a scrollbar instead of shrinking to fit. For most cases, [`codeFontSize`](#font-sizes) above is simpler; override `.reveal pre` directly when you need a selector the config API doesn't expose.

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
