package com.kslides

import com.kslides.Playground.playgroundContent
import com.kslides.config.PlaygroundConfig
import com.kslides.slide.DslSlide
import kotlinx.html.SECTION
import kotlinx.html.iframe
import kotlinx.html.style
import kotlinx.html.title

/**
 * Embed an interactive [Kotlin Playground](https://github.com/JetBrains/kotlin-playground) iframe
 * inside a [DslSlide] `content{}` block. The source file referenced by [srcName] is inlined into
 * the Playground editor; any [otherSrcs] are attached as hidden `<textarea class="hidden-dependency">`
 * elements that Playground loads alongside the main file.
 *
 * When Playground's `dataTargetPlatform` is [TargetPlatform.JUNIT], the included file must not
 * contain a `package` declaration.
 *
 * @param srcName path or URL of the primary source file shown in the editor.
 * @param otherSrcs additional source files attached as hidden dependencies (supporting classes,
 *   JUnit helpers, etc.).
 * @param configBlock optional [PlaygroundConfig] overrides (iframe size, editor theme, target
 *   platform, auto-complete, etc.). Merged with global and presentation-level defaults.
 *
 * The enclosing `<section>` is supplied via the [SECTION] context parameter, so calling this
 * outside a [DslSlide] `content{}` block is a compile-time error.
 */
context(section: SECTION)
fun DslSlide.playground(
  srcName: String,
  vararg otherSrcs: String = emptyArray(),
  configBlock: PlaygroundConfig.() -> Unit = {},
) {
  if (srcName.isBlank()) {
    KSlides.logger.warn { "playground() called with a blank srcName; skipping the iframe" }
    return
  }
  val filename = newFilename()
  val localConfig = PlaygroundConfig().apply { configBlock() }
  val mergedConfig =
    PlaygroundConfig()
      .also { config ->
        config.merge(globalPlaygroundConfig)
        config.merge(presentationPlaygroundConfig)
        config.merge(localConfig)
      }

  // CSS values are additive
  val combinedCss =
    CssValue(
      globalPlaygroundConfig.css,
      presentationPlaygroundConfig.css,
      localConfig.css,
    )

  recordIframeContent(private_useHttp, mergedConfig.staticContent, presentation.kslides, playgroundPath, filename) {
    playgroundContent(presentation.kslides, mergedConfig, combinedCss, srcName, otherSrcs.toList())
  }

  section.iframe {
    src = playgroundFilename(filename)
    mergedConfig.width.also { if (it.isNotBlank()) width = it }
    mergedConfig.height.also { if (it.isNotBlank()) height = it }
    mergedConfig.style.also { if (it.isNotBlank()) style = it }
    mergedConfig.title.also { if (it.isNotBlank()) title = it }
  }
}
