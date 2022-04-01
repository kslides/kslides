package com.kslides

import mu.*
import org.apache.commons.text.*
import java.io.*
import java.net.*

const val INDENT_TOKEN = "__indent__"

object Include : KLogging() {
  fun includeFile(
    path: String,
    lineNumbers: String = "",
    beginToken: String = "",
    endToken: String = "",
    indentToken: String = INDENT_TOKEN,
    commentPrefix: String = "//"
  ) =
    try {
      processCode(
        File("${System.getProperty("user.dir")}/$path").readLines(),
        lineNumbers,
        beginToken,
        endToken,
        indentToken,
        commentPrefix,
      )
    } catch (e: Exception) {
      logger.warn(e) { "Unable to read $path" }
      ""
    }

  fun includeUrl(
    url: String,
    lineNumbers: String = "",
    beginToken: String = "",
    endToken: String = "",
    indentToken: String = INDENT_TOKEN,
    commentPrefix: String = "//"
  ) =
    try {
      processCode(
        URL(url).readText().lines(),
        lineNumbers,
        beginToken,
        endToken,
        indentToken,
        commentPrefix,
      )
    } catch (e: Exception) {
      logger.warn(e) { "Unable to read $url" }
      ""
    }

  private fun processCode(
    lines: List<String>,
    lineNumbers: String = "",
    beginToken: String,
    endToken: String,
    indentToken: String,
    commentPrefix: String,
  ): String {

    val subLines =
      if (lineNumbers.isNotEmpty()) {
        val lineNums = lineNumbers.toIntList()
        lines.filterIndexed { i, _ -> i + 1 in lineNums }
      } else {
        val startIndex =
          if (beginToken.isEmpty())
            0
          else {
            val beginRegex = "$commentPrefix\\s*$beginToken".toRegex()
            (lines
              .asSequence()
              .mapIndexed { i, s -> i to s }
              .firstOrNull { it.second.contains(beginRegex) }?.first
              ?: throw IllegalArgumentException("beginToken not found: $beginToken")) + 1
          }

        val endIndex =
          if (endToken.isEmpty())
            lines.size
          else {
            val endRegex = "$commentPrefix\\s*$endToken".toRegex()
            (lines.reversed()
              .asSequence()
              .mapIndexed { i, s -> (lines.size - i - 1) to s }
              .firstOrNull { it.second.contains(endRegex) }?.first
              ?: throw IllegalArgumentException("endToken not found: $endToken"))
          }

        lines.subList(startIndex, endIndex)
      }

    return subLines.map { "$indentToken$it" }.joinToString("\n") { StringEscapeUtils.escapeHtml4(it) }
  }
}