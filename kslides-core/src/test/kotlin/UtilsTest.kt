package com.github.readingbat

import com.kslides.*
import io.kotest.core.spec.style.*
import io.kotest.matchers.*
import io.kotest.matchers.string.*

class UtilsTest : StringSpec(
  {
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

    "Code fence test" {
      val s =
        """
        # Presentation // NO TAB
  
        ````kotlin     // NO TAB
           val x = 1   // WITH TAB
        ````           // NO TAB
        """

      s.trimIndent()
        .lines()
        .forEach {
          if (it.contains("NO TAB"))
            it.trimStart().length shouldBe it.length

          if (it.contains("WITH TAB"))
            it.trimStart().length shouldNotBe it.length
        }
    }

    "Code fence test with include" {
      val s =
        """
        # Presentation // NO TAB
  
        ````kotlin     // NO TAB
        val x = 1      // NO TAB
val y = 1              // NO TAB
        ````           // NO TAB
        """

      s.trimIndentWithInclude()
        .lines()
        .forEach {
          if (it.contains("NO TAB"))
            it.trimStart().length shouldBe it.length
          else {
            if (it.isNotEmpty()) it.trimStart().length shouldNotBe it.length
          }
        }
    }

    "Map merge test 1" {
      val m1 = mapOf("a" to 1, "b" to 2)
      val m2 = mapOf("c" to 3, "d" to 4)
      val m3 = mapOf("a" to 1, "b" to 2, "c" to 3, "d" to 4)
      m1.merge(m2) shouldBe m3
    }

    "Map merge test 2" {
      val m1 = mapOf("a" to 1, "b" to 2)
      val m2 = mapOf("b" to 3, "c" to 4)
      val m3 = mapOf("a" to 1, "b" to 3, "c" to 4)
      m1.merge(m2) shouldBe m3
    }

    "Map merge test 3" {
      val m1: Map<String, Int> = mapOf()
      val m2 = mapOf("b" to 3, "c" to 4)
      val m3 = mapOf("b" to 3, "c" to 4)
      m1.merge(m2) shouldBe m3
    }

    "Map merge test 4" {
      val m1 = mapOf("b" to 3, "c" to 4)
      val m2: Map<String, Int> = mapOf()
      val m3 = mapOf("b" to 3, "c" to 4)
      m1.merge(m2) shouldBe m3
    }

    "Map merge test 5" {
      val m1 = mapOf("a" to 1, "b" to 2)
      val m2 = mapOf("a" to 4, "b" to 5)
      val m3 = mapOf("a" to 4, "b" to 5)
      m1.merge(m2) shouldBe m3
    }

    "PresentationConfig Test 1" {
      val p1 = PresentationConfig(true)
      val p2 = PresentationConfig()
      val p3 =
        PresentationConfig()
          .apply {
            merge(p1)
            merge(p2)
          }
      p3.enableMenu shouldBe false
    }

    "PresentationConfig Test 2" {
      val p1 = PresentationConfig(true).apply {
        enableMenu = false
      }
      val p2 = PresentationConfig()
      val p3 =
        PresentationConfig()
          .apply {
            merge(p1)
            merge(p2)
          }
      p3.enableMenu shouldBe false
    }

    "PresentationConfig Test 3" {
      val p1 = PresentationConfig(true).apply {
        enableMenu = false
      }
      val p2 = PresentationConfig().apply {
        enableMenu = true
      }
      val p3 =
        PresentationConfig()
          .apply {
            merge(p1)
            merge(p2)
          }
      p3.enableMenu shouldBe true
    }

    "PresentationConfig Test 4" {
      val p1 = PresentationConfig(true).apply {
        enableMenu = true
      }
      val p2 = PresentationConfig().apply {
        enableMenu = false
      }
      val p3 =
        PresentationConfig()
          .apply {
            merge(p1)
            merge(p2)
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

      val lines = text.lines().filter { it.trim().isNotEmpty() }

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

    // This tests for the case where you are matching for a token in the same file and you want to avoid matching the invocation
    "From/To Test 2" {
      val text = """
        "1"
        1
        2
        3
        "3"
      """

      val lines = text.lines().filter { it.trim().isNotEmpty() }

      lines.fromTo("1", "3").also {
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
            includeFile(slides, beginToken = "image begin", endToken = "image end")
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

      val lines = text.lines().filter { it.trim().isNotEmpty() }

      lines.lineNumbers("").size shouldBe 5

      lines.lineNumbers("1-5").also {
        it.size shouldBe 5
        it[0] shouldContain "1"
        it[1] shouldContain "2"
        it[2] shouldContain "3"
        it[3] shouldContain "4"
        it[4] shouldContain "5"
      }

      lines.lineNumbers("1,5").also {
        it.size shouldBe 2
        it[0] shouldContain "1"
        it[1] shouldContain "5"
      }

      lines.lineNumbers("1,3, 5").also {
        it.size shouldBe 3
        it[0] shouldContain "1"
        it[1] shouldContain "3"
        it[2] shouldContain "5"
      }
    }
  }
)