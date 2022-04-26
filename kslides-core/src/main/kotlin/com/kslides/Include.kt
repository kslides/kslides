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
  exclusive: Boolean = true,
  indentToken: String = INDENT_TOKEN,
  enableEscape: Boolean = true,
  enableTrimIndent: Boolean = true,
) =
  try {
    val file = File("${System.getProperty("user.dir")}/$path")
    file
      .readLines()
      .fromTo(beginToken, endToken, exclusive)
      .lineNumbers(lineNumbers)
      .fixIndents(indentToken, enableEscape, enableTrimIndent)
  } catch (e: Exception) {
    Include.logger.warn(e) { "Unable to read $path" }
    ""
  }

fun includeUrl(
  url: String,
  lineNumbers: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  indentToken: String = INDENT_TOKEN,
  enableEscape: Boolean = true,
  enableTrimIndent: Boolean = true,
) =
  try {
    URL(url)
      .readText()
      .lines()
      .fromTo(beginToken, endToken, exclusive)
      .lineNumbers(lineNumbers)
      .fixIndents(indentToken, enableEscape, enableTrimIndent)
  } catch (e: Exception) {
    Include.logger.warn(e) { "Unable to read $url" }
    ""
  }

internal fun List<String>.fromTo(
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true
): List<String> {
  val beginIndex =
    if (beginToken.isNotBlank())
      (this
        .asSequence()
        .mapIndexed { i, s -> i to s }
        .firstOrNull { it.second.contains(beginToken) }?.first
        ?: throw IllegalArgumentException("Begin token not found: $beginToken")) + (if (exclusive) 1 else 0)
    else
      0

  val endIndex =
    if (endToken.isNotBlank())
      (this
        .reversed()
        .asSequence()
        .mapIndexed { i, s -> (this.size - i - (if (exclusive) 1 else 0)) to s }
        .firstOrNull { it.second.contains(endToken) }?.first
        ?: throw IllegalArgumentException("End token not found: $endToken"))
    else
      this.size

  return if (beginToken.isNotBlank() || endToken.isNotBlank()) this.subList(beginIndex, endIndex) else this
}

internal fun List<String>.lineNumbers(lineNumbers: String) =
  if (lineNumbers.isNotBlank()) {
    val lineNums = lineNumbers.toIntList()
    filterIndexed { i, _ -> i + 1 in lineNums }
  } else {
    this
  }

internal fun List<String>.fixIndents(
  indentToken: String,
  enableEscape: Boolean,
  enableTrimIndent: Boolean,
) =
  (if (enableTrimIndent) joinToString("\n").trimIndent().lines() else this)
    .map { "$indentToken$it" }
    .joinToString("\n") { if (enableEscape) escapeHtml4(it) else it }
