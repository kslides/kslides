package com.kslides

import org.apache.commons.text.StringEscapeUtils.escapeHtml4
import java.io.*
import java.net.*
import java.util.*

fun slideBackground(color: String) = "<!-- .slide: data-background=\"$color\" -->"

fun fragmentIndex(index: Int) = "<!-- .element: class=\"fragment\" data-fragment-index=\"$index\" -->"

fun includeFile(
  path: String,
  beginToken: String = "",
  endToken: String = "",
  commentPrefix: String = "//"
) =
  try {
    processCode(
      File("${System.getProperty("user.dir")}/$path").readLines(),
      beginToken,
      endToken,
      commentPrefix
    )
  } catch (e: Exception) {
    e.printStackTrace()
    ""
  }

fun includeUrl(
  source: String,
  beginToken: String = "",
  endToken: String = "",
  commentPrefix: String = "//"
) =
  try {
    processCode(
      URL(source).readText().lines(),
      beginToken,
      endToken,
      commentPrefix
    )
  } catch (e: Exception) {
    e.printStackTrace()
    ""
  }

private fun processCode(
  lines: List<String>,
  beginToken: String,
  endToken: String,
  commentPrefix: String
): String {
  val beginRegex = "$commentPrefix\\s*$beginToken".toRegex()
  val endRegex = "$commentPrefix\\s*$endToken".toRegex()

  val startIndex =
    if (beginToken.isEmpty())
      0
    else
      (lines
        .asSequence()
        .mapIndexed { i, s -> i to s }
        .firstOrNull { it.second.contains(beginRegex) }?.first
        ?: throw IllegalArgumentException("beginToken not found: $beginToken")) + 1

  val endIndex =
    if (endToken.isEmpty())
      lines.size
    else
      (lines.reversed()
        .asSequence()
        .mapIndexed { i, s -> (lines.size - i - 1) to s }
        .firstOrNull { it.second.contains(endRegex) }?.first
        ?: throw IllegalArgumentException("endToken not found: $endToken"))

  return lines.subList(startIndex, endIndex).joinToString("\n") { escapeHtml4(it) }
}

fun String.toLower(locale: Locale = Locale.getDefault()) = lowercase(locale)

fun <K, V> Map<K, V>.merge(other: Map<K, V>) =
  mutableMapOf<K, V>()
    .also { result ->
      result.putAll(this)
      other.forEach { (key, value) -> result[key] = value }
    }

fun String.trimIndentWithInclude(): String {
  var insideFence = false
  var fence = ""
  var fenceLine = -1

  return lines()
    .asSequence()
    .mapIndexed { i, str ->
      val trimmed = str.trimStart()
      if (insideFence) {
        when {
          trimmed.startsWith(fence) -> {
            insideFence = false
            fenceLine = -1
            trimmed
          }
          fenceLine != -1 && str.isNotBlank() -> {
            fenceLine = -1
            trimmed
          }
          else -> str
        }
      } else {
        val fenceLength = trimmed.length - trimmed.trimStart('`', '~').length
        if (fenceLength > 0) {
          insideFence = true
          fenceLine = i
          fence = trimmed.substring(0, fenceLength)
        }
        trimmed
      }
    }
    .joinToString("\n")
}