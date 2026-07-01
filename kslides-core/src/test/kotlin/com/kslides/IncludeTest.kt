package com.kslides

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.File

/**
 * Exercises the public [include] function directly — its `../` traversal guard, the
 * recoverable-vs-propagated failure contract, and the begin/end-token + line-pattern slicing.
 * Local fixtures are created under the process working directory (`user.dir`) because that is the
 * root [include] resolves relative paths against.
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
  }
}
