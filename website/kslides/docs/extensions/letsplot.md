---
icon: lucide/chart-line
---

# Lets-Plot

The optional `kslides-letsplot` module adds a `letsPlot { }` block that embeds [Lets-Plot Kotlin](https://github.com/JetBrains/lets-plot-kotlin) figures inside a slide.

## Setup

Add the module to your dependencies (see [Installation](../installation.md)):

```kotlin
dependencies {
  implementation("com.github.pambrose:kslides-core:0.25.0")
  implementation("com.github.pambrose:kslides-letsplot:0.25.0")
}
```

## A simple plot

```kotlin
import com.kslides.kslides
import com.kslides.letsplot.letsPlot
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.letsPlot as plot

fun main() {
  kslides {
    presentation {
      dslSlide {
        content {
          letsPlot {
            figure = plot(mapOf(
              "x" to (1..10).toList(),
              "y" to (1..10).map { it * it },
            )) + geomPoint { x = "x"; y = "y" }
          }
        }
      }
    }
  }
}
```

## Output behavior

- **Static site mode** — each figure is written to `docs/letsPlot/<slug>.html` and embedded as an iframe.
- **HTTP mode** — figures are served per-session.

## Configuring the JS runtime

The Lets-Plot JS runtime version is configurable on the global config:

```kotlin
kslides {
  letsPlotJsVersion = "4.9.0"
  // ...
}
```

The default tracks the matched runtime for the bundled `lets-plot-kotlin` version.
