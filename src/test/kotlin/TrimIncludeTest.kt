package com.github.readingbat

import com.kslides.*
import io.kotest.core.spec.style.*
import io.kotest.matchers.*


class TrimIncludeTest : StringSpec(
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
          if (it.contains("NO TAB"))
            it.trimStart().length shouldBe it.length
          else
            if (it.isNotEmpty())
              it.trimStart().length shouldNotBe it.length
        }
    }
  }
)