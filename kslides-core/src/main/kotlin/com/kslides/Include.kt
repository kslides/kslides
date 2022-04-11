package com.kslides

import mu.*
import org.apache.commons.text.StringEscapeUtils.escapeHtml4
import java.io.*
import java.net.*

const val INDENT_TOKEN = "__indent__"

object Include : KLogging()

fun includeFile(
  path: String,
  lineNumbers: String = "",
  beginToken: String = "",
  endToken: String = "",
  indentToken: String = INDENT_TOKEN,
  commentPrefix: String = "//",
  enableEscape: Boolean = true,
  enableTrimIndent: Boolean = true,
) =
  try {
    processCode(
      File("${System.getProperty("user.dir")}/$path").readLines(),
      lineNumbers,
      beginToken,
      endToken,
      indentToken,
      commentPrefix,
      enableEscape,
      enableTrimIndent,
    )
  } catch (e: Exception) {
    Include.logger.warn(e) { "Unable to read $path" }
    ""
  }

fun includeUrl(
  url: String,
  lineNumbers: String = "",
  beginToken: String = "",
  endToken: String = "",
  indentToken: String = INDENT_TOKEN,
  commentPrefix: String = "//",
  enableEscape: Boolean = true,
  enableTrimIndent: Boolean = true,
) =
  try {
    processCode(
      URL(url).readText().lines(),
      lineNumbers,
      beginToken,
      endToken,
      indentToken,
      commentPrefix,
      enableEscape,
      enableTrimIndent,
    )
  } catch (e: Exception) {
    Include.logger.warn(e) { "Unable to read $url" }
    ""
  }

private fun processCode(
  lines: List<String>,
  lineNumbers: String,
  beginToken: String,
  endToken: String,
  indentToken: String,
  commentPrefix: String,
  enableEscape: Boolean,
  enableTrimIndent: Boolean,
): String {
  val startIndex =
    if (beginToken.isBlank())
      0
    else {
      val beginRegex = "$commentPrefix\\s*$beginToken".toRegex()
      (lines
        .asSequence()
        .mapIndexed { i, s -> i to s }
        .firstOrNull { it.second.contains(beginRegex) }?.first
        ?: throw IllegalArgumentException("beginToken not found: $commentPrefix $beginToken")) + 1
    }

  val endIndex =
    if (endToken.isBlank())
      lines.size
    else {
      val endRegex = "$commentPrefix\\s*$endToken".toRegex()
      (lines
        .reversed()
        .asSequence()
        .mapIndexed { i, s -> (lines.size - i - 1) to s }
        .firstOrNull { it.second.contains(endRegex) }?.first
        ?: throw IllegalArgumentException("endToken not found: $commentPrefix $endToken"))
    }

  val begEndLines = if (beginToken.isBlank() && endToken.isBlank()) lines else lines.subList(startIndex, endIndex)

  val rangeLines =
    if (lineNumbers.isNotBlank()) {
      val lineNums = lineNumbers.toIntList()
      begEndLines.filterIndexed { i, _ -> i + 1 in lineNums }
    } else {
      begEndLines
    }

  return (if (enableTrimIndent) rangeLines.joinToString("\n").trimIndent().lines() else rangeLines)
    .map { "$indentToken$it" }
    .joinToString("\n") { if (enableEscape) escapeHtml4(it) else it }
}
