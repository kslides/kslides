package com.kslides

import com.kslides.InternalUtils.fixIndents
import com.kslides.InternalUtils.fromTo
import com.kslides.InternalUtils.isUrl
import com.kslides.InternalUtils.pad
import com.kslides.InternalUtils.toLineRanges
import com.kslides.InternalUtils.whiteSpace
import com.kslides.Playground.logger
import com.kslides.Utils.INDENT_TOKEN
import kotlinx.html.*
import mu.*
import java.io.*
import java.net.*

object Utils : KLogging() {
  internal const val INDENT_TOKEN = "--indent--"
}

fun slideBackground(color: String) = "<!-- .slide: data-background=\"$color\" -->"

fun fragment(effect: Effect = Effect.NONE, index: Int = 0) =
  "<!-- .element: ${effect.toOutput()}${if (index > 0) " data-fragment-index=\"$index\"" else ""} -->"

fun HTMLTag.rawHtml(html: String) = unsafe { raw(html) }

fun <T> List<T>.permuteBy(vararg orders: List<Int>): Sequence<List<T>> =
  sequence {
    orders
      .forEach { order ->
        yield(
          buildList {
            order.forEach { this@buildList += this@permuteBy[it] }
          })
      }
  }

fun String.toLinePatterns() =
  replace(whiteSpace, "")
    .trimStart('[', '(')
    .trimEnd(']', ')')
    .split("|")
    .map { if (it == "*") "" else it }

fun githubSourceUrl(username: String, repoName: String, path: String = "", branchName: String = "master") =
  "https://github.com/$username/$repoName/blob/$branchName/$path"

fun githubRawUrl(username: String, repoName: String, path: String = "", branchName: String = "master") =
  "https://raw.githubusercontent.com/$username/$repoName/$branchName/$path"

// Used within htmlSlide{} and markdownSlide{} blocks
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
      if (src.contains("../")) throw IllegalArgumentException("Illegal filename: $src")
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
  src: String,
  linePattern: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  trimIndent: Boolean = true,
) = include(src, linePattern, beginToken, endToken, exclusive, trimIndent, "", false).pad()

// When called from a DslSlide, turn off indentToken and escaping
fun DslSlide.include(
  src: String,
  linePattern: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  trimIndent: Boolean = true,
) = include(src, linePattern, beginToken, endToken, exclusive, trimIndent, "", false).pad()