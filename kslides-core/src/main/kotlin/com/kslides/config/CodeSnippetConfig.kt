package com.kslides.config

/**
 * Receiver for [com.kslides.codeSnippet] blocks. Describes a single reveal.js `<pre><code>`
 * element with optional line highlighting, line numbers, and a copy-to-clipboard button.
 *
 * Append to [code] by using the unary-plus operator inside the block: `+"val x = 1"`.
 */
class CodeSnippetConfig {
  /** The code text that will appear inside `<code>`. Append via the unary-plus operator. */
  var code = ""

  /** `<code>` class used by highlight.js / reveal.js to pick a language. Defaults to `"kotlin"`. */
  var language: String = "kotlin"

  /**
   * reveal.js `data-line-numbers` pattern (e.g. `"1-3|5"`). Blank turns on all line numbers;
   * the literal string `"none"` disables line numbers entirely.
   */
  var highlightPattern: String = ""

  /** Starting line number (`data-ln-start-from`). `-1` omits the attribute. */
  var lineOffSet: Int = -1

  /** reveal.js `data-id` used to auto-animate transitions between code snippets. */
  var dataId: String = ""

  /** When `true`, emits `data-trim` so reveal.js strips leading/trailing whitespace. */
  var trim: Boolean = true

  /**
   * When `true`, the HTML is escaped (default reveal.js behavior). Set to `false` (which emits
   * `data-noescape`) when the snippet is already escaped or when you want to embed raw HTML.
   */
  var escapeHtml: Boolean = false

  /** When `true`, attaches a "COPY" button (requires the CopyCode reveal.js plugin). */
  var copyButton: Boolean = true

  /** Custom button label (defaults to the plugin's "Copy" text). */
  var copyButtonText: String = ""

  /** Custom message shown while the "copied" state is active. */
  var copyButtonMsg: String = ""

  /** Append [this] string to [code]. Used inside the `codeSnippet {}` block as `+"…"`. */
  operator fun String.unaryPlus() {
    code += this
  }
}