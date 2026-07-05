---
icon: lucide/sliders
---

# Configuration

kslides has three configuration scopes, each one overriding its parent:

1. **Global** — `kslides { presentationConfig { } }`
2. **Presentation** — `presentation { presentationConfig { } }`
3. **Slide** — `markdownSlide { slideConfig { } }` (and the same on `htmlSlide` / `dslSlide`)

Anything you don't set falls through to the next level up, then to the reveal.js defaults.

## Cascading example

```kotlin
--8<-- "Configuration.kt:cascade"
```

The `markdownSlide` here ends up with `transition = ZOOM` (slide-level wins) on a `#2A9EEE` background, while every other slide in the deck would inherit the presentation's `transition = FADE` and `#1f1f1f` background.

## Transitions

```kotlin
--8<-- "Configuration.kt:transitions"
```

Available transitions: `NONE`, `FADE`, `SLIDE`, `CONVEX`, `CONCAVE`, `ZOOM` (see `com.kslides.Transition`).

## Top-corner navigation links

reveal.js renders small nav links in the top corners. kslides exposes them as `topLeftHref` / `topRightHref`. Set to an empty string to hide them:

```kotlin
--8<-- "Configuration.kt:href"
```

## Copy-code button

reveal.js can render a "Copy" button on every code block via the
[CopyCode plugin](https://github.com/Martinomagnifico/reveal.js-copycode). Turn it on with
`enableCodeCopy`, then tune it through `copyCodeConfig { }`:

```kotlin
--8<-- "Configuration.kt:copycode"
```

`button` (`ALWAYS` / `HOVER` / `FALSE`) and `display` (`TEXT` / `ICONS` / `BOTH`) are typed
enums, so a mistyped value is a compile error. `scale`, `offset`, and `radius` are em values,
so fractional numbers such as `0.8` are fine.

## What can I configure?

The same DSL exposes every reveal.js setting (history, controls, progress bar, autoslide, …) and a number of kslides-managed extras (menu, copy-code button, code highlighting). Browse the source in `com.kslides.config` for the full list — every property is documented in-place.
