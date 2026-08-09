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

## Follow-along presenting

`followAlong = true` is the other HTTP-only output flag. It keeps a remote audience on the presenter's slide over a websocket, with a break-away/rejoin toggle — see [Presenting](presenting.md). Like `devMode`, it has no effect on filesystem output and is never written into the static pages under `outputDir`. The two can be enabled together; both client scripts share one injection point.

## Multiple presentations in one program

```kotlin
--8<-- "Output.kt:multi"
```

Each `presentation { }` becomes a separate page; nested directories under `path` map to nested directories on disk.

## Driving kslides from your own code

Tooling that renders presentations on its own terms — the way `exportPdf()` does — can evaluate a
deck without producing any output, then serve it:

```kotlin
--8<-- "Output.kt:programmatic"
```

`buildKSlides()` applies every configuration block, including `output { }`, but writes no files and
starts no server. `startHttpServer()` then runs the same Ktor server `enableHttp = true` would, and
`presentationPaths` lists what it serves, in declaration order.

## Asset paths

Write asset paths relative to the **output root** — the directory your decks are written to, not the
deck's own location. kslides resolves them from wherever the deck sits, so one path works for a deck
at the root and a deck nested several levels down:

```kotlin
presentation {
  path = "talks/deep/deck.html"          // three levels down
  presentationConfig {
    favicon = "favicon.ico"              // resolves to ../../favicon.ico
    customTheme { logo("images/logo.png") }
    topLeftSvgSrc = "images/gh.svg"
  }
  dslSlide {
    slideConfig { backgroundImage = "images/bg.png" }
    content { playground("src/Hello.kt") }   // iframe content resolves too
  }
}
```

Absolute (`/img/x.png`), external (`https://…`) and `data:` values are used exactly as written.

A few things are **deliberately** left alone, because kslides hands them to something else rather
than emitting them as URLs itself — write these relative to the deck:

- corner links (`topLeftHref`, `topRightHref`) and `logo(href = )`, which are navigation targets
- image paths written inside Markdown or HTML slide content, which reveal.js receives unparsed
- `menuConfig { themesPath }`, which the menu plugin resolves itself

Filesystem output keeps these links relative, so a site published under a path prefix still resolves;
HTTP mode addresses them absolutely, which is where the routes are registered.

## When to use which

| You want…                                            | Use                              |
|------------------------------------------------------|----------------------------------|
| Deploy to GitHub Pages / Netlify                     | `enableFileSystem = true`        |
| Local preview during development                     | `enableHttp = true` (default)    |
| Render dynamic data per request                      | HTTP only                        |
| Present live to a remote audience                    | `followAlong = true` (HTTP only) |
| Hand someone the deck as a file                      | [`exportPdf()`](pdf-export.md)   |
| Both — preview locally, deploy the static artifact   | Leave both enabled               |
