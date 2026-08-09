package com.kslides

import kotlinx.html.HTMLTag
import kotlinx.html.unsafe
import org.apache.commons.text.StringEscapeUtils
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
   * The two things kslides can repair before handing text to the XML parser: an ampersand opening
   * no reference XML declares, and a `]]>` run, which XML forbids in content even outside CDATA.
   * Both are losslessly reversible — serialization puts them back the way they were written.
   *
   * A bare `<` is the one that gets away, and deliberately: distinguishing a real tag from a stray
   * `<` is exactly the judgement the raw sinks exist to avoid making.
   */
  private fun String.repairForXml(repair: (MatchResult) -> CharSequence): String {
    // Most text holds no ampersand at all, and the regex scan costs ~1ns/char on a render path
    // that is serialized on the render lock and re-run per HTTP request; indexOf settles it in
    // ~50ns.
    val cdataSafe = if (contains("]]>")) replace("]]>", "]]&gt;") else this
    return if (cdataSafe.indexOf('&') < 0) cdataSafe else illegalAmpersandRegex.replace(cdataSafe, repair)
  }

  /**
   * Make text safe for the XML parse on the way into the DOM, treating it as the body of a
   * `<script>` or `<style>` — raw-text elements, where HTML does not decode entities. So an
   * illegal ampersand is escaped rather than decoded: a decoded `&&` or `&copy;` would silently
   * rewrite the author's JavaScript. Lossless regardless, since serialization hands `&amp;&amp;`
   * back as `&&` inside those elements.
   *
   * Numeric references and XML's own five names are already legal and pass through, so this is a
   * no-op over content [fixIndents] has already escaped.
   */
  internal fun String.xmlSafeAsRawText(): String = repairForXml { match -> "&amp;${match.groupValues[1]}" }

  /**
   * [xmlSafeAsRawText] for element content — slide bodies, inline SVG, third-party snippets. Here
   * a named entity XML does not predeclare (`&nbsp;`, `&mdash;`) becomes the character it stands
   * for, which is what the author meant and what the browser would have shown. Anything naming no
   * entity at all is escaped instead, exactly as a browser renders it.
   *
   * The decoding is unconditional, so content *about* HTML entities has to write `&amp;nbsp;` to
   * show one.
   */
  internal fun String.xmlSafeAsMarkup(): String =
    repairForXml { match ->
      val decoded = StringEscapeUtils.unescapeHtml4(match.value)
      // A lone "&", or a name HTML does not know either, opens no reference and stands for
      // itself — which is what a browser shows.
      if (decoded == match.value) "&amp;${match.groupValues[1]}" else decoded
    }

  /**
   * [com.kslides.rawHtml] for the body of a `<script>` or `<style>` — raw-text elements, where
   * HTML does not decode entities, so a decoded `&&` or `&copy;` would silently rewrite an
   * author's JavaScript or CSS. Illegal ampersands are escaped rather than decoded, which is
   * lossless here: serialization hands them back bare inside those two elements, so the browser
   * sees what was written.
   */
  internal fun HTMLTag.rawSource(source: String) = unsafe { raw(source.xmlSafeAsRawText()) }

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
