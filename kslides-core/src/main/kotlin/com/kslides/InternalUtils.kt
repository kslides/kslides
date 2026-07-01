package com.kslides

import org.apache.commons.text.StringEscapeUtils
import java.io.File

/**
 * Module-private utility helpers used across kslides-core: indentation handling for `include()`,
 * line-range parsing for code-snippet highlighting, file output, and small string predicates.
 * Implementation detail — not part of the public API.
 */
@Suppress("TooManyFunctions")
internal object InternalUtils {
  internal val whiteSpace = "\\s".toRegex()

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

  internal fun String.trimIndentWithInclude(): String {
    var insideFence = false
    var fence = ""
    var fenceLine = -1

    return lines()
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

            else -> {
              str
            }
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
      }.joinToString("\n")
  }

  /**
   * Parse a comma/semicolon-separated list of single line numbers and `a-b`/`a:b` ranges into the
   * expanded list of line numbers (e.g. `"1,3-5"` → `[1, 3, 4, 5]`; descending ranges count down).
   *
   * @throws IllegalArgumentException on any malformed element — a non-integer endpoint (`"-5"`,
   *   `"a"`) or more than two endpoints (`"1-2-3"`) — reporting the offending element uniformly
   *   rather than leaking a raw [NumberFormatException].
   */
  internal fun String.toIntList(): List<Int> =
    buildList {
      replace(whiteSpace, "")
        .trimStart('[', '(')
        .trimEnd(']', ')')
        .split(",", ";")
        .filter { it.isNotBlank() }
        .forEach { splitElem ->
          val elem = splitElem.split('-', '–', ':')
          try {
            when (elem.size) {
              1 -> {
                add(splitElem.toInt())
              }

              2 -> {
                val (beg, end) = elem[0].toInt() to elem[1].toInt()
                // beg..end already yields the single element [beg] when beg == end
                addAll(if (beg <= end) beg..end else beg downTo end)
              }

              else -> {
                throw IllegalArgumentException("Invalid line range: '$splitElem'")
              }
            }
          } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid line range: '$splitElem'", e)
          }
        }
    }

  internal fun List<String>.fromTo(
    beginToken: String = "",
    endToken: String = "",
    exclusive: Boolean = true,
  ): List<String> {
    val beginIndex =
      if (beginToken.isNotBlank()) {
        // Do not match calling token in the same file. Tokens are documented as plain substrings,
        // so escape them — a metacharacter (e.g. "items[0]") must not be compiled as a regex.
        val unquotedBegin = Regex(Regex.escape(beginToken))
        val quotedBegin = Regex(Regex.escape(beginToken) + "\"")
        (
          asSequence()
            .mapIndexed { i, s -> i to s }
            .firstOrNull { it.second.contains(unquotedBegin) && !it.second.contains(quotedBegin) }
            ?.first
            ?: throw IllegalArgumentException("Begin token not found: $beginToken")
          ) + (if (exclusive) 1 else 0)
      } else {
        0
      }

    val endIndex =
      if (endToken.isNotBlank()) {
        // Do not match calling token in the same file. Tokens are documented as plain substrings,
        // so escape them — a metacharacter (e.g. "items[0]") must not be compiled as a regex.
        val unquotedEnd = Regex(Regex.escape(endToken))
        val quotedEnd = Regex(Regex.escape(endToken) + "\"")
        (
          reversed()
            .asSequence()
            .mapIndexed { i, s -> (this.size - i - (if (exclusive) 1 else 0)) to s }
            .firstOrNull { it.second.contains(unquotedEnd) && !it.second.contains(quotedEnd) }
            ?.first
            ?: throw IllegalArgumentException("End token not found: $endToken")
          )
      } else {
        this.size
      }

    require(beginIndex <= endIndex) {
      "include token range is empty or inverted: begin token '$beginToken' (index $beginIndex) " +
        "occurs after end token '$endToken' (index $endIndex)"
    }
    return if (beginIndex == 0 && endIndex == this.size) this else subList(beginIndex, endIndex)
  }

  internal fun List<String>.toLineRanges(linePattern: String): List<String> =
    when {
      linePattern.isNotBlank() -> {
        linePattern.toIntList().let { lineNums -> filterIndexed { i, _ -> i + 1 in lineNums } }
      }

      else -> {
        this
      }
    }

  internal fun List<String>.fixIndents(
    indentToken: String,
    trimIndent: Boolean,
    escapeHtml: Boolean,
  ) = (
    if (trimIndent) joinToString("\n").trimIndent().lines() else this
    ).map { "$indentToken$it" }
    .joinToString("\n") { if (escapeHtml) StringEscapeUtils.escapeHtml4(it) else it }

  internal fun writeString(
    path: String,
    slideName: String,
    content: String,
  ) {
    mkdir(path)    // Create directory if missing
    "$path$slideName"
      .also {
        KSlides.logger.info { "Writing String content to: $it" }
        File(it).writeText(content)
      }
  }

  internal fun writeByteArray(
    path: String,
    slideName: String,
    bytes: ByteArray,
  ) {
    mkdir(path)    // Create directory if missing
    "$path$slideName"
      .also {
        KSlides.logger.info { "Writing ByteArray content to: $it" }
        File(it).writeBytes(bytes)
      }
  }

  // mkdirs() (not mkdir()) so nested output paths — e.g. playground/letsPlot/kroki subdirs under a
  // multi-segment outputDir — are created in full rather than silently no-oping on a missing parent.
  internal fun mkdir(name: String) = File(name).run { exists() || mkdirs() }

  private val httpRegex = Regex("\\s*http[s]?://.*")

  internal fun String.isUrl() = lowercase().matches(httpRegex)

  internal fun String.stripBraces() = trimStart().trimEnd().trimStart('[', '(').trimEnd(']', ')')

  internal fun String.pad() = "\n$this\n"
}
