package com.kslides

import com.kslides.InternalUtils.fixIndents
import com.kslides.InternalUtils.fromTo
import com.kslides.InternalUtils.indentInclude
import com.kslides.InternalUtils.isUrl
import com.kslides.InternalUtils.stripBraces
import com.kslides.InternalUtils.toIntList
import com.kslides.InternalUtils.toLineRanges
import com.kslides.InternalUtils.trimIndentWithInclude
import com.kslides.config.PresentationConfig
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

class UtilsTest : StringSpec() {
  init {
    "String.toIntList()" {

      "1,2,4".toIntList() shouldBe listOf(1, 2, 4)

      "[1,2,4]".toIntList() shouldBe listOf(1, 2, 4)

      "(1,2,4)".toIntList() shouldBe listOf(1, 2, 4)

      "3-5".toIntList() shouldBe listOf(3, 4, 5)

      "5-3".toIntList() shouldBe listOf(5, 4, 3)

      "2, 4 - 6, 8".toIntList() shouldBe listOf(2, 4, 5, 6, 8)

      "2, 6 - 4, 8".toIntList() shouldBe listOf(2, 6, 5, 4, 8)

      "2, 4,".toIntList() shouldBe listOf(2, 4)

      "".toIntList() shouldBe listOf()

      "[]".toIntList() shouldBe listOf()

      "6".toIntList() shouldBe listOf(6)

      ",5".toIntList() shouldBe listOf(5)

      ",".toIntList() shouldBe listOf()
    }

    "toIntList rejects a range with more than two endpoints" {
      shouldThrowExactly<IllegalArgumentException> { "1-2-3".toIntList() }
    }

    "toIntList reports malformed endpoints as IllegalArgumentException, not NumberFormatException" {
      // A leading '-' makes the first endpoint blank; a non-numeric token is likewise invalid.
      // Both must surface uniformly rather than leaking a raw NumberFormatException.
      shouldThrowExactly<IllegalArgumentException> { "-5".toIntList() }
      shouldThrowExactly<IllegalArgumentException> { "a".toIntList() }
      shouldThrowExactly<IllegalArgumentException> { "1-b".toIntList() }
    }

    "toIntList accepts ':' as a range separator and ';' as an element separator" {
      "1:3".toIntList() shouldBe listOf(1, 2, 3)
      "1;3".toIntList() shouldBe listOf(1, 3)
      "1:3;5".toIntList() shouldBe listOf(1, 2, 3, 5)
    }

    "trimIndentWithInclude preserves indentation inside a ~~~ fence" {
      // ~~~ fences are handled like ``` fences: the fence markers and the first content line are
      // left-trimmed, while deeper content lines keep their indentation.
      val s =
        """
      # Presentation // NO TAB

      ~~~kotlin      // NO TAB
      val a = 0      // NO TAB
         val x = 1   // WITH TAB
      ~~~            // NO TAB
      """

      s
        .trimIndentWithInclude()
        .lines()
        .forEach {
          if (it.contains("NO TAB"))
            it.trimStart().length shouldBe it.length

          if (it.contains("WITH TAB"))
            it.trimStart().length shouldNotBe it.length
        }
    }

    "Code fence test with include" {
      val str =
        """
      # Presentation // NO TAB

      ````kotlin     // NO TAB
      val x = 1      // NO TAB
val y = 1              // NO TAB
      ````           // NO TAB
      """

      str
        .trimIndentWithInclude()
        .lines()
        .forEach {
          if (it.contains("NO TAB")) {
            it.trimStart().length shouldBe it.length
          } else {
            if (it.isNotBlank()) it.trimStart().length shouldNotBe it.length
          }
        }
    }

    "PresentationConfig Test 1" {
      val p1 = PresentationConfig().apply { assignDefaults() }
      val p2 = PresentationConfig()
      val p3 =
        PresentationConfig()
          .also { config ->
            config.mergeConfig(p1)
            config.mergeConfig(p2)
          }
      p3.enableMenu shouldBe false
    }

    "PresentationConfig Test 2" {
      val p1 = PresentationConfig().apply {
        assignDefaults()
        enableMenu = false
      }
      val p2 = PresentationConfig()
      val p3 =
        PresentationConfig()
          .also { config ->
            config.mergeConfig(p1)
            config.mergeConfig(p2)
          }
      p3.enableMenu shouldBe false
    }

    "PresentationConfig Test 3" {
      val p1 = PresentationConfig().apply {
        assignDefaults()
        enableMenu = false
      }
      val p2 = PresentationConfig().apply {
        enableMenu = true
      }
      val p3 =
        PresentationConfig()
          .also { config ->
            config.mergeConfig(p1)
            config.mergeConfig(p2)
          }
      p3.enableMenu shouldBe true
    }

    "PresentationConfig Test 4" {
      val p1 = PresentationConfig().apply {
        assignDefaults()
        enableMenu = true
      }
      val p2 = PresentationConfig().apply {
        enableMenu = false
      }
      val p3 =
        PresentationConfig()
          .also { config ->
            config.mergeConfig(p1)
            config.mergeConfig(p2)
          }
      p3.enableMenu shouldBe false
    }

    "From/To Test 1" {
      val text = """
      1
      2
      3
      4
      5
    """

      val lines = text.lines().filter { it.trim().isNotBlank() }

      lines.fromTo("", "").size shouldBe 5

      lines.fromTo("", "", false).size shouldBe 5

      lines.fromTo("1", "").size shouldBe 4

      lines.fromTo("1", "", false).size shouldBe 5

      lines.fromTo("", "5").size shouldBe 4

      lines.fromTo("", "5", false).size shouldBe 5

      lines.fromTo("1", "2").size shouldBe 0

      lines.fromTo("1", "2", false).size shouldBe 2

      lines.fromTo("1", "3").also {
        it.size shouldBe 1
        it[0] shouldContain "2"
      }

      lines.fromTo("1", "3", false).also {
        it.size shouldBe 3
        it[0] shouldContain "1"
        it[1] shouldContain "2"
        it[2] shouldContain "3"
      }

      lines.fromTo("2", "5").size shouldBe 2

      lines.fromTo("2", "5", false).size shouldBe 4
    }

    // This tests for the case where you are matching for a token in the same file
    // and you want to avoid matching the invocation
    "From/To Test 2" {
      val text = """
      "1"
      1
      2
      3
      "3"
    """

      val lines = text.lines().filter { it.trim().isNotBlank() }

      lines.fromTo("1", "3", false).also {
        it.size shouldBe 3
        it[0] shouldContain "1"
        it[1] shouldContain "2"
        it[2] shouldContain "3"
      }
    }

    "From/To Test 3" {
      val text = """
    verticalSlides {
      // image begin
      markdownSlide {
        // Size of image is controlled by css above
        content {
          ""${'"'}
          ## Images

          ![revealjs-image](images/revealjs.png)
          ""${'"'}
        }
      }
      // image end

      markdownSlide {
        content {
          ""${'"'}
          ## Images Slide Description
          ```kotlin []
          include(slides, beginToken = "image begin", endToken = "image end")
          ```
          ""${'"'}
        }
      }
    }
    """

      val quoted = Regex("image end\"")
      val nonquoted = Regex("image end")

      """endToken = "image end")""".also {
        it.contains(quoted) shouldBe true
        it.contains(nonquoted) shouldBe true
      }
      "// image end".also {
        it.contains(quoted) shouldBe false
        it.contains(nonquoted) shouldBe true
      }

      val lines = text.lines()

      lines.fromTo("image begin", "image end").size shouldBe 10
    }

    "Lines Test" {
      val text = """
      1
      2
      3
      4
      5
    """

      val lines = text.lines().filter { it.trim().isNotBlank() }

      lines.toLineRanges("").size shouldBe 5

      lines.toLineRanges("1-5").also {
        it.size shouldBe 5
        it[0] shouldContain "1"
        it[1] shouldContain "2"
        it[2] shouldContain "3"
        it[3] shouldContain "4"
        it[4] shouldContain "5"
      }

      lines.toLineRanges("1,5").also {
        it.size shouldBe 2
        it[0] shouldContain "1"
        it[1] shouldContain "5"
      }

      lines.toLineRanges("1,3, 5").also {
        it.size shouldBe 3
        it[0] shouldContain "1"
        it[1] shouldContain "3"
        it[2] shouldContain "5"
      }
    }

    "fromTo treats begin/end tokens as literal substrings, not regex patterns" {
      // Regression: tokens containing regex metacharacters must match literally and must never
      // throw PatternSyntaxException (e.g. "items[0]", "foo(x)").
      listOf("before", "tag items[0]", "kept", "end foo(x)", "after")
        .fromTo(beginToken = "items[0]", endToken = "foo(x)") shouldBe listOf("kept")

      // A literal token absent from the text reports "not found" rather than matching via regex
      // semantics — a raw regex `a.c` would have matched "abc".
      shouldThrowExactly<IllegalArgumentException> {
        listOf("abc", "body").fromTo(beginToken = "a.c")
      }
    }

    "mkdir creates nested directories" {
      val base = java.nio.file.Files
        .createTempDirectory("kslides-mkdir")
        .toFile()
      try {
        val nested = java.io.File(base, "a/b/c").path
        InternalUtils.mkdir(nested) shouldBe true
        java.io.File(nested).isDirectory shouldBe true
      } finally {
        base.deleteRecursively()
      }
    }

    "fixIndents prepends the indent token to every line" {
      listOf("a", "b").fixIndents(indentToken = ">>", trimIndent = false, escapeHtml = false) shouldBe ">>a\n>>b"
    }

    "fixIndents trims common indentation when trimIndent is true" {
      listOf("    a", "    b").fixIndents(indentToken = "", trimIndent = true, escapeHtml = false) shouldBe "a\nb"
    }

    "fixIndents HTML-escapes each line when escapeHtml is true" {
      listOf("<b>x</b>").fixIndents(indentToken = "", trimIndent = false, escapeHtml = true) shouldBe
        "&lt;b&gt;x&lt;/b&gt;"
    }

    "indentInclude replaces the indent token, re-indenting to the marker column" {
      "@@code".indentInclude("@@") shouldBe "code"
      "    @@code".indentInclude("@@") shouldBe "    code"
    }

    "indentInclude carries the first marker's indent to subsequent marked lines" {
      "  @@first\n@@second\nplain".indentInclude("@@") shouldBe "  first\n  second\nplain"
    }

    "URL Prefix Test" {
      " HTTP://".isUrl() shouldBe true
      " http://".isUrl() shouldBe true
      " HTTPS://".isUrl() shouldBe true
      " https://".isUrl() shouldBe true
      " docs/something".isUrl() shouldBe false
      "".isUrl() shouldBe false
      " ".isUrl() shouldBe false
      "http://0.0.0.0:8080/playground-file".isUrl() shouldBe true
    }

    "stripBraces Test" {
      " [123] ".stripBraces() shouldBe "123"
      "[123]".stripBraces() shouldBe "123"
      "123".stripBraces() shouldBe "123"
    }
  }
}
