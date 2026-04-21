package com.kslides

import com.kslides.config.CopyCodeConfig
import com.kslides.config.DiagramConfig
import com.kslides.config.KSlidesConfig
import com.kslides.config.LetsPlotIframeConfig
import com.kslides.config.MenuConfig
import com.kslides.config.PlaygroundConfig
import com.kslides.config.SlideConfig
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank

class ConfigsTest : StringSpec() {
  init {
    "LetsPlotIframeConfig assigns defaults" {
      val config = LetsPlotIframeConfig().apply { assignDefaults() }
      config.width shouldBe "100%"
      config.height shouldBe ""
      config.style shouldBe ""
      config.title shouldBe ""
      config.staticContent shouldBe false
    }

    "LetsPlotIframeConfig cascade: presentation overrides global" {
      val global = LetsPlotIframeConfig().apply {
        assignDefaults()
        width = "80%"
        staticContent = false
      }
      val presentation = LetsPlotIframeConfig().apply {
        width = "70%"
      }
      val merged = LetsPlotIframeConfig().also {
        it.merge(global)
        it.merge(presentation)
      }
      merged.width shouldBe "70%"
      merged.staticContent shouldBe false
      merged.height shouldBe ""
    }

    "LetsPlotIframeConfig cascade: per-call overrides presentation overrides global" {
      val global = LetsPlotIframeConfig().apply {
        assignDefaults()
        height = "400px"
      }
      val presentation = LetsPlotIframeConfig().apply {
        height = "500px"
      }
      val perCall = LetsPlotIframeConfig().apply {
        height = "600px"
        style = "border: 1px solid;"
      }
      val merged = LetsPlotIframeConfig().also {
        it.merge(global)
        it.merge(presentation)
        it.merge(perCall)
      }
      merged.height shouldBe "600px"
      merged.style shouldBe "border: 1px solid;"
      merged.width shouldBe "100%"
    }

    "LetsPlotIframeConfig companion invoke returns a configured instance" {
      val built = LetsPlotIframeConfig {
        width = "90%"
        staticContent = true
      }
      built.width shouldBe "90%"
      built.staticContent shouldBe true
    }

    "KSlidesConfig exposes a non-blank letsPlotJsVersion default" {
      KSlidesConfig().letsPlotJsVersion.shouldNotBeBlank()
    }

    "KSlidesConfig default URLs are reasonable" {
      val config = KSlidesConfig()
      config.playgroundUrl shouldBe "https://unpkg.com/kotlin-playground@1"
      config.krokiUrl shouldBe "https://kroki.io"
    }

    "PlaygroundConfig assigns defaults" {
      val config = PlaygroundConfig().apply { assignDefaults() }
      config.width shouldBe "100%"
      config.height shouldBe "250px"
      config.staticContent shouldBe false
    }

    "DiagramConfig assigns defaults" {
      val config = DiagramConfig().apply { assignDefaults() }
      config.outputType shouldBe DiagramOutputType.SVG
      config.width shouldBe ""
      config.options.isEmpty() shouldBe true
    }

    "MenuConfig merge combines values without reset" {
      val first = MenuConfig().apply {
        openButton = true
        sticky = false
      }
      val second = MenuConfig().apply {
        sticky = true
      }
      val merged = MenuConfig().also {
        it.merge(first)
        it.merge(second)
      }
      merged.openButton shouldBe true
      merged.sticky shouldBe true
    }

    "CopyCodeConfig merge preserves untouched fields" {
      val first = CopyCodeConfig().apply {
        copy = "Copy"
        timeout = 1000
      }
      val second = CopyCodeConfig().apply {
        copy = "Copied"
      }
      val merged = CopyCodeConfig().also {
        it.merge(first)
        it.merge(second)
      }
      merged.copy shouldBe "Copied"
      merged.timeout shouldBe 1000
    }

    "SlideConfig merge lets the later config win on shared keys" {
      val first = SlideConfig().apply {
        assignDefaults()
        backgroundColor = "black"
      }
      val second = SlideConfig().apply {
        backgroundColor = "white"
      }
      val merged = SlideConfig().also {
        it.merge(first)
        it.merge(second)
      }
      merged.backgroundColor shouldBe "white"
    }
  }
}
