---
icon: lucide/play
---

# Playground

`playground { }` embeds a [Kotlin Playground](https://play.kotlinlang.org/) iframe — the audience can edit and run the snippet without leaving the slide.

## Basic usage

```kotlin
--8<-- "Playground.kt:basic"
```

## Configured

```kotlin
--8<-- "Playground.kt:configured"
```

Common options:

- `theme` — `PlaygroundTheme.DARCULA`, `PlaygroundTheme.IDEA`, etc.
- `dataHighlightOnly` — render as a static highlight, with no run button.
- `lines` — show line numbers.
- `from` / `to` — show only this line range of the source file.
- `fontSize` — size of the code in the editor and output pane (see below).
- `width` / `height` — iframe dimensions, e.g. `"350px"`.

## Font size

`fontSize` sizes the code in the editor and the run-output pane. Like every other config value it
cascades: set it once globally, per presentation, or per `playground()` call.

```kotlin
--8<-- "Playground.kt:fontsize"
```

Two things worth knowing:

- **Prefer absolute units.** The Playground renders in its own iframe document, so `em` resolves
  against that document's root font size, not the surrounding slide's.
- **Line spacing follows on its own.** CodeMirror's `line-height` is a unitless ratio, so it scales
  with whatever `fontSize` you set. Set `lineHeight` — a unitless ratio or a length — only to
  tighten or loosen the spacing itself.

For anything else inside the iframe, `css { }` injects rules into its `<head>`, after the generated
`fontSize` rules, so a hand-written rule of equal specificity wins:

```kotlin
playground("src/main/kotlin/playground/HelloWorld.kt") {
  css {
    rule(".CodeMirror-gutters") { backgroundColor = Color("#073642") }
  }
}
```

## Output behavior

- **Static site mode** — kslides writes each playground iframe to `docs/playground/<slug>.html`.
- **HTTP mode** — the Ktor server serves the iframe at a session-scoped URL, regenerating it on demand.

## Tip: point at a real source file

`playground()` takes a path (or URL) rather than inline code, so keep the snippet in its own `.kt`
file under your source tree:

```kotlin
playground("src/main/kotlin/playground/HelloWorld.kt")
```

This way the snippet stays compilable and refactorable as your real code evolves. Additional files
listed after the first are attached as hidden dependencies — supporting classes, JUnit helpers:

```kotlin
playground("src/main/kotlin/playground/HelloPets.kt", "src/main/kotlin/playground/Cat.kt")
```
