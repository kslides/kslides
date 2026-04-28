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

- `theme` — `"default"`, `"darcula"`, etc.
- `highlightOnly` — render as a static highlight, with no run button.
- `lines` — visible line range, e.g. `"1-5"`.

## Output behavior

- **Static site mode** — kslides writes each playground iframe to `docs/playground/<slug>.html`.
- **HTTP mode** — the Ktor server serves the iframe at a session-scoped URL, regenerating it on demand.

## Tip: prefer `include()`

For longer programs, keep the code in its own `.kt` file and `include()` it:

```kotlin
playground {
  code = include("src/main/kotlin/playground/HelloWorld.kt")
}
```

This way the snippet stays compilable and refactorable as your real code evolves.
