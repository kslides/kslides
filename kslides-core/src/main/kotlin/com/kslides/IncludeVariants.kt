package com.kslides

import com.kslides.InternalUtils.pad
import com.kslides.Utils.INDENT_TOKEN
import com.kslides.slide.DslSlide
import com.kslides.slide.HtmlSlide
import com.kslides.slide.MarkdownSlide
import kotlinx.html.CODE

// Destination-aware include() overloads, one per sink, each resolved by the receiver of the block
// it is called in. Escaping is not a property of the file being included -- it depends on where
// the text lands, and only the sink knows.
//
// Resolution is lexical, so factoring a content{} body out into a helper function moves the call
// outside the receiver and silently reselects the escaping overload. Keep interpolated include()
// calls inside the block.

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
 * [include] variant for a Markdown slide's `content{}` block, where reveal.js's Markdown renderer
 * escapes its own output. Unlike [CODE.include] and [DslSlide.include] the indent token is kept,
 * since Markdown content is re-indented to match the surrounding block.
 */
fun MarkdownSlide.include(
  src: String,
  linePattern: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  trimIndent: Boolean = true,
  indentToken: String = INDENT_TOKEN,
) = include(src, linePattern, beginToken, endToken, exclusive, trimIndent, indentToken, false)

/**
 * [include] variant for an `htmlSlide` `content{}` block — the one sink whose content is still
 * parsed as markup, so escaping stays on. Identical to the bare [include]; it exists so every sink
 * is receiver-resolved, rather than `htmlSlide` being the one whose behavior comes from the
 * *absence* of an overload.
 */
fun HtmlSlide.include(
  src: String,
  linePattern: String = "",
  beginToken: String = "",
  endToken: String = "",
  exclusive: Boolean = true,
  trimIndent: Boolean = true,
  indentToken: String = INDENT_TOKEN,
) = include(src, linePattern, beginToken, endToken, exclusive, trimIndent, indentToken, true)
