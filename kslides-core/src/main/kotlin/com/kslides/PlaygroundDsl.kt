package com.kslides

import com.kslides.Playground.playgroundContent
import com.kslides.config.PlaygroundConfig
import com.kslides.slide.DslSlide
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
 * @throws IllegalStateException if called outside a [DslSlide] `content{}` block.
 */
//context(Presentation, DslSlide, SECTION)
fun DslSlide.playground(
  srcName: String,
  vararg otherSrcs: String = emptyArray(),
  configBlock: PlaygroundConfig.() -> Unit = {},
) {
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

  private_section?.iframe {
    src = playgroundFilename(filename)
    mergedConfig.width.also { if (it.isNotBlank()) width = it }
    mergedConfig.height.also { if (it.isNotBlank()) height = it }
    mergedConfig.style.also { if (it.isNotBlank()) style = it }
    mergedConfig.title.also { if (it.isNotBlank()) title = it }
  } ?: error("playground() must be called from within a content{} block")
}
