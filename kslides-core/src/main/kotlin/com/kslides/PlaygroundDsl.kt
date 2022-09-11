package com.kslides

import com.kslides.Playground.playgroundContent
import com.kslides.config.PlaygroundConfig
import com.kslides.slide.DslSlide
import kotlinx.html.*

//context(Presentation, DslSlide, SECTION)
@KSlidesDslMarker
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
      localConfig.css
    )

  recordContent(presentation.kslides, mergedConfig.staticContent, filename, playgroundPath, _useHttp) {
    playgroundContent(presentation.kslides, mergedConfig, combinedCss, srcName, otherSrcs.toList())
  }

  _section?.iframe {
    src = playgroundFilename(filename)
    mergedConfig.width.also { if (it.isNotBlank()) width = it }
    mergedConfig.height.also { if (it.isNotBlank()) height = it }
    mergedConfig.style.also { if (it.isNotBlank()) style = it }
    mergedConfig.title.also { if (it.isNotBlank()) title = it }
  } ?: error("playground() must be called from within a content{} block")
}