# Slide Font-Size API — Design

**Date:** 2026-07-06
**Status:** Approved

## Problem

Setting slide and code-block font sizes currently requires users to write raw CSS at the
`kslides{}` or `presentation{}` level, e.g.:

```kotlin
css += """
  .reveal pre { font-size: 0.60em; }
  .reveal pre code { white-space: pre-wrap; word-break: break-word; }
"""
css += """
  .reveal .smallcode pre { font-size: 0.40em; }
"""
```

Per-slide overrides additionally require a hand-managed CSS class (`smallcode`) and a
two-class specificity trick so the override outranks the global rule. This should be a
first-class, typed API with the same cascade semantics as every other slide option.

## API Surface

Three new cascading properties on `SlideConfig`, stored in `kslidesManagedValues` (they
drive HTML/CSS generation, not `Reveal.initialize()`):

```kotlin
/** Font size for all slide content, any CSS length (e.g. "34px", "0.9em"). Blank inherits the theme default. */
var fontSize by ConfigProperty<String>(kslidesManagedValues)

/** Font size for code blocks (`pre`) on the slide (e.g. "0.60em"). Blank inherits reveal's default (0.55em). */
var codeFontSize by ConfigProperty<String>(kslidesManagedValues)

/** When true, long code lines wrap (pre-wrap + break-word) instead of overflowing horizontally. */
var codeWrap by ConfigProperty<Boolean>(kslidesManagedValues)
```

Defaults in `assignDefaults()`: `""`, `""`, `false`.

The existing global → presentation → slide merge (`Slide.mergedSlideConfig`) provides
defaults-for-all-slides and per-slide overrides with no new mechanism:

```kotlin
presentationConfig {
  slideConfig {
    codeFontSize = "0.60em"   // default for all slides
    codeWrap = true
  }
}

markdownSlide {
  slideConfig {
    fontSize = "34px"         // this slide only
    codeFontSize = "0.40em"
  }
}
```

A slide can explicitly set `codeWrap = false` to override a presentation-level `true`.

## Rendering

### `fontSize` — inline style on the section

Applied in `Slide.processSlide(section)`: prepended to the section's inline style as
`font-size: <value>; <user style>`. User-supplied `style` comes last so it wins on
conflict. Reveal themes size headings/text in `em`, so all slide content scales.

### `codeFontSize` / `codeWrap` — generated class + head CSS rule

Inline styles cannot reach `<pre>` tags: reveal renders markdown client-side, so kslides
never sees them. Instead:

- Each distinct (codeFontSize, codeWrap) combination used in a presentation gets one
  auto-generated class (`kslides-code-1`, `kslides-code-2`, … in first-appearance order).
- The class is appended to the slide's `<section>` class list at render time.
- One rule set per distinct combination is emitted as a `<style>` element in the page head:

```css
.reveal .kslides-code-1 pre { font-size: 0.60em; }
.reveal .kslides-code-1 pre code { white-space: pre-wrap; word-break: break-word; }
```

Deduping by value means a presentation-wide default yields a single shared rule, not one
per slide. Because per-slide values produce their own class, override order is resolved by
the config cascade at build time — no CSS specificity tricks.

Timing: per-slide `slideConfig{}` blocks only execute during body rendering (slide content
lambdas run at render time, and vertical-stack children are reconstructed per render), so
the head cannot know the values up front. Instead, `Slide.processSlide` registers each
(codeFontSize, codeWrap) combination in a per-render registry on `Presentation` (cleared
at the top of `Page.generatePage`, which is already serialized on `renderLock`), and after
the HTML document is built, `generatePage` inserts the `<style>` element into the DOM
head — the same post-processing pattern it already uses for `data-separator` fixes. The
registry is cleared per render, so class assignment order is deterministic across renders.

### Scope notes

- Playground iframes keep their own `playgroundConfig.css` sizing — out of scope.
- Multi-slide markdown (`---` separators): the generated child sections inherit the outer
  section's inline font-size and remain descendants for the `pre` rules, so both
  properties apply to every generated slide.

## `slideDefinition()` Integration

Both variants (`Presentation.slideDefinition` and
`VerticalSlidesContext.slideDefinition`) gain an optional trailing parameter:

```kotlin
configBlock: SlideConfig.() -> Unit = {}
```

applied to the generated markdownSlide's `slideConfig`. The examples'
`smallSlideDefinition` wrappers become:

```kotlin
slideDefinition(source = source, token = token, configBlock = { codeFontSize = "0.40em" })
```

## Example Migration (kslides-examples/Slides.kt)

- Delete the two raw CSS blocks for code sizing/wrapping (currently lines 113–126) and
  replace with `slideConfig { codeFontSize = "0.60em"; codeWrap = true }` inside the
  global `presentationConfig {}`.
- Rework `smallSlideDefinition` to pass `configBlock = { codeFontSize = "0.40em" }`
  instead of `classes = "smallcode"`.
- Regenerate the example HTML in `/docs`.

## Testing

Kotest 6 `StringSpec()` + `init {}` tests using `kslidesTest{}`:

- Cascade resolution: global-only, presentation override, slide override for all three
  properties.
- `fontSize` inline-style merge: with and without a user-supplied `style`; user style wins
  on conflict.
- Rendered page contains the generated class on the section and the matching rule in the
  head.
- Dedup: two slides with the same codeFontSize share one class/rule; different values get
  distinct classes.
- `codeWrap = false` at slide level overrides presentation-level `true` (no wrap rule for
  that slide's class).
- Unset values emit nothing (no class, no rule, no inline font-size).

## Docs / Housekeeping

- CHANGELOG entry under unreleased.
- Docs-site mention (website/kslides) of the new properties.
- KDoc on the three properties and the new `configBlock` parameter.
