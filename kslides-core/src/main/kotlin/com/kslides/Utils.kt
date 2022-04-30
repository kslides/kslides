package com.kslides

import kotlinx.html.*

fun githubSourceUrl(username: String, repoName: String, path: String = "", branchName: String = "master") =
  "https://github.com/$username/$repoName/blob/$branchName/$path"

fun githubRawUrl(username: String, repoName: String, path: String = "", branchName: String = "master") =
  "https://raw.githubusercontent.com/$username/$repoName/$branchName/$path"

fun slideBackground(color: String) = "<!-- .slide: data-background=\"$color\" -->"

fun fragment(effect: Effect = Effect.NONE, index: Int = 0) =
  "<!-- .element: ${effect.toOutput()}${if (index > 0) " data-fragment-index=\"$index\"" else ""} -->"

fun HTMLTag.rawHtml(html: String) = unsafe { raw(html) }

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

private val whiteSpace = "\\s".toRegex()

internal fun String.toIntList() =
  buildList {
    replace(whiteSpace, "")
      .trimStart('[', '(')
      .trimEnd(']', ')')
      .split(",", ";")
      .filter { it.isNotBlank() }
      .forEach { splitElem ->
        splitElem.split('-', '–', ':')
          .also { elem ->
            when (elem.size) {
              1 -> add(splitElem.toInt())
              2 -> {
                elem.let { it[0].toInt() to it[1].toInt() }
                  .also { (beg, end) ->
                    when {
                      beg == end -> add(beg)
                      beg < end -> addAll(beg..end)
                      else -> addAll((beg downTo end))
                    }
                  }
              }
              else -> throw IllegalArgumentException("Invalid argument: $elem")
            }
          }
      }
  }

fun lineNumbers(ranges: String) =
  ranges.replace(whiteSpace, "")
    .trimStart('[', '(')
    .trimEnd(']', ')')
    .split("|")
    .map { if (it == "*") "" else it }

fun DIV.unorderedList(vararg items: String, block: UL.() -> Unit = {}) {
  val funcs: List<LI.() -> Unit> = items.map { { +it } }
  unorderedList(*funcs.toTypedArray(), block = block)
}

fun DIV.orderedList(vararg items: String, block: OL.() -> Unit = {}) {
  val funcs: List<LI.() -> Unit> = items.map { { +it } }
  orderedList(*funcs.toTypedArray(), block = block)
}

fun DIV.unorderedList(vararg items: LI.() -> Unit, block: UL.() -> Unit = {}) =
  ul {
    block()
    items.forEach {
      li { it() }
    }
  }

fun DIV.orderedList(vararg items: LI.() -> Unit, block: OL.() -> Unit = {}) =
  ol {
    block()
    items.forEach {
      li { it() }
    }
  }

fun LI.listHref(url: String, block: A.() -> Unit = {}) {
  a {
    block()
    href = url; +url
  }
}