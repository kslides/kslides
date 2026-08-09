package com.kslides.config

import com.kslides.InternalUtils.resolveAgainst
import com.kslides.InternalUtils.resolveListAgainst
import com.kslides.KSlidesDslMarker
import com.kslides.Speed
import com.kslides.Transition
import com.kslides.Transition.SLIDE
import com.kslides.Utils.INDENT_TOKEN
import kotlinx.html.SECTION

// Whether data-background names an image rather than a color. Copied from reveal.js's own test
// (dist/reveal.js, background handling), so kslides resolves exactly the values reveal.js will
// treat as an image — keep in sync when the bundled reveal.js is upgraded.
private val imagePathRegex =
  Regex("""\.(svg|png|jpg|jpeg|gif|bmp|webp)([?#\s]|$)""", RegexOption.IGNORE_CASE)

/**
 * Per-slide configuration that can also be set at the global or presentation level and
 * cascaded. Most values are emitted as `data-*` attributes on the slide's `<section>` at
 * render time.
 *
 * Transitions: set [transition] for a symmetric in/out transition, or [transitionIn] /
 * [transitionOut] for independent directions; unset values fall back to [Transition.SLIDE].
 *
 * Backgrounds: [background] is a generic color/image string (reveal.js `data-background`);
 * the more-specific `background*` properties map to `data-background-color`,
 * `data-background-image`, etc.
 */
@KSlidesDslMarker
class SlideConfig : AbstractConfig() {
  /** Slide-transition style for both directions. [Transition.UNASSIGNED] inherits. */
  var transition by ConfigProperty<Transition>(revealjsManagedValues)

  /** Inbound slide transition. [Transition.UNASSIGNED] uses [transition] instead. */
  var transitionIn by ConfigProperty<Transition>(revealjsManagedValues)

  /** Outbound slide transition. [Transition.UNASSIGNED] uses [transition] instead. */
  var transitionOut by ConfigProperty<Transition>(revealjsManagedValues)

  /** Transition speed. [Speed.UNASSIGNED] inherits. */
  var transitionSpeed by ConfigProperty<Speed>(revealjsManagedValues)

  /**
   * reveal.js `data-background` — any valid color or image reference.
   *
   * Resolved against the output root, like [backgroundImage], when the value names an image —
   * decided by the same test reveal.js itself uses, so kslides resolves exactly what reveal.js
   * treats as an image. Anything else — a color name, `#hex`, `rgb()` — is emitted as written.
   * Prefer [backgroundImage] when you mean an image.
   */
  var background by ConfigProperty<String>(revealjsManagedValues)

  /**
   * URL for a full-slide background image (`data-background-image`).
   *
   * A relative value resolves against the output root, so the same path works from a deck at any
   * depth. Absolute (`/img/x.png`), external (`https://...`), and `data:` values pass through.
   */
  var backgroundImage by ConfigProperty<String>(revealjsManagedValues)

  /** Solid background color (`data-background-color`). */
  var backgroundColor by ConfigProperty<String>(revealjsManagedValues)

  /** CSS `background-size` (`"cover"`, `"contain"`, or explicit dimensions). */
  var backgroundSize by ConfigProperty<String>(revealjsManagedValues)

  /** CSS `background-position`. */
  var backgroundPosition by ConfigProperty<String>(revealjsManagedValues)

  /** CSS `background-repeat`. */
  var backgroundRepeat by ConfigProperty<String>(revealjsManagedValues)

  /** Background opacity in the range `0.0..1.0`. `-1.0` omits the attribute. */
  var backgroundOpacity by ConfigProperty<Double>(revealjsManagedValues)

  /** Transition style for full-page slide backgrounds. */
  var backgroundTransition by ConfigProperty<Transition>(revealjsManagedValues)

  /** URL of an iframe shown as the slide background. Resolves like [backgroundImage]. */
  var backgroundIframe by ConfigProperty<String>(revealjsManagedValues)

  /** When `true`, the background iframe receives user input instead of reveal.js. */
  var backgroundInteractive by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * URL of a video shown as the slide background (`data-background-video`), or a comma-separated
   * list of sources. Each source is trimmed and resolves like [backgroundImage]; a `data:` URI is
   * passed through whole, since it carries a comma of its own.
   */
  var backgroundVideo by ConfigProperty<String>(revealjsManagedValues)

  /** Loop the background video. */
  var backgroundVideoLoop by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Mute the background video. */
  var backgroundVideoMuted by ConfigProperty<Boolean>(revealjsManagedValues)

  /** (MarkdownSlide only) Character set (`data-charset`) when loading external Markdown. */
  var markdownCharset by ConfigProperty<String>(revealjsManagedValues)

  /** (MarkdownSlide only) Horizontal-slide separator regex (`data-separator`). */
  var markdownSeparator by ConfigProperty<String>(revealjsManagedValues)

  /** (MarkdownSlide only) Vertical-slide separator regex (`data-separator-vertical`). */
  var markdownVerticalSeparator by ConfigProperty<String>(revealjsManagedValues)

  /**
   * (MarkdownSlide only) Speaker-notes separator regex (`data-separator-notes`). Only emitted
   * when both [markdownSeparator] and [markdownVerticalSeparator] are also set; setting
   * `data-separator-notes` in isolation would disable the default `---` / `--` separators.
   */
  var markdownNotesSeparator by ConfigProperty<String>(revealjsManagedValues)

  /**
   * (Markdown + HtmlSlide) Placeholder token the renderer replaces with the slide's current
   * indentation level when including external content via [com.kslides.include].
   */
  var indentToken by ConfigProperty<String>(revealjsManagedValues)

  /**
   * (Markdown + HtmlSlide) When `true`, the renderer skips `trimIndent()` on slide content.
   * Set this if your content has meaningful leading whitespace.
   */
  var disableTrimIndent by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * Font size for all content on the slide — any CSS length (e.g. `"34px"`, `"0.9em"`).
   * Rendered as an inline `font-size` on the slide's `<section>`; reveal.js themes size
   * headings/text in `em`, so everything scales. Blank inherits the theme default.
   *
   * "Everything" includes code blocks: `<pre>` lives inside the `<section>`, so an `em`
   * [codeFontSize] — and reveal.js's own `0.55em` default — resolves against this value
   * rather than the theme base, and the two multiply. See [codeFontSize].
   *
   * @throws IllegalArgumentException on assignment of a value that is not a CSS length.
   */
  var fontSize by ConfigProperty<String>(kslidesManagedValues, ::requireCssLength)

  /**
   * Font size for code blocks (`<pre>`) on the slide (e.g. `"0.60em"`). Rendered as a
   * generated CSS class + head rule because reveal.js renders Markdown client-side. Blank
   * inherits reveal.js's default (`0.55em`).
   *
   * An `em` value is relative to the enclosing `<section>`, so it compounds with [fontSize]
   * when both are set: `fontSize = "0.65em"` with `codeFontSize = "0.60em"` renders code at
   * `0.60 × 0.65` of the theme base, not `0.60`. To size code independently of [fontSize],
   * use an absolute unit — `"23px"` matches reveal.js's default rendered size on a 42px theme
   * and still scales with the deck, which reveal.js zooms with a transform — or divide the
   * factor out (`"0.85em"` under `fontSize = "0.65em"` ≈ the `0.55em` default).
   *
   * @throws IllegalArgumentException on assignment of a value that is not a CSS length.
   */
  var codeFontSize by ConfigProperty<String>(kslidesManagedValues, ::requireCssLength)

  /**
   * When `true`, long code lines wrap (`white-space: pre-wrap; word-break: break-word`)
   * instead of overflowing horizontally. A slide can set `false` to override a
   * presentation-level `true`.
   */
  var codeWrap by ConfigProperty<Boolean>(kslidesManagedValues)

  internal fun assignDefaults() {
    transition = Transition.UNASSIGNED
    transitionIn = Transition.UNASSIGNED
    transitionOut = Transition.UNASSIGNED
    transitionSpeed = Speed.UNASSIGNED
    background = ""
    backgroundColor = ""

    backgroundImage = ""
    backgroundSize = ""
    backgroundPosition = ""
    backgroundRepeat = ""
    backgroundOpacity = -1.0

    backgroundTransition = Transition.UNASSIGNED
    backgroundIframe = ""
    backgroundInteractive = false

    backgroundVideo = ""
    backgroundVideoLoop = false
    backgroundVideoMuted = false

    markdownCharset = ""
    markdownSeparator = ""
    markdownVerticalSeparator = ""
    markdownNotesSeparator = "^Notes?:"

    indentToken = INDENT_TOKEN  // Token for adjusting markdown content indentation
    disableTrimIndent = false   // Disable calling of trimIndent() on markdown content

    fontSize = ""
    codeFontSize = ""
    codeWrap = false
  }

  @Suppress("CyclomaticComplexMethod")
  internal fun applyConfig(
    section: SECTION,
    rootPrefix: String,
  ) {
    if (transition != Transition.UNASSIGNED)
      section.attributes["data-transition"] = transition.asInOut()
    else
      when {
        transitionIn != Transition.UNASSIGNED && transitionOut != Transition.UNASSIGNED -> {
          section.attributes["data-transition"] = "${transitionIn.asIn()} ${transitionOut.asOut()}"
        }

        transitionIn != Transition.UNASSIGNED -> {
          section.attributes["data-transition"] = "${transitionIn.asIn()} ${SLIDE.asOut()}"
        }

        transitionOut != Transition.UNASSIGNED -> {
          section.attributes["data-transition"] = "${SLIDE.asIn()} ${transitionOut.asOut()}"
        }
      }

    if (transitionSpeed != Speed.UNASSIGNED)
      section.attributes["data-transition-speed"] = transitionSpeed.name.lowercase()

    if (background.isNotBlank())
      section.attributes["data-background"] =
        if (imagePathRegex.containsMatchIn(background)) background.resolveAgainst(rootPrefix) else background

    if (backgroundColor.isNotBlank())
      section.attributes["data-background-color"] = backgroundColor

    if (backgroundImage.isNotBlank())
      section.attributes["data-background-image"] = backgroundImage.resolveAgainst(rootPrefix)

    if (backgroundSize.isNotBlank())
      section.attributes["data-background-size"] = backgroundSize

    if (backgroundPosition.isNotBlank())
      section.attributes["data-background-position"] = backgroundPosition

    if (backgroundRepeat.isNotBlank())
      section.attributes["data-background-repeat"] = backgroundRepeat

    if (backgroundOpacity != -1.0) {
      require(backgroundOpacity in 0.0..1.0) { "backgroundOpacity must be between 0.0 and 1.0" }
      section.attributes["data-background-opacity"] = backgroundOpacity.toString()
    }

    if (backgroundTransition != Transition.UNASSIGNED)
      section.attributes["data-background-transition"] = backgroundTransition.asInOut()

    if (backgroundIframe.isNotBlank()) {
      section.attributes["data-background-iframe"] = backgroundIframe.resolveAgainst(rootPrefix)

      if (backgroundInteractive)
        section.attributes["data-background-interactive"] = ""
    }

    if (backgroundVideo.isNotBlank()) {
      section.attributes["data-background-video"] = backgroundVideo.resolveListAgainst(rootPrefix)
      if (backgroundVideoLoop)
        section.attributes["data-background-video-loop"] = ""
      if (backgroundVideoMuted)
        section.attributes["data-background-video-muted"] = ""
    }
  }

  internal fun applyMarkdownItems(section: SECTION) {
    if (markdownSeparator.isNotBlank())
      section.attributes["data-separator"] = markdownSeparator

    if (markdownVerticalSeparator.isNotBlank())
      section.attributes["data-separator-vertical"] = markdownVerticalSeparator

    // If any of the data-separator values are defined, then plain "---" in markdown will not work
    // So do not define data-separator-notes unless using other data-separator values
    if (markdownNotesSeparator.isNotBlank() && markdownSeparator.isNotBlank() && markdownVerticalSeparator.isNotBlank())
      section.attributes["data-separator-notes"] = markdownNotesSeparator
  }
}
