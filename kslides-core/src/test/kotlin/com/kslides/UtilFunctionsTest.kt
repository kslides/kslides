package com.kslides

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class UtilFunctionsTest : StringSpec() {
  init {
    "Dimensions and 'by' infix construct matching values" {
      val d = 800 by 600
      d.width shouldBe 800
      d.height shouldBe 600
    }

    "slideBackground renders a reveal.js data-background comment" {
      slideBackground("#123456") shouldBe "<!-- .slide: data-background=\"#123456\" -->"
    }

    "fragment with NONE and no index emits just the class attribute" {
      fragment() shouldBe "<!-- .element: class=\"fragment\" -->"
    }

    "fragment with a specific Effect adds the hyphenated modifier" {
      fragment(Effect.FADE_UP).shouldContain("fade-up")
    }

    "fragment with an index adds data-fragment-index; zero is omitted" {
      fragment(Effect.NONE, 0).shouldNotContain("data-fragment-index")
      fragment(Effect.NONE, 3).shouldContain("data-fragment-index=\"3\"")
    }

    "permuteBy yields the elements in the specified orders" {
      val list = listOf("a", "b", "c")
      val perms = list.permuteBy(listOf(0, 1, 2), listOf(2, 0, 1)).toList()
      perms shouldBe listOf(listOf("a", "b", "c"), listOf("c", "a", "b"))
    }

    "githubSourceUrl builds the blob URL" {
      githubSourceUrl("acct", "repo", "a/b.kt") shouldBe
        "https://github.com/acct/repo/blob/master/a/b.kt"
    }

    "githubSourceUrl honors a custom branch" {
      githubSourceUrl("acct", "repo", "a/b.kt", "dev") shouldBe
        "https://github.com/acct/repo/blob/dev/a/b.kt"
    }

    "githubRawUrl builds the raw.githubusercontent URL" {
      githubRawUrl("acct", "repo", "a/b.kt") shouldBe
        "https://raw.githubusercontent.com/acct/repo/master/a/b.kt"
    }

    "toLinePatterns splits on pipes and maps '*' to empty" {
      "[1-5|8|*]".toLinePatterns() shouldBe listOf("1-5", "8", "")
      "(  1, 3 | * )".toLinePatterns() shouldBe listOf("1,3", "")
      "".toLinePatterns() shouldBe listOf("")
    }
  }
}
