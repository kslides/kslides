package com.kslides

import com.kslides.Mermaid.mermaidContent
import com.kslides.config.MermaidIframeConfig
import com.kslides.slide.DslSlide
import kotlinx.html.*

@KSlidesDslMarker
fun DslSlide.mermaid(
  iframeConfig: MermaidIframeConfig = MermaidIframeConfig(),
  mermaidText: () -> String,
) {
  val filename = newFilename()
  val mergedConfig =
    MermaidIframeConfig()
      .also { config ->
        config.merge(globalMermaidConfig)
        config.merge(presentationMermaidConfig)
        config.merge(iframeConfig)
      }

  recordContent(presentation.kslides, mergedConfig.staticContent, filename, mermaidPath, _useHttp) {
    mermaidContent(presentation.kslides, mermaidText())
  }

  _section?.iframe {
    src = mermaidFilename(filename)
    mergedConfig.width.also { if (it.isNotBlank()) width = it }
    mergedConfig.height.also { if (it.isNotBlank()) height = it }
    mergedConfig.style.also { if (it.isNotBlank()) style = it }
    mergedConfig.title.also { if (it.isNotBlank()) title = it }
  } ?: error("mermaid() must be called from within a content{} block")
}
