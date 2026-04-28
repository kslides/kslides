---
icon: simple/kotlin
---

# DSL slides

`dslSlide { }` exposes the [kotlinx.html](https://github.com/Kotlin/kotlinx.html) builder. The generated HTML is type-safe, refactorable, and can include arbitrary Kotlin logic.

## Basic usage

```kotlin
--8<-- "DslSlides.kt:basic"
```

Every kotlinx.html tag function is available inside `content { }`: `h1`, `p`, `ul`, `img`, `section`, etc.

## Generated content

Because the DSL is just Kotlin, you can compose slides from data:

```kotlin
--8<-- "DslSlides.kt:loop"
```

## When to reach for the DSL

- The slide is generated from data (a list, an API response, a file scan).
- You want compile-time checks on tag and attribute names.
- You're embedding kslides extensions like [`playground { }`](../extensions/playground.md), [`diagram { }`](../extensions/diagrams.md), or [`codeSnippet { }`](../extensions/code-snippets.md) — those live inside `dslSlide { content { ... } }`.
