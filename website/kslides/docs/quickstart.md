---
icon: lucide/zap
---

# Quickstart

Build your first deck and serve it locally.

!!! tip "Starting from scratch?"

    `kslides-init.sh` scaffolds a complete project — build config, a starter deck, and a
    GitHub Pages workflow — in one command. See [Installation](installation.md#scaffold-a-new-project).

    ```bash
    curl -fsSL https://raw.githubusercontent.com/kslides/kslides/master/kslides-init.sh | bash -s -- my-talk --title "My Talk"
    ```

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

## 5. Iterate without the restart dance

Set `devMode = true` alongside `enableHttp` and the browser follows your edits — reloading on
every app restart and returning you to the slide you were on:

```kotlin
--8<-- "Output.kt:devmode"
```

Pair it with `./kslides-dev.sh` to rebuild and restart automatically on every source change.
See [Dev mode](output.md#dev-mode-live-reload).

## Next steps

- [Configure transitions, navigation links, and theme](configuration.md).
- [Set slide and code font sizes](styling.md#font-sizes) without writing CSS.
- [Embed runnable Kotlin code](extensions/playground.md).
- [Draw Mermaid diagrams](extensions/mermaid.md) that render offline, or
  [other diagram formats via Kroki](extensions/diagrams.md).
