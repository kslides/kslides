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
