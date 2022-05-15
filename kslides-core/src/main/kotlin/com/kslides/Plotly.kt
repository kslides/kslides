package com.kslides

import com.github.pambrose.common.response.*
import com.kslides.InternalUtils.mkdir
import com.kslides.config.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.html.*
import kotlinx.html.dom.*
import mu.*
import space.kscience.plotly.*
import java.io.*

object Plotly : KLogging() {

  const val argName = "slidename"

  internal fun writePlotlyFile(config: OutputConfig, slide: DslSlide, content: String) {
    // Create directory if missing
    mkdir(config.plotlyPath)

    "${config.plotlyPath}${slide._slideFilename}"
      .also {
        logger.info { "Writing plotly content to: $it" }
        File(it).writeText(content)
      }
  }

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
    }.serialize() //.also { logger.info { "\n$it" } }

  fun Routing.setupPlotly(kslides: KSlides) {
    get(kslides.kslidesConfig.plotlyEndpoint) {
      val params = call.request.queryParameters
      respondWith {
        val path = params[argName] ?: throw IllegalArgumentException("Missing plotly arg $argName")
        kslides.plotlyContent[path] ?: throw IllegalArgumentException("Invalid plotly path: $path")
      }
    }
  }
}