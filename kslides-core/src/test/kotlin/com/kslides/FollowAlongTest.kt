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
      val html = generatePage(deck(follow = true), useHttp = true)
      html shouldContain FollowAlong.FOLLOW_PATH
      html shouldContain "kslides-follow-badge"
    }

    "filesystem output never injects the follow-along client, even when enabled" {
      val html = generatePage(deck(follow = true), useHttp = false, rootPrefix = "")
      html shouldNotContain FollowAlong.FOLLOW_PATH
    }

    "the follow-along client is absent when followAlong is off" {
      val html = generatePage(deck(follow = false), useHttp = true)
      html shouldNotContain FollowAlong.FOLLOW_PATH
    }

    "the shared injection point carries both client scripts when devMode and followAlong are on" {
      val html = generatePage(deck(follow = true, dev = true), useHttp = true)
      html shouldContain LiveReload.RELOAD_PATH
      html shouldContain FollowAlong.FOLLOW_PATH
    }

    "the presenter client carries its token across in-deck links" {
      // A corner link to another deck is a full page load, and the new page reads its role from the
      // query string — so without this the presenter silently lands as a viewer. The rules it
      // encodes are asserted here because they are all easy to lose in an edit: skip in-page '#'
      // navigation (never reloads), skip cross-origin (must not leak the token), and skip a link
      // that already carries it (no double-append).
      val html = generatePage(deck(follow = true), useHttp = true)
      html shouldContain "carryTokenAcrossLinks"
      html shouldContain "a[href]"
      html shouldContain "location.origin"
      html shouldContain FollowAlong.PRESENT_PARAM

      // Injected as page source rather than executed here, so this is a wiring check — the
      // navigation itself was verified in a browser across two decks.
      html shouldContain "DOMContentLoaded"
    }

    "the injected client survives the XML serializer that writes it into the page" {
      // rawSource escapes the script's ampersands on the way in and the serializer hands them back
      // bare, so the client can write them plainly. Asserting each site verbatim rather than
      // slicing the script: an escaped arrival fails these, and the token regex in particular
      // would then match nothing and silently demote the presenter to a viewer.
      val html = generatePage(deck(follow = true, dev = true), useHttp = true)
      html shouldContain "[?&]${FollowAlong.PRESENT_PARAM}=([^&]+)"
      html shouldContain "&role=presenter&token="
      html shouldContain "? '&' : '?'"
      // The live-reload client shares the sink and the same freedom.
      html shouldContain "window.Reveal && Reveal.isReady()"
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
