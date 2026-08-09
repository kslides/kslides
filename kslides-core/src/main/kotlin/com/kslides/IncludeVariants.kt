package com.kslides

import com.kslides.InternalUtils.pad
import com.kslides.Utils.INDENT_TOKEN
import com.kslides.slide.DslSlide
import com.kslides.slide.MarkdownContent
import kotlinx.html.CODE

// Destination-aware include() overloads. Escaping is not a property of the file being included —
// it depends on where the text lands. A <code> block, a DSL slide, and a Markdown content{} block
// each escape their own output, so content arriving pre-escaped would show &quot; and &lt; as
// visible characters. Each overload is resolved by its block's receiver, so the author never has
// to remember which case they are in. The bare include() keeps escaping on, for htmlSlide content,
// which really is parsed as markup.

/**
 * [include] variant for use inside a `<code>` block. Disables the HTML escape + indent-token
 * behavior (the `<code>` tag already controls both) and pads the result for reveal.js's
 * line-number display.
 */
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
fun DslSlide.include(
  src: String,
  linePattern: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  trimIndent: Boolean = true,
) = include(src, linePattern, beginToken, endToken, exclusive, trimIndent, "", false).pad()

/**
 * [include] variant for a Markdown slide's `content{}` block. Escaping is turned off: reveal.js's
 * Markdown renderer escapes what it emits, so content escaped here would arrive at the browser as
 * visible `&quot;` and `&lt;` rather than the characters they name.
 *
 * Same reasoning as [CODE.include] and [DslSlide.include], and resolved the same way — by the
 * block's receiver rather than by the author remembering. The indent token is kept, since Markdown
 * content is re-indented to match the surrounding block.
 */
fun MarkdownContent.include(
  src: String,
  linePattern: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  trimIndent: Boolean = true,
  indentToken: String = INDENT_TOKEN,
) = include(src, linePattern, beginToken, endToken, exclusive, trimIndent, indentToken, false)
