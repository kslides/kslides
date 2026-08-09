package com.kslides

import com.kslides.Page.generatePage
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File

/**
 * Exercises the public [include] function directly — its `../` traversal guard, the
 * recoverable-vs-propagated failure contract, the begin/end-token + line-pattern slicing, and that
 * included content survives the render. Local fixtures are created under the process working
 * directory (`user.dir`) because that is the root [include] resolves relative paths against.
 */
class IncludeTest : StringSpec() {
  init {
    val workDir = File(System.getProperty("user.dir"))

    fun withTempFile(
      content: String,
      block: (relName: String) -> Unit,
    ) {
      val file = File.createTempFile("kslides-include", ".kt", workDir)
      try {
        file.writeText(content)
        block(file.name)
      } finally {
        file.delete()
      }
    }

    "include() rejects local paths that escape the working directory" {
      shouldThrowExactly<IllegalArgumentException> { include("../secret.txt") }
      shouldThrowExactly<IllegalArgumentException> { include("a/../../etc/passwd") }
    }

    "include() does not apply the ../ guard to URLs" {
      // isUrl() short-circuits the local-path traversal guard; an unreachable URL just yields an
      // empty string via the recoverable I/O path, so no IllegalArgumentException is thrown.
      shouldNotThrow<IllegalArgumentException> { include("https://kslides.invalid/../nope.kt") }
    }

    "include() returns an empty string when a local file is missing (recoverable I/O failure)" {
      include("this-file-does-not-exist-9f3a2b.kt") shouldBe ""
    }

    "include() returns the full file contents by default" {
      withTempFile("line1\nline2\nline3") { name ->
        include(name, trimIndent = false, indentToken = "", escapeHtml = false) shouldBe "line1\nline2\nline3"
      }
    }

    "include() honors a line pattern" {
      withTempFile("a\nb\nc\nd") { name ->
        include(name, linePattern = "2-3", trimIndent = false, indentToken = "", escapeHtml = false) shouldBe "b\nc"
      }
    }

    "include() slices between begin and end tokens" {
      withTempFile("pre\n// begin\nkept1\nkept2\n// end\npost") { name ->
        include(
          name,
          beginToken = "// begin",
          endToken = "// end",
          trimIndent = false,
          indentToken = "",
          escapeHtml = false,
        ) shouldBe "kept1\nkept2"
      }
    }

    "include() propagates a missing begin token rather than silently returning an empty slide" {
      withTempFile("a\nb\nc") { name ->
        shouldThrowExactly<IllegalArgumentException> {
          include(name, beginToken = "NOT-PRESENT")
        }
      }
    }

    "a page renders an included em dash instead of dying on the entity" {
      // Regression, and the only level that catches it: escapeHtml4 named every entity it knew, so
      // an em dash became "&mdash;" and the XML parse on the way into the DOM threw
      // SAXParseException — taking down every deck rather than the one slide, since rendering is a
      // single pass. Asserting on include()'s return value cannot reach that.
      //
      // Deliberately an htmlSlide: that is the last sink whose content is still parsed, so it is
      // the only one that still exercises the escaping default. A markdownSlide would resolve to
      // MarkdownSlide.include and quietly test nothing.
      withTempFile("val x = 1 // an — dash") { name ->
        generatePage(
          kslidesTest {
            presentation { htmlSlide { content { "<pre><code>${include(name)}</code></pre>" } } }
          }.presentation("/"),
          // Element content, so the serializer writes the dash back as an entity itself. The
          // regression this guards is upstream of that: escapeHtml4 would put "&mdash;" into
          // include()'s own output, and the parse would throw before reaching here.
        ) shouldContain "an &mdash; dash"
      }
    }
  }
}
