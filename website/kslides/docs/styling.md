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
