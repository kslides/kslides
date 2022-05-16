package com.kslides

import com.kslides.config.*
import kotlinx.html.*
import kotlinx.html.dom.*
import mu.*
import space.kscience.plotly.*

object Plotly : KLogging() {

  fun plotlyContent(config: KSlidesConfig, block: SECTION.() -> Unit) =
    document {
      append.html {
        head {
          //script { src = config.plotlyUrl; type = "text/javascript" }
        }
        body {
          cdnPlotlyHeader.visit(consumer)
          section {
            block(this)
          }
        }
      }
    }.serialize()
}