package com.github.readingbat

import com.kslides.KSlides
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class OutputConfigTest : StringSpec() {
  init {
    "OutputConfig exposes default dirs and paths" {
      val output = KSlides().outputConfig

      output.outputDir shouldBe "docs"
      output.playgroundDir shouldBe "playground"
      output.letsPlotDir shouldBe "letsPlot"
      output.krokiDir shouldBe "kroki"

      output.playgroundPath shouldBe "docs/playground/"
      output.letsPlotPath shouldBe "docs/letsPlot/"
      output.krokiPath shouldBe "docs/kroki/"
    }

    "OutputConfig respects a custom outputDir and feature dir" {
      val output = KSlides().outputConfig.apply {
        outputDir = "custom-out"
        letsPlotDir = "plots"
      }
      output.letsPlotPath shouldBe "custom-out/plots/"
    }

    "OutputConfig has filesystem and http enabled by default" {
      val output = KSlides().outputConfig
      output.enableFileSystem shouldBe true
      output.enableHttp shouldBe true
      output.httpPort shouldBe 8080
    }
  }
}
