package com.kslides

import org.apache.commons.text.*
import java.io.*
import java.net.*

const val INDENT_TOKEN = "__indent__"

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
    e.printStackTrace()
    ""
  }

fun includeUrl(
  source: String,
  lineNumbers: String = "",
  beginToken: String = "",
  endToken: String = "",
  indentToken: String = INDENT_TOKEN,
  commentPrefix: String = "//"
) =
  try {
    processCode(
      URL(source).readText().lines(),
      lineNumbers,
      beginToken,
      endToken,
      indentToken,
      commentPrefix,
    )
  } catch (e: Exception) {
    e.printStackTrace()
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

  val prefixed = subLines.map { "$indentToken$it" }
  return prefixed.joinToString("\n") { StringEscapeUtils.escapeHtml4(it) }
}
