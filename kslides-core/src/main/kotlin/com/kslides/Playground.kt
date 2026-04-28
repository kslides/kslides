package com.kslides

import com.kslides.CssValue.Companion.writeCssToHead
import com.kslides.config.PlaygroundConfig
import com.kslides.config.PlaygroundConfig.Companion.toPropertyName
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.dom.document
import kotlinx.html.dom.serialize

/**
 * Renderer for Kotlin Playground iframe documents. Builds a self-contained HTML page that boots
 * the Kotlin Playground script (per [com.kslides.config.KSlidesConfig.playgroundUrl]) against a
 * primary source file plus optional dependency files, applying the supplied CSS to the page head.
 *
 * The single rendering helper is `internal`; consumers reach this functionality through the
 * [com.kslides.playground] DSL function rather than calling here directly.
 */
object Playground {
  private val logger = KotlinLogging.logger {}

  internal fun playgroundContent(
    kslides: KSlides,
    config: PlaygroundConfig,
    combinedCss: CssValue,
    srcName: String,
    otherSrcs: List<String>,
  ) = document {
    val kslidesConfig = kslides.kslidesConfig
    append.html {
      head {
        script {
          src = kslidesConfig.playgroundUrl
          attributes["data-selector"] = ".${kslidesConfig.playgroundSelector}"
        }
        writeCssToHead(combinedCss)
      }
      body {
        code(kslidesConfig.playgroundSelector) {
          config.toAttributes().forEach { attributes[it.first.toPropertyName()] = it.second }

          logger.info { "Including file: $srcName" }
          +include(srcName)

          otherSrcs
            .forEach { filename ->
              logger.info { "Including additional file: $filename" }
              textArea(classes = "hidden-dependency") {
                +this@code.include(filename)
              }
            }
        }
      }
    }
  }.serialize()
}
