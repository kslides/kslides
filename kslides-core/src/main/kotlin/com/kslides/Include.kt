package com.kslides

import kotlinx.html.*
import mu.*
import org.apache.commons.text.StringEscapeUtils.escapeHtml4
import java.io.*
import java.net.*

const val INDENT_TOKEN = "__indent__"

object Include : KLogging()

private fun String.pad() = "\n$this\n"

// This will cover htmlSlide and markdownSlide
fun includeFile(
  path: String,
  linePattern: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  trimIndent: Boolean = true,
  indentToken: String = INDENT_TOKEN,
  escapeHtml: Boolean = true,
) =
  try {
    File("${System.getProperty("user.dir")}/$path")
      .readLines()
      .fromTo(beginToken, endToken, exclusive)
      .toLineRanges(linePattern)
      .fixIndents(indentToken, trimIndent, escapeHtml)
  } catch (e: Exception) {
    Include.logger.warn(e) { "Unable to read $path" }
    ""
  }

// When called from a code block, turn off indentToken and escaping
fun CODE.includeFile(
  path: String,
  linePattern: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  trimIndent: Boolean = true,
) = includeFile(path, linePattern, beginToken, endToken, exclusive, false, "", false).pad()

// When called from a DslSlide, turn off indentToken and escaping
fun DslSlide.includeFile(
  path: String,
  linePattern: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  trimIndent: Boolean = true,
) = includeFile(path, linePattern, beginToken, endToken, exclusive, trimIndent, "", false).pad()

// When called from a vertical DslSlide, turn off indentToken and escaping
fun VerticalDslSlide.includeFile(
  path: String,
  linePattern: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  trimIndent: Boolean = true,
) = includeFile(path, linePattern, beginToken, endToken, exclusive, trimIndent, "", false).pad()

// This will cover htmlSlide and markdownSlide
fun includeUrl(
  url: String,
  linePattern: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  trimIndent: Boolean = true,
  indentToken: String = INDENT_TOKEN,
  escapeHtml: Boolean = true,
) =
  try {
    URL(url)
      .readText()
      .lines()
      .fromTo(beginToken, endToken, exclusive)
      .toLineRanges(linePattern)
      .fixIndents(indentToken, trimIndent, escapeHtml)
  } catch (e: Exception) {
    Include.logger.warn(e) { "Unable to read $url" }
    ""
  }

// When called from a code block, turn off indentToken and escaping
fun CODE.includeUrl(
  url: String,
  linePattern: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  trimIndent: Boolean = true,
) = includeUrl(url, linePattern, beginToken, endToken, exclusive, trimIndent, "", false).pad()

// When called from a DslSlide, turn off indentToken and escaping
fun DslSlide.includeUrl(
  url: String,
  linePattern: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  trimIndent: Boolean = true,
) = includeUrl(url, linePattern, beginToken, endToken, exclusive, trimIndent, "", false).pad()

// When called from a vertical DslSlide, turn off indentToken and escaping
fun VerticalDslSlide.includeUrl(
  url: String,
  linePattern: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  trimIndent: Boolean = true,
) = includeUrl(url, linePattern, beginToken, endToken, exclusive, trimIndent, "", false).pad()

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

internal fun List<String>.toLineRanges(linePattern: String): List<String> =
  when {
    linePattern.isNotBlank() ->
      linePattern.toIntList().let { lineNums ->
        filterIndexed { i, _ -> i + 1 in lineNums }
      }
    else -> this
  }

internal fun List<String>.fixIndents(
  indentToken: String,
  trimIndent: Boolean,
  escapeHtml: Boolean,
) =
  (if (trimIndent) joinToString("\n").trimIndent().lines() else this)
    .map { "$indentToken$it" }
    .joinToString("\n") { if (escapeHtml) escapeHtml4(it) else it }