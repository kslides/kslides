package com.kslides

import com.kslides.config.CopyCodeButton
import com.kslides.config.CopyCodeConfig
import com.kslides.config.CopyCodeDisplay
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

    "CopyCodeConfig serializes only the set options, using each enum's wire value" {
      val config = CopyCodeConfig().apply {
        button = CopyCodeButton.ALWAYS
        display = CopyCodeDisplay.ICONS
        timeout = 2000
        copy = "Copy"
        scale = 0.8
      }
      val encoded = Json.encodeToString(config.values)

      // Enums emit their @SerialName wire value, not the enum constant name.
      encoded.contains(""""button":"always"""") shouldBe true
      encoded.contains(""""display":"icons"""") shouldBe true
      // Nested + typed values round-trip (copy under text, the Double scale under style).
      encoded.contains(""""copy":"Copy"""") shouldBe true
      encoded.contains(""""scale":0.8""") shouldBe true
      // Unset options are omitted entirely rather than emitted as null.
      encoded.contains("copied") shouldBe false
      encoded.contains("window") shouldBe false
    }

    "reading an unset CopyCodeConfig property throws rather than returning a default" {
      shouldThrowExactly<IllegalStateException> { CopyCodeConfig().copy }
      shouldThrowExactly<IllegalStateException> { CopyCodeConfig().scale }
    }

    "CopyCodeConfig delegates round-trip every value type and backing location" {
      val config = CopyCodeConfig().apply {
        button = CopyCodeButton.HOVER // top-level enum
        timeout = 500 // top-level Int
        window = true // top-level Boolean
        copy = "C" // nested under text
        copybg = "#fff" // nested under style (String)
        scale = 1.25 // nested under style (Double)
      }
      config.button shouldBe CopyCodeButton.HOVER
      config.timeout shouldBe 500
      config.window shouldBe true
      config.copy shouldBe "C"
      config.copybg shouldBe "#fff"
      config.scale shouldBe 1.25
    }

    "CopyCodeConfig merge is per-field, including nested style options" {
      val first = CopyCodeConfig().apply {
        scale = 0.8
        offset = 1.0
        button = CopyCodeButton.ALWAYS
      }
      val second = CopyCodeConfig().apply {
        offset = 2.0 // overrides first
        radius = 0.5 // adds a field first never set
      }
      val merged = CopyCodeConfig().also {
        it.merge(first)
        it.merge(second)
      }
      merged.scale shouldBe 0.8 // untouched by second, preserved
      merged.offset shouldBe 2.0 // later config wins
      merged.radius shouldBe 0.5 // contributed only by second
      merged.button shouldBe CopyCodeButton.ALWAYS // top-level field from first
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

    "revealjsOption sets a raw reveal.js value that cascades through merge" {
      val menu = MenuConfig().apply { revealjsOption("themes", listOf("black", "white")) }
      menu.revealjsManagedValues["themes"] shouldBe listOf("black", "white")

      // The raw value participates in the cascade like any typed option.
      val merged = MenuConfig().also { it.merge(menu) }
      merged.revealjsManagedValues["themes"] shouldBe listOf("black", "white")
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
