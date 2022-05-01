package com.kslides

import kotlinx.html.*
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
  enableTrimIndent: Boolean = true,
  enableEscape: Boolean = true,
) =
  try {
    File("${System.getProperty("user.dir")}/$path")
      .readLines()
      .fromTo(beginToken, endToken, exclusive)
      .toLineRanges(lineNumbers)
      .fixIndents(indentToken, enableTrimIndent, enableEscape)
  } catch (e: Exception) {
    Include.logger.warn(e) { "Unable to read $path" }
    ""
  }

// When called from a dslSlide, you no longer need the indentToken and you want to add newlines to front and back
fun CODE.includeFile(
  path: String,
  lineNumbers: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  indentToken: String = "",
  enableTrimIndent: Boolean = true,
  enableEscape: Boolean = true,
) =
  "\n${
    com.kslides.includeFile(
      path,
      lineNumbers,
      beginToken,
      endToken,
      exclusive,
      indentToken,
      enableTrimIndent,
      enableEscape
    )
  }\n"

fun includeUrl(
  url: String,
  lineNumbers: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  indentToken: String = INDENT_TOKEN,
  enableTrimIndent: Boolean = true,
  enableEscape: Boolean = true,
) =
  try {
    URL(url)
      .readText()
      .lines()
      .fromTo(beginToken, endToken, exclusive)
      .toLineRanges(lineNumbers)
      .fixIndents(indentToken, enableTrimIndent, enableEscape)
  } catch (e: Exception) {
    Include.logger.warn(e) { "Unable to read $url" }
    ""
  }

fun CODE.includeUrl(
  url: String,
  lineNumbers: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  indentToken: String = "",
  enableTrimIndent: Boolean = true,
  enableEscape: Boolean = true,
) =
  "\n${
    com.kslides.includeUrl(
      url,
      lineNumbers,
      beginToken,
      endToken,
      exclusive,
      indentToken,
      enableTrimIndent,
      enableEscape
    )
  }\n"

internal fun List<String>.fromTo(
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true
): List<String> {
  val beginIndex =
    if (beginToken.isNotBlank()) {
      // Do not match calling token in the same file
      val unquotedBegin = Regex(beginToken)
      val quotedBegin = Regex("$beginToken\"")
      (this
        .asSequence()
        .mapIndexed { i, s -> i to s }
        .firstOrNull { it.second.contains(unquotedBegin) && !it.second.contains(quotedBegin) }?.first
        ?: throw IllegalArgumentException("Begin token not found: $beginToken")) + (if (exclusive) 1 else 0)
    } else {
      0
    }

  val endIndex =
    if (endToken.isNotBlank()) {
      // Do not match calling token in the same file
      val unquotedEnd = Regex(endToken)
      val quotedEnd = Regex("$endToken\"")
      (this
        .reversed()
        .asSequence()
        .mapIndexed { i, s -> (this.size - i - (if (exclusive) 1 else 0)) to s }
        .firstOrNull { it.second.contains(unquotedEnd) && !it.second.contains(quotedEnd) }?.first
        ?: throw IllegalArgumentException("End token not found: $endToken"))
    } else {
      this.size
    }

  return if (beginIndex == 0 && endIndex == this.size) this else subList(beginIndex, endIndex)
}

internal fun List<String>.toLineRanges(text: String): List<String> =
  when {
    text.isNotBlank() ->
      text.toIntList().let { lineNums ->
        filterIndexed { i, _ -> i + 1 in lineNums }
      }
    else -> this
  }

internal fun List<String>.fixIndents(
  indentToken: String,
  enableTrimIndent: Boolean,
  enableEscape: Boolean,
) =
  (if (enableTrimIndent) joinToString("\n").trimIndent().lines() else this)
    .map { "$indentToken$it" }
    .joinToString("\n") { if (enableEscape) escapeHtml4(it) else it }
