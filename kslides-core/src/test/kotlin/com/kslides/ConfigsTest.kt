package com.kslides

import com.kslides.config.CopyCodeConfig
import com.kslides.config.DiagramConfig
import com.kslides.config.KSlidesConfig
import com.kslides.config.LetsPlotIframeConfig
import com.kslides.config.MenuConfig
import com.kslides.config.PlaygroundConfig
import com.kslides.config.PresentationConfig
import com.kslides.config.SlideConfig
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import kotlin.reflect.full.memberProperties

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

    "KSlidesConfig pins letsPlotJsVersion to the lets-plot core version" {
      // The generated letsPlot{} iframes load this JS runtime; it must match the lets-plot-common
      // core version pulled in transitively by lets-plot-kotlin-jvm (see gradle/libs.versions.toml,
      // `letsplot = "4.14.0"` -> lets-plot-common 4.10.1). Bump both together.
      KSlidesConfig().letsPlotJsVersion shouldBe "4.10.1"
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

    "PresentationConfig routes reveal.js vs kslides options into separate maps" {
      val config = PresentationConfig()
      config.controls = true // reveal.js-managed
      config.enableMenu = true // kslides-managed
      config.revealjsManagedValues.keys shouldContain "controls"
      config.revealjsManagedValues.keys shouldNotContain "enableMenu"
      config.kslidesManagedValues.keys shouldContain "enableMenu"
      config.kslidesManagedValues.keys shouldNotContain "controls"
    }

    "reading an unset PresentationConfig property throws rather than returning a default" {
      shouldThrowExactly<IllegalStateException> { PresentationConfig().controls }
      shouldThrowExactly<IllegalStateException> { PresentationConfig().enableMenu }
    }

    "assignDefaults seeds every kslides-managed option so it reads back without throwing" {
      val config = PresentationConfig().apply { assignDefaults() }
      // Each key that landed in kslidesManagedValues must read back through its delegate (the getter
      // throws only on an unset property), catching a default added without a corresponding seed.
      config.kslidesManagedValues.keys.forEach { key ->
        PresentationConfig::class
          .memberProperties
          .firstOrNull { it.name == key }
          ?.let { prop -> shouldNotThrowAny { prop.getter.call(config) } }
      }
      config.enableMenu shouldBe false
      config.theme shouldBe PresentationTheme.BLACK
      config.title shouldBe ""
    }

    "PresentationConfig cascade: a later merge wins on a reveal.js option" {
      val global = PresentationConfig().apply {
        assignDefaults()
        controls = true
      }
      val presentation = PresentationConfig().apply { controls = false }
      val merged = PresentationConfig().also {
        it.mergeConfig(global)
        it.mergeConfig(presentation)
      }
      merged.controls shouldBe false
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
