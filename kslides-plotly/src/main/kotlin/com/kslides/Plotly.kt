package com.kslides

import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.dom.document
import kotlinx.html.dom.serialize
import mu.two.KLogging
import space.kscience.plotly.cdnPlotlyHeader

object Plotly : KLogging() {
  internal fun plotlyContent(
    kslides: KSlides,
    block: SECTION.() -> Unit,
  ) = document {
    append.html {
      head {
        //script { src = kslides.kslidesConfig.plotlyUrl; type = "text/javascript" }
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
