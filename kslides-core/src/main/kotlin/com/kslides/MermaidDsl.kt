@file:Suppress("MatchingDeclarationName")

package com.kslides

import com.kslides.slide.DslSlide
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.html.SECTION
import kotlinx.html.pre

/**
 * Internal support for the native [mermaid] DSL helper: the bundled runtime path, the
 * reveal.js-theme-aware init snippet, and the head CSS that [Page] emits for presentations
 * containing at least one mermaid block.
 */
internal object Mermaid {
  internal val logger = KotlinLogging.logger {}

  // Bundled Mermaid version: 11.16.0 (UMD build from
  // https://cdn.jsdelivr.net/npm/mermaid@11.16.0/dist/mermaid.min.js), checked in at
  // docs/revealjs/plugin/mermaid/mermaid.min.js. docs/revealjs/ is the single source of truth
  // for browser assets: filesystem-mode links resolve against the copied docs/revealjs/ tree,
  // and kslides-core's processResources grafts docs/revealjs/** onto the published JAR
  // classpath at revealjs/** so the Ktor static handler (which registers the "plugin" static
  // root) serves the same file in HTTP mode.
  internal const val MERMAID_JS_PATH = "plugin/mermaid/mermaid.min.js"

  // reveal.js themes with dark backgrounds; they map to Mermaid's "dark" theme, everything
  // else to Mermaid's "default" (light) theme.
  private val darkRevealThemes =
    setOf(
      PresentationTheme.BLACK,
      PresentationTheme.BLACK_CONTRAST,
      PresentationTheme.BLOOD,
      PresentationTheme.DRACULA,
      PresentationTheme.LEAGUE,
      PresentationTheme.MOON,
      PresentationTheme.NIGHT,
    )

  internal fun mermaidTheme(theme: PresentationTheme) = if (theme in darkRevealThemes) "dark" else "default"

  // Strip the reveal.js code-block chrome from the diagram containers and hide the raw diagram
  // source until Mermaid replaces it with an SVG (Mermaid marks each element data-processed
  // before rendering it). visibility (rather than display) keeps the element's layout intact so
  // Mermaid's size calculations still work.
  internal val headCss =
    """
    .reveal pre.mermaid {
      width: auto;
      text-align: center;
      box-shadow: none;
      background: transparent;
    }
    .reveal pre.mermaid:not([data-processed]) {
      visibility: hidden;
    }
    """.trimIndent()

  // Hidden reveal.js sections are display:none, which breaks Mermaid's size calculations, so
  // startOnLoad is disabled and diagrams render lazily as their slide becomes visible. Print
  // view lays every slide out at once, so there the whole deck renders up front. The trailing
  // isReady() call covers the race where reveal.js finishes initializing while mermaid.min.js
  // is still being fetched (the ready event would then fire before the listener is attached).
  internal fun initScript(theme: PresentationTheme) =
    """
    mermaid.initialize({ startOnLoad: false, theme: '${mermaidTheme(theme)}' });
    function kslidesMermaidRun(root) {
      var nodes = root.querySelectorAll('pre.mermaid:not([data-processed])');
      if (nodes.length > 0) mermaid.run({ nodes: nodes });
    }
    function kslidesMermaidSync() {
      if (Reveal.isPrintView()) kslidesMermaidRun(document);
      else if (Reveal.getCurrentSlide()) kslidesMermaidRun(Reveal.getCurrentSlide());
    }
    Reveal.on('ready', kslidesMermaidSync);
    Reveal.on('slidechanged', kslidesMermaidSync);
    if (Reveal.isReady()) kslidesMermaidSync();
    """.trimIndent()
}

/**
 * Embed a client-side [Mermaid](https://mermaid.js.org) diagram inside a [DslSlide] `content{}`
 * block. Unlike [diagram] (which renders through a Kroki server), the diagram is rendered in the
 * browser by the Mermaid runtime bundled with kslides — no network access or external service is
 * required, so decks keep working offline.
 *
 * Emits a `<pre class="mermaid">` element whose text content is the trimIndent-ed [source].
 * Presentations containing at least one mermaid block automatically get the bundled Mermaid
 * runtime plus an init snippet that renders each diagram as its slide becomes visible (hidden
 * reveal.js slides are `display:none`, which breaks Mermaid's size calculations, so diagrams are
 * not rendered up front). Mermaid's dark theme is selected automatically when the presentation's
 * reveal.js theme is dark.
 *
 * Diagram source can also be loaded from a file or URL via [include]:
 * `mermaid(include("path/to/diagram.mmd"))`.
 *
 * @param source Mermaid diagram source, e.g. `"graph TD; A-->B"`. A blank value logs a warning
 *   and emits nothing.
 *
 * The enclosing `<section>` is supplied via the [SECTION] context parameter, so calling this
 * outside a [DslSlide] `content{}` block is a compile-time error.
 */
context(section: SECTION)
fun DslSlide.mermaid(source: String) {
  if (source.isBlank()) {
    Mermaid.logger.warn { "mermaid() called with blank diagram source; skipping the embed" }
    return
  }
  presentation.mermaidUsed = true
  section.pre(classes = "mermaid") {
    +source.trimIndent()
  }
}
