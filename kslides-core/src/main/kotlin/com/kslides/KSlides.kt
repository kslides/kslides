package com.kslides

import com.kslides.Output.runHttpServer
import com.kslides.Output.writeToFileSystem
import kotlinx.css.*
import kotlinx.html.*
import mu.*

@HtmlTagMarker
fun kslides(kslidesBlock: KSlides.() -> Unit) =
  KSlides()
    .apply {
      kslidesBlock()
      require(presentationBlocks.isNotEmpty()) { "At least one presentation must be defined" }

      configBlock(globalConfig)

      presentationBlocks.forEach { presentationBlock ->
        Presentation(this)
          .apply {
            presentationBlock(this)
            require(slides.isNotEmpty()) { "At least one slide must be defined for a presentation" }
            validatePath()

            finalConfig =
              PresentationConfig()
                .apply {
                  merge(kslides.globalConfig)
                  merge(presentationConfig)
                }

            assignCssFiles()
            assignJsFiles()
            assignPlugins()
            assignDependencies()
          }
      }

      outputBlock(presentationOutput)

      if (presentationOutput.enableFileSystem)
        writeToFileSystem(presentationOutput)

      if (presentationOutput.enableHttp)
        runHttpServer(presentationOutput)

      if (!presentationOutput.enableFileSystem && !presentationOutput.enableHttp)
        KSlides.logger.warn { "Set enableHttp or enableFileSystem to true in the output block" }
    }

class KSlides {
  internal val globalConfig = PresentationConfig(true)
  internal val presentationOutput = PresentationOutput(this)
  internal var configBlock: PresentationConfig.() -> Unit = {}
  internal var outputBlock: PresentationOutput.() -> Unit = {}
  internal var presentationBlocks = mutableListOf<Presentation.() -> Unit>()
  internal val presentationMap = mutableMapOf<String, Presentation>()
  internal val presentations get() = presentationMap.values
  internal fun presentation(name: String) = presentationMap[name] ?: throw IllegalArgumentException("Presentation $name not found")
  val staticRoots = mutableListOf("assets", "css", "dist", "js", "plugin")
  val css = AppendableString()

  @HtmlTagMarker
  fun css(block: CssBuilder.() -> Unit) {
    css += "${CssBuilder().apply(block)}\n"
  }

  @HtmlTagMarker
  fun presentationDefault(block: PresentationConfig.() -> Unit) {
    configBlock = block
  }

  @HtmlTagMarker
  fun output(outputBlock: PresentationOutput.() -> Unit) {
    this.outputBlock = outputBlock
  }

  @HtmlTagMarker
  fun presentation(block: Presentation.() -> Unit) {
    presentationBlocks += block
  }

  companion object : KLogging()
}