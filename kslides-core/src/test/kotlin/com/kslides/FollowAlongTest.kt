package com.kslides

import com.kslides.Page.generatePage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class FollowAlongTest : StringSpec() {
  // Single-slide deck rendered to "/"; only followAlong/devMode vary.
  private fun deck(
    follow: Boolean,
    dev: Boolean = false,
  ) = kslidesTest {
    output {
      followAlong = follow
      devMode = dev
    }
    presentation { markdownSlide { content { "# Hi" } } }
  }.presentation("/")

  init {
    "followAlong over HTTP injects the follow-along client" {
      val html = generatePage(deck(follow = true), useHttp = true, srcPrefix = "/revealjs")
      html shouldContain FollowAlong.FOLLOW_PATH
      html shouldContain "kslides-follow-badge"
    }

    "filesystem output never injects the follow-along client, even when enabled" {
      val html = generatePage(deck(follow = true), useHttp = false, srcPrefix = "revealjs")
      html shouldNotContain FollowAlong.FOLLOW_PATH
    }

    "the follow-along client is absent when followAlong is off" {
      val html = generatePage(deck(follow = false), useHttp = true, srcPrefix = "/revealjs")
      html shouldNotContain FollowAlong.FOLLOW_PATH
    }

    "the shared injection point carries both client scripts when devMode and followAlong are on" {
      val html = generatePage(deck(follow = true, dev = true), useHttp = true, srcPrefix = "/revealjs")
      html shouldContain LiveReload.RELOAD_PATH
      html shouldContain FollowAlong.FOLLOW_PATH
    }

    "a configured presenterToken is used verbatim" {
      deckKSlides(token = "my-token").outputConfig.followAlongToken shouldBe "my-token"
    }

    "a blank presenterToken generates a random, stable token" {
      val kslides = deckKSlides()
      val token = kslides.outputConfig.followAlongToken
      token.isNotBlank() shouldBe true
      kslides.outputConfig.followAlongToken shouldBe token // stable per instance
      deckKSlides().outputConfig.followAlongToken shouldNotBe token // fresh instance, fresh token
    }
  }

  private fun deckKSlides(token: String = "") =
    kslidesTest {
      output { presenterToken = token }
      presentation { markdownSlide { content { "# Hi" } } }
    }
}
