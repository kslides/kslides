---
icon: lucide/zap
---

# Quickstart

Build your first deck and serve it locally.

## 1. Minimal program

A complete kslides program is just a `main()` calling the `kslides {}` DSL:

```kotlin
--8<-- "HelloWorld.kt:hello"
```

The default output is a static site under `docs/` plus a Ktor server on port 8080.

## 2. Choose where it goes

You can switch off either output, or change the port:

=== "Static site only"

    ```kotlin
    --8<-- "Output.kt:filesystem"
    ```

=== "HTTP only"

    ```kotlin
    --8<-- "Output.kt:http"
    ```

## 3. Add more presentations

Each `presentation { }` block becomes its own deck at the given `path`:

```kotlin
--8<-- "Output.kt:multi"
```

After running, you'll have `docs/index.html` and `docs/talks/2026.html`.

## 4. Mix slide types

You're not limited to Markdown — see the [Slides overview](slides/index.md) for the full picture.

```kotlin
--8<-- "DslSlides.kt:basic"
```

## Next steps

- [Configure transitions, navigation links, and theme](configuration.md).
- [Embed runnable Kotlin code](extensions/playground.md).
- [Render diagrams from text](extensions/diagrams.md).
