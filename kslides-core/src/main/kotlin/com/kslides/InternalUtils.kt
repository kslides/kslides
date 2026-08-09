package com.kslides

import org.apache.commons.text.StringEscapeUtils
import org.xml.sax.SAXParseException
import java.io.File

/**
 * Module-private utility helpers used across kslides-core: indentation handling for `include()`,
 * line-range parsing for code-snippet highlighting, file output, small string predicates, and the
 * path-resolution rule every emitted URL goes through ([resolveAgainst]).
 * Implementation detail — not part of the public API.
 */
@Suppress("TooManyFunctions")
internal object InternalUtils {
  internal val whiteSpace = "\\s".toRegex()

  private const val SNIPPET_MARGIN = 40

  private const val SNIPPET_FULL_LINE = 160

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
    // escapeXml10, not escapeHtml4: this text is parsed as XML on its way into the DOM, where
    // HTML4's named entities are undefined — an em dash escaped to "&mdash;" aborts the render
    // with `The entity "mdash" was referenced, but not declared`.
    .joinToString("\n") { if (escapeHtml) StringEscapeUtils.escapeXml10(it) else it }

  // An ampersand XML would reject: one opening neither a numeric reference nor one of the five
  // names XML predeclares. It may still open a name HTML knows, which the group captures.
  private val illegalAmpersandRegex =
    Regex("&(?!#\\d+;|#x[0-9a-fA-F]+;|(?:amp|lt|gt|quot|apos);)([a-zA-Z][a-zA-Z0-9]*;)?")

  /**
   * Make element content safe for the XML parse on the way into the DOM — slide bodies, inline
   * SVG, third-party snippets. Two things are repairable, and both losslessly: an ampersand
   * opening no reference XML declares, and a `]]>` run, which XML forbids in content even outside
   * CDATA. Serialization puts each back the way it was written.
   *
   * A named entity XML does not predeclare (`&nbsp;`, `&mdash;`) becomes the character it stands
   * for, which is what the author meant and what the browser would have shown; anything naming no
   * entity at all is escaped instead, exactly as a browser renders it. The decoding is
   * unconditional, so content *about* HTML entities has to write `&amp;nbsp;` to show one.
   *
   * A bare `<` is the one that gets away, and deliberately: telling a real tag from a stray `<` is
   * the judgement a raw markup sink exists to avoid making. Script and style bodies face none of
   * this — they are emitted as text nodes, which never reach the parser.
   */
  internal fun String.xmlSafeAsMarkup(): String {
    val cdataSafe = if (contains("]]>")) replace("]]>", "]]&gt;") else this
    // Most text holds no ampersand at all, so settle that with indexOf rather than the regex.
    // Both are cheap next to the XML parse this feeds, which is the cost that actually matters.
    if (cdataSafe.indexOf('&') < 0) return cdataSafe
    return illegalAmpersandRegex.replace(cdataSafe) { match ->
      val decoded = StringEscapeUtils.unescapeHtml4(match.value)
      // A lone "&", or a name HTML does not know either, opens no reference and stands for
      // itself — which is what a browser shows.
      if (decoded == match.value) "&amp;${match.groupValues[1]}" else decoded
    }
  }

  // kotlinx.html parses raw content as "<unsafeRoot>$content</unsafeRoot>", so a reported column
  // on line 1 counts the wrapper too.
  private const val UNSAFE_ROOT_PREFIX = "<unsafeRoot>"

  /**
   * Turn a parser failure into something an author can act on.
   *
   * Xerces reports a line and column into a document kslides synthesized and nobody has seen, and
   * the failure takes down every deck in the render rather than the slide that caused it. So
   * quote the offending line, point at the column, and name the fix — [parsed] is the text as the
   * parser saw it, which is what those coordinates index.
   */
  internal fun xmlParseFailure(
    parsed: String,
    e: SAXParseException,
  ): String {
    val lines = parsed.lines()
    val index = (e.lineNumber - 1).coerceIn(0, lines.lastIndex)
    val onFirstLine = index == 0
    val line = lines[index].let { if (onFirstLine) it.removePrefix(UNSAFE_ROOT_PREFIX) else it }
    val column = (if (onFirstLine) e.columnNumber - UNSAFE_ROOT_PREFIX.length else e.columnNumber).coerceIn(1, line.length + 1)

    // Show the whole line when it is readable: the reported column is where the parser gave up,
    // which on a long line can be well past the character the author actually got wrong, so a
    // window centred there would crop the mistake out. Only genuinely long lines get windowed.
    val windowed = line.length > SNIPPET_FULL_LINE
    val from = if (windowed) (column - 1 - SNIPPET_MARGIN).coerceAtLeast(0) else 0
    val to = if (windowed) (column - 1 + SNIPPET_MARGIN).coerceAtMost(line.length) else line.length
    val snippet = (if (from > 0) "..." else "") + line.substring(from, to) + (if (to < line.length) "..." else "")
    val caret = " ".repeat((if (from > 0) 3 else 0) + (column - 1 - from)) + "^"

    return buildString {
      appendLine("slide content is not well-formed XML (line ${e.lineNumber}, column ${e.columnNumber}): ${e.message}")
      appendLine()
      appendLine("  $snippet")
      appendLine("  $caret")
      appendLine()
      append(
        "This content is parsed as markup, so a bare '<' reads as a tag — write '&lt;'. Only " +
          "htmlSlide bodies, inline SVG and rawHtml() take that path; Markdown, script and style " +
          "content does not.",
      )
    }
  }

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

  // Whether the author already anchored this path themselves: absolute or protocol-relative
  // ("/foo", "//cdn/foo"), an http(s) URL, or a data: URI.
  private fun String.isAnchoredPath() = startsWith("/") || startsWith("data:") || isUrl()

  /**
   * kslides' one path-resolution rule: a relative path is resolved against [prefix], and a path the
   * author already anchored is emitted as written.
   *
   * Every URL the renderer emits goes through this, differing only in which prefix is passed: the
   * render's walk back to the output root (see [com.kslides.Presentation.renderRootPrefix]) for
   * author-supplied paths, the reveal.js asset directory for kslides' own filenames, and for
   * [com.kslides.CssFile] / [com.kslides.JsFile] whichever the entry's
   * [com.kslides.AssetOrigin] names.
   */
  internal fun String.resolveAgainst(prefix: String): String = if (isAnchoredPath()) this else "$prefix$this"

  /**
   * [resolveAgainst] for an attribute that takes a comma-separated list of sources, resolving each
   * one. Sources are trimmed, since a leading space would otherwise land between the prefix and the
   * path.
   *
   * A `data:` URI carries a comma by construction, so such a value is passed through whole rather
   * than split — splitting it would strip the anchoring [resolveAgainst] promises to honor.
   */
  internal fun String.resolveListAgainst(prefix: String): String =
    if (startsWith("data:"))
      this
    else
      split(",").joinToString(",") { it.trim().resolveAgainst(prefix) }

  internal fun String.stripBraces() = trimStart().trimEnd().trimStart('[', '(').trimEnd(']', ')')

  internal fun String.pad() = "\n$this\n"
}
