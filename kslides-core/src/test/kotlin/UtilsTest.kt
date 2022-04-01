package com.github.readingbat

import com.kslides.*
import io.kotest.core.spec.style.*
import io.kotest.matchers.*

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
          if (it.contains("NO TAB")) {
            it.trimStart().length shouldBe it.length
          } else {
            if (it.isNotEmpty())
              it.trimStart().length shouldNotBe it.length
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
      p3.enableMenu shouldBe true
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
  }
)