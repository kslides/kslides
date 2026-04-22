package com.kslides

import com.kslides.InternalUtils.fixIndents
import com.kslides.InternalUtils.fromTo
import com.kslides.InternalUtils.isUrl
import com.kslides.InternalUtils.pad
import com.kslides.InternalUtils.toLineRanges
import com.kslides.InternalUtils.whiteSpace
import com.kslides.Utils.INDENT_TOKEN
import com.kslides.slide.DslSlide
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.html.CODE
import kotlinx.html.HTMLTag
import kotlinx.html.unsafe
import java.io.File
import java.net.URL

/** Internal utility holder — not part of the public API. */
object Utils {
  private val logger = KotlinLogging.logger {}
  internal const val INDENT_TOKEN = "--indent--"
}

/**
 * Emit the reveal.js HTML comment that sets a slide's background color. Embed the result
 * verbatim in a Markdown slide's `content{}` block.
 *
 * @param color any valid CSS color value (hex, rgb, named color).
 */
fun slideBackground(color: String) = "<!-- .slide: data-background=\"$color\" -->"

/**
 * Emit a reveal.js fragment attribute comment. Attach to a line in Markdown content to reveal
 * it progressively on click.
 *
 * @param effect fragment animation; [Effect.NONE] emits a plain `.fragment` class.
 * @param index optional `data-fragment-index` for controlling reveal order; `0` omits it.
 */
fun fragment(
  effect: Effect = Effect.NONE,
  index: Int = 0,
) = "<!-- .element: ${effect.toOutput()}${if (index > 0) " data-fragment-index=\"$index\"" else ""} -->"

/**
 * Write [html] into the current kotlinx.html tag without escaping. Intended for embedding
 * Markdown or third-party HTML snippets inside a DSL slide.
 */
fun HTMLTag.rawHtml(html: String) = unsafe { raw(html) }

/**
 * Generate a sequence of list permutations by picking indices from the receiver according to
 * each [orders] spec. Used by the example deck to build progressive-reveal animations of the
 * same set of items in different orders.
 *
 * @param orders one index list per permutation; each inner list must reference valid indices
 *   into the receiver.
 */
fun <T> List<T>.permuteBy(vararg orders: List<Int>): Sequence<List<T>> =
  sequence {
    orders
      .forEach { order ->
        yield(
          buildList {
            order.forEach { this@buildList += this@permuteBy[it] }
          },
        )
      }
  }

/**
 * Parse a reveal.js line-highlight pattern string (e.g. `"[1-3|5|*]"` or `"(1|2|*)"`) into a
 * list of reveal.js `data-line-numbers` tokens. `*` in the input is converted to an empty
 * string (meaning "highlight nothing").
 */
fun String.toLinePatterns() =
  replace(whiteSpace, "")
    .trimStart('[', '(')
    .trimEnd(']', ')')
    .split("|")
    .map { if (it == "*") "" else it }

/**
 * Build a `https://github.com/{user}/{repo}/blob/{branch}/{path}` URL pointing at a source file
 * in the GitHub web UI.
 */
fun githubSourceUrl(
  username: String,
  repoName: String,
  path: String = "",
  branchName: String = "master",
) = "https://github.com/$username/$repoName/blob/$branchName/$path"

/**
 * Build a `https://raw.githubusercontent.com/{user}/{repo}/{branch}/{path}` URL pointing at the
 * raw contents of a file on GitHub. Useful as the `src` argument to [include].
 */
fun githubRawUrl(
  username: String,
  repoName: String,
  path: String = "",
  branchName: String = "master",
) = "https://raw.githubusercontent.com/$username/$repoName/$branchName/$path"

/**
 * Load content from a local file or remote URL. Preferred over embedding code inline in slides
 * because it avoids whitespace-sensitivity issues and lets you keep the source of truth in one
 * place.
 *
 * Intended for use inside `markdownSlide { content { ... } }` and `htmlSlide { content { ... } }`
 * blocks. For [com.kslides.slide.DslSlide] use the [include] variant defined on [DslSlide] or
 * [kotlinx.html.CODE]; those variants disable HTML escaping and the indentation token since the
 * enclosing `<code>` tag handles both.
 *
 * @param src local path (relative to the process working directory) or `http(s)://` URL. Paths
 *   containing `../` are rejected.
 * @param linePattern optional reveal.js-style line range (e.g. `"1-3|5"`) to restrict which
 *   lines are returned.
 * @param beginToken if non-blank, only content after a line containing this token is returned.
 * @param endToken if non-blank, only content before a line containing this token is returned.
 * @param exclusive when `true`, the begin/end-token lines themselves are excluded from the
 *   output.
 * @param trimIndent remove the common leading indentation from all returned lines.
 * @param indentToken marker string the renderer uses to re-indent included lines to match the
 *   surrounding Markdown/HTML; set to `""` in code contexts.
 * @param escapeHtml HTML-escape the returned content; disable for [kotlinx.html.CODE] blocks
 *   that already own their escaping.
 * @return the resolved content, or an empty string if reading fails (a warning is logged).
 * @throws IllegalArgumentException if [src] is a local path containing `"../"`.
 */
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
) = runCatching {
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
}.getOrElse { e ->
  KSlides.logger.warn(e) { "Unable to read ${if (src.isUrl()) "url" else "file"} $src" }
  ""
}

/**
 * [include] variant for use inside a `<code>` block. Disables the HTML escape + indent-token
 * behavior (the `<code>` tag already controls both) and pads the result for reveal.js's
 * line-number display.
 */
// When called from a code block, turn off indentToken and escaping
fun CODE.include(
  src: String,
  linePattern: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  trimIndent: Boolean = true,
) = include(src, linePattern, beginToken, endToken, exclusive, trimIndent, "", false).pad()

/**
 * [include] variant for use inside a [com.kslides.slide.DslSlide] `content{}` block. Same
 * semantics as [CODE.include] — HTML escaping and the indent token are turned off.
 */
// When called from a DslSlide, turn off indentToken and escaping
fun DslSlide.include(
  src: String,
  linePattern: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  trimIndent: Boolean = true,
) = include(src, linePattern, beginToken, endToken, exclusive, trimIndent, "", false).pad()

/**
 * A width × height pair. Used primarily for sizing the `letsPlot{}` render area; construct via
 * the [by] infix helper (`640 by 480`) for readability.
 */
class Dimensions(
  val width: Int,
  val height: Int,
)

/** Infix helper for building a [Dimensions]: `640 by 480`. */
infix fun Int.by(that: Int): Dimensions = Dimensions(this, that)
