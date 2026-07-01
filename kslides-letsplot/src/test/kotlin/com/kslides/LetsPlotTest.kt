package com.kslides

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.letsPlot

class LetsPlotTest : StringSpec() {
  init {
    "letsPlotContent embeds the script URL" {
      val figure = letsPlot(mapOf("x" to listOf(1, 2), "y" to listOf(3, 4))) +
        geomPoint {
          x = "x"
          y = "y"
        }
      val html = LetsPlot.letsPlotContent(figure, "https://example.com/lets-plot.js", null)
      html.shouldContain("https://example.com/lets-plot.js")
    }

    "letsPlotContent emits an iframe-wrapped document" {
      val figure = letsPlot(mapOf("x" to listOf(1), "y" to listOf(2))) +
        geomPoint {
          x = "x"
          y = "y"
        }
      val html = LetsPlot.letsPlotContent(figure, "https://cdn/lets-plot.js", null)
      html.shouldContain("<iframe")
      html.shouldContain("srcdoc=")
      html.shouldContain("lets-plot")
    }

    "letsPlotContent honors an explicit plotSize" {
      val figure = letsPlot(mapOf("x" to listOf(1), "y" to listOf(2))) +
        geomPoint {
          x = "x"
          y = "y"
        }
      val html = LetsPlot.letsPlotContent(figure, "https://cdn/lets-plot.js", DoubleVector(640.0, 320.0))
      html.shouldContain("640")
      html.shouldContain("320")
    }

    "letsPlotContent tolerates a null plotSize" {
      val figure = letsPlot(mapOf("x" to listOf(1), "y" to listOf(2))) +
        geomPoint {
          x = "x"
          y = "y"
        }
      val html = LetsPlot.letsPlotContent(figure, "https://cdn/lets-plot.js", null)
      (html.isNotBlank()) shouldBe true
    }

    "scriptUrlForVersion returns the Lets-Plot CDN URL for the version" {
      val url = LetsPlot.scriptUrlForVersion("4.9.0")
      url.shouldContain("4.9.0")
      url.shouldContain("lets-plot")
    }

    "the default letsPlotJsVersion resolves to a real CDN URL (not a dev/localhost build)" {
      val version = com.kslides.config
        .KSlidesConfig()
        .letsPlotJsVersion
      val url = LetsPlot.scriptUrlForVersion(version)
      url.shouldContain(version)
      url.shouldContain("cdn.jsdelivr.net")
      url.shouldNotContain("127.0.0.1")
    }

    "isLocalScriptUrl flags localhost/dev URLs and clears CDN URLs" {
      LetsPlot.isLocalScriptUrl("http://127.0.0.1:8080/lets-plot.js") shouldBe true
      LetsPlot.isLocalScriptUrl("http://localhost/lets-plot.js") shouldBe true
      LetsPlot.isLocalScriptUrl("https://cdn.jsdelivr.net/gh/x/lets-plot.js") shouldBe false
    }
  }
}
