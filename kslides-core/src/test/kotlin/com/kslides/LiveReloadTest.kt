package com.kslides

import com.kslides.Page.generatePage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class LiveReloadTest : StringSpec() {
  // Single-slide deck rendered to "/"; only devMode varies.
  private fun deck(dev: Boolean) =
    kslidesTest {
      output { devMode = dev }
      presentation { markdownSlide { content { "# Hi" } } }
    }.presentation("/")

  init {
    "dev mode over HTTP injects the live-reload client" {
      val html = generatePage(deck(dev = true), useHttp = true, srcPrefix = "/revealjs")
      html shouldContain LiveReload.RELOAD_PATH
      html shouldContain "kslides-devmode-state"
    }

    "filesystem output never injects the live-reload client, even in dev mode" {
      val html = generatePage(deck(dev = true), useHttp = false, srcPrefix = "revealjs")
      html shouldNotContain LiveReload.RELOAD_PATH
    }

    "the live-reload client is absent when dev mode is off" {
      val html = generatePage(deck(dev = false), useHttp = true, srcPrefix = "/revealjs")
      html shouldNotContain LiveReload.RELOAD_PATH
    }
  }
}
