package com.kslides

import java.util.*

fun String.toLower(locale: Locale = Locale.getDefault()) = lowercase(locale)

fun slideBackground(color: String) = "<!-- .slide: data-background=\"$color\" -->"

fun fragmentIndex(index: Int) = "<!-- .element: class=\"fragment\" data-fragment-index=\"$index\" -->"

fun <K, V> Map<K, V>.merge(other: Map<K, V>) =
  mutableMapOf<K, V>()
    .also { result ->
      result.putAll(this)
      other.forEach { (key, value) -> result[key] = value }
    }

internal fun String.indentInclude(indentToken: String): String {
  var firstLineFound = false
  var firstLineIndent = ""
  return lines()
    .joinToString("\n") { str ->
      if (!firstLineFound) {
        val trimmed = str.trimStart()
        if (trimmed.startsWith(indentToken)) {
          firstLineFound = true
          firstLineIndent = str.substring(0, str.indexOf(indentToken))
          firstLineIndent + trimmed.substring(indentToken.length)
        } else {
          str
        }
      } else {
        if (str.startsWith(indentToken)) {
          firstLineIndent + str.substring(indentToken.length)
        } else {
          firstLineFound = false
          firstLineIndent = ""
          str
        }
      }
    }
}

internal fun String.indentFirstLine(indentToken: String): String {
  var firstLineFound = false
  return lines()
    .joinToString("\n") { str ->
      if (!firstLineFound) {
        val trimmed = str.trimStart()
        if (trimmed.startsWith(indentToken)) {
          firstLineFound = true
          trimmed.substring(indentToken.length)
        } else {
          str
        }
      } else {
        if (str.startsWith(indentToken)) {
          str.substring(indentToken.length)
        } else {
          firstLineFound = false
          str
        }
      }
    }
}

internal fun String.trimIndentWithInclude(): String {
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