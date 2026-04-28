package com.kslides

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class EnumsTest : StringSpec() {
  init {
    "Transition.asInOut / asIn / asOut follow reveal.js naming" {
      Transition.FADE.asInOut() shouldBe "fade"
      Transition.FADE.asIn() shouldBe "fade-in"
      Transition.FADE.asOut() shouldBe "fade-out"
      Transition.CONCAVE.asInOut() shouldBe "concave"
    }

    "Effect.NONE emits just the fragment class" {
      Effect.NONE.toOutput() shouldBe "class=\"fragment\""
    }

    "non-NONE Effect appends a hyphenated modifier" {
      Effect.FADE_IN_THEN_OUT.toOutput() shouldContain "fade-in-then-out"
      Effect.HIGHLIGHT_CURRENT_BLUE.toOutput() shouldContain "highlight-current-blue"
    }

    "Highlight.cssSrc points at the plugin directory" {
      Highlight.MONOKAI.cssSrc shouldBe "plugin/highlight/monokai.css"
      Highlight.ZENBURN.cssSrc shouldBe "plugin/highlight/zenburn.css"
    }

    "PresentationTheme.cssSrc converts underscore to hyphen" {
      PresentationTheme.BLACK.cssSrc shouldBe "dist/theme/black.css"
      PresentationTheme.BLACK_CONTRAST.cssSrc shouldBe "dist/theme/black-contrast.css"
      PresentationTheme.WHITE_CONTRAST.cssSrc shouldBe "dist/theme/white-contrast.css"
    }

    "HrefTarget.htmlVal produces an underscore-prefixed lowercase name" {
      HrefTarget.SELF.htmlVal shouldBe "_self"
      HrefTarget.BLANK.htmlVal shouldBe "_blank"
      HrefTarget.PARENT.htmlVal shouldBe "_parent"
      HrefTarget.TOP.htmlVal shouldBe "_top"
    }

    "TargetPlatform.queryVal defaults to the lowercased name" {
      TargetPlatform.JUNIT.queryVal shouldBe "junit"
      TargetPlatform.CANVAS.queryVal shouldBe "canvas"
      TargetPlatform.JS.queryVal shouldBe "js"
      TargetPlatform.JAVA.queryVal shouldBe "java"
    }

    "TargetPlatform.JSIR uses its explicit override value" {
      TargetPlatform.JSIR.queryVal shouldBe "js-ir"
    }

    "PlaygroundMode.queryVal defaults to lowercased name, with an explicit override for OBJC" {
      PlaygroundMode.KOTLIN.queryVal shouldBe "kotlin"
      PlaygroundMode.SWIFT.queryVal shouldBe "swift"
      PlaygroundMode.OBJC.queryVal shouldBe "obj-c"
    }
  }
}
