package com.kslides

import com.kslides.Include.logger
import kotlinx.html.*
import mu.*
import org.apache.commons.text.StringEscapeUtils.escapeHtml4
import java.io.*
import java.net.*

const val INDENT_TOKEN = "__indent__"

object Include : KLogging()

private fun String.pad() = "\n$this\n"

// Used within htmlSlide and markdownSlide
fun include(
  src: String,
  linePattern: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  trimIndent: Boolean = true,
  indentToken: String = INDENT_TOKEN,
  escapeHtml: Boolean = true,
) =
  try {
    if (src.isUrl()) {
      URL(src)
        .readText()
        .lines()
        .fromTo(beginToken, endToken, exclusive)
        .toLineRanges(linePattern)
        .fixIndents(indentToken, trimIndent, escapeHtml)
    } else {
      // Do not let queries wander outside of repo
      if (src.contains("../")) throw IllegalArgumentException("Invalid filename: $src")
      File("${System.getProperty("user.dir")}/$src")
        .readLines()
        .fromTo(beginToken, endToken, exclusive)
        .toLineRanges(linePattern)
        .fixIndents(indentToken, trimIndent, escapeHtml)
    }
  } catch (e: Exception) {
    logger.warn(e) { "Unable to read ${if (src.isUrl()) "url" else "file"} $src" }
    ""
  }

// When called from a code block, turn off indentToken and escaping
fun CODE.include(
  path: String,
  linePattern: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  trimIndent: Boolean = true,
) = include(path, linePattern, beginToken, endToken, exclusive, trimIndent, "", false).pad()

// When called from a DslSlide, turn off indentToken and escaping
fun DslSlide.include(
  path: String,
  linePattern: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  trimIndent: Boolean = true,
) = include(path, linePattern, beginToken, endToken, exclusive, trimIndent, "", false).pad()

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