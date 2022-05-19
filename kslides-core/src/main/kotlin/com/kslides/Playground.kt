package com.kslides

import com.kslides.CssValue.Companion.writeCssToHead
import com.kslides.config.*
import com.kslides.config.PlaygroundConfig.Companion.toPropertyName
import kotlinx.html.*
import kotlinx.html.dom.*
import mu.*
import kotlin.collections.set

object Playground : KLogging() {
  internal fun playgroundContent(
    kslides: KSlides,
    config: PlaygroundConfig,
    combinedCss: CssValue,
    srcName: String,
    otherSrcs: List<String>
  ) =
    document {
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