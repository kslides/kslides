package com.kslides

import kotlinx.html.*
import kotlinx.html.dom.*
import mu.KLogging
import space.kscience.plotly.cdnPlotlyHeader

object Plotly : KLogging() {
  internal fun plotlyContent(kslides: KSlides, block: SECTION.() -> Unit) =
    document {
      append.html {
        head {
          //script { src = kslides.internalKSlidesConfig.plotlyUrl; type = "text/javascript" }
        }
        body {
          cdnPlotlyHeader.visit(consumer)
          section {
            block()
          }
        }
      }
    }.serialize()
}