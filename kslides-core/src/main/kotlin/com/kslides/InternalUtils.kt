package com.kslides

import com.kslides.slide.*
import mu.*
import org.apache.commons.text.*
import java.io.*

object InternalUtils : KLogging() {
  internal val whiteSpace = "\\s".toRegex()

  internal fun <K, V> Map<K, V>.merge(other: Map<K, V>) =
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
        linePattern.toIntList().let { lineNums -> filterIndexed { i, _ -> i + 1 in lineNums } }
      else -> this
    }

  internal fun List<String>.fixIndents(
    indentToken: String,
    trimIndent: Boolean,
    escapeHtml: Boolean,
  ) =
    (if (trimIndent) joinToString("\n").trimIndent().lines() else this)
      .map { "$indentToken$it" }
      .joinToString("\n") { if (escapeHtml) StringEscapeUtils.escapeHtml4(it) else it }

  internal fun writeContentFile(path: String, slide: DslSlide, content: String) {
    mkdir(path)    // Create directory if missing

    "$path${slide._slideFilename}"
      .also {
        logger.info { "Writing content to: $it" }
        File(it).writeText(content)
      }
  }

  internal fun mkdir(name: String) = File(name).run { if (!exists()) mkdir() else false }

  private val httpRegex = Regex("\\s*http[s]?://.*")

  internal fun String.isUrl() = lowercase().matches(httpRegex)

  internal fun String.stripBraces() = trimStart().trimEnd().trimStart('[', '(').trimEnd(']', ')')

  internal fun String.pad() = "\n$this\n"
}
