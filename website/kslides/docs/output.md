---
icon: lucide/share-2
---

# Output modes

kslides can render to two destinations, independently or together. Both are configured inside `output { }`.

## Static site

```kotlin
--8<-- "Output.kt:filesystem"
```

Files land under `outputDir` (default `docs/`) — perfect for pushing to GitHub Pages or Netlify.

When the deck uses `playground { }`, `letsPlot { }`, or `diagram { }`, kslides emits the iframe content as separate HTML files under `docs/playground/`, `docs/letsPlot/`, and `docs/kroki/` respectively.

## HTTP server

```kotlin
--8<-- "Output.kt:http"
```

This starts a Ktor server on the chosen port. Iframe content is generated on the fly and cached per session.

## Dev mode (live reload)

```kotlin
--8<-- "Output.kt:devmode"
```

With `devMode = true` (which requires `enableHttp`), every served page embeds a small client that reconnects to the server over a websocket and refreshes the browser — restoring the current slide and fragment — whenever the server restarts.

Because slide content is compiled Kotlin, picking up an edit requires restarting the JVM. Trigger that however you like:

- **`./kslides-dev.sh`** — a watcher script that recompiles and restarts the app on every source change, so the loop is fully automatic: edit a slide, save, and the browser updates on the same slide.
- **IDE rerun** — press Run again on `main()`; the browser reconnects and refreshes. This is also the path on Windows.

`./gradlew -t run` is *not* a reliable trigger: Gradle's continuous build cannot restart a long-running (blocking) server task.

**Note:** keep `devMode` out of published builds. If `enableFileSystem` is also enabled, the generated static pages under `outputDir` embed the reload client too. That is harmless on a static host — the client simply can't reach the websocket and retries quietly — but it is dead weight in your deployable output. Use `devMode = false` (or disable filesystem output) for the run that produces the `docs/` you publish.

## Multiple presentations in one program

```kotlin
--8<-- "Output.kt:multi"
```

Each `presentation { }` becomes a separate page; nested directories under `path` map to nested directories on disk.

## When to use which

| You want…                                            | Use                              |
|------------------------------------------------------|----------------------------------|
| Deploy to GitHub Pages / Netlify                     | `enableFileSystem = true`        |
| Local preview during development                     | `enableHttp = true` (default)    |
| Render dynamic data per request                      | HTTP only                        |
| Both — preview locally, deploy the static artifact   | Leave both enabled               |
