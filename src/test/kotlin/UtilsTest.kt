package com.github.readingbat

import com.kslides.*
import io.kotest.core.spec.style.*
import io.kotest.matchers.*

class UtilsTest : StringSpec(
  {
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
  }
)