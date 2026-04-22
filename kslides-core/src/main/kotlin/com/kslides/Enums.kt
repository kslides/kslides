package com.kslides

/**
 * Code-highlighting theme shipped with the reveal.js highlight plugin. Assign to
 * [com.kslides.config.PresentationConfig.highlight].
 */
enum class Highlight {
  MONOKAI,
  ZENBURN,
  ;

  /** Path (relative to the reveal.js static root) of this theme's stylesheet. */
  val cssSrc get() = "plugin/highlight/${name.lowercase()}.css"
}

/**
 * Built-in reveal.js presentation theme. Assign to
 * [com.kslides.config.PresentationConfig.theme].
 */
enum class PresentationTheme {
  BEIGE,
  BLACK,
  BLOOD,
  LEAGUE,
  MOON,
  NIGHT,
  SERIF,
  SIMPLE,
  SKY,
  SOLARIZED,
  WHITE,
  DRACULA,
  BLACK_CONTRAST,
  WHITE_CONTRAST,
  ;

  /** Path (relative to the reveal.js static root) of this theme's stylesheet. */
  val cssSrc get() = "dist/theme/${name.lowercase().replace("_", "-")}.css"
}

/**
 * reveal.js transition speed. [UNASSIGNED] means "inherit from the enclosing scope" and is the
 * default in [com.kslides.config.SlideConfig].
 */
enum class Speed {
  DEFAULT,
  FAST,
  SLOW,
  UNASSIGNED,
}

/**
 * reveal.js slide-transition style. [UNASSIGNED] means "inherit" and is the default in
 * [com.kslides.config.SlideConfig] so that per-slide transitions fall back to the presentation
 * setting.
 */
enum class Transition {
  NONE,
  FADE,
  SLIDE,
  CONVEX,
  CONCAVE,
  ZOOM,
  UNASSIGNED,
  ;

  /** reveal.js `data-transition` token for bidirectional transitions (e.g. `"fade"`). */
  fun asInOut() = name.lowercase()

  /** reveal.js `data-transition` token for inbound transitions (e.g. `"fade-in"`). */
  fun asIn() = "${name.lowercase()}-in"

  /** reveal.js `data-transition` token for outbound transitions (e.g. `"fade-out"`). */
  fun asOut() = "${name.lowercase()}-out"
}

/**
 * reveal.js fragment effect applied to a progressively-revealed element. Used by [com.kslides.fragment]
 * to build `.fragment` class attributes.
 */
enum class Effect {
  NONE,
  FADE_OUT,
  FADE_UP,
  FADE_DOWN,
  FADE_LEFT,
  FADE_RIGHT,
  FADE_IN_THEN_OUT,
  FADE_IN_THEN_SEMI_OUT,
  GROW,
  SEMI_FADE_OUT,
  SHRINK,
  STRIKE,
  HIGHLIGHT_RED,
  HIGHLIGHT_GREEN,
  HIGHLIGHT_BLUE,
  HIGHLIGHT_CURRENT_RED,
  HIGHLIGHT_CURRENT_GREEN,
  HIGHLIGHT_CURRENT_BLUE,
  ;

  /** Build the reveal.js `class="fragment ..."` attribute fragment for this effect. */
  fun toOutput() = "class=\"fragment${if (this != NONE) (" " + name.lowercase().replace('_', '-')) else ""}\""
}

/**
 * reveal.js navigation mode. Controls how arrow-key and touch navigation behave across
 * horizontal and vertical slides.
 */
enum class NavigationMode {
  DEFAULT,
  LINEAR,
  GRID,
}

/**
 * Kotlin Playground execution target — what runtime the embedded code runs against. Mirrors
 * the [Kotlin Playground](https://github.com/JetBrains/kotlin-playground) `data-target-platform`
 * attribute.
 *
 * @property queryVal the corresponding string sent to Playground.
 */
enum class TargetPlatform(
  s: String = "",
) {
  JUNIT,
  CANVAS,
  JS,
  JSIR("js-ir"),
  JAVA,
  ;

  /** Value written into Playground's `data-target-platform` attribute. */
  val queryVal: String = s.ifEmpty { this.name.lowercase() }
}

/** Kotlin Playground editor theme. */
enum class PlaygroundTheme {
  DEFAULT,
  IDEA,
  DARCULA,
}

/**
 * Language/syntax mode the Kotlin Playground editor operates in. Not every mode is executable —
 * some (e.g. [GROOVY], [XML]) only enable syntax highlighting.
 *
 * @property queryVal the corresponding string sent to Playground.
 */
enum class PlaygroundMode(
  s: String = "",
) {
  KOTLIN,
  JS,
  JAVA,
  GROOVY,
  XML,
  C,
  SHELL,
  SWIFT,
  OBJC("obj-c"),
  ;

  /** Value written into Playground's language-mode attribute. */
  val queryVal: String = s.ifEmpty { this.name.lowercase() }
}

/** Toggle for Kotlin Playground's "open in IDE" crosslink button. */
enum class Crosslink {
  ENABLED,
  DISABLED,
}

/**
 * HTML `<a target="...">` value. Used by corner links and [com.kslides.listHref] to avoid
 * hard-coding the magic strings.
 */
enum class HrefTarget {
  SELF,
  BLANK,
  PARENT,
  TOP,
  ;

  /** The raw HTML value (e.g. `"_blank"`, `"_self"`). */
  val htmlVal get() = "_${name.lowercase()}"
}

/** reveal.js 5.0+ view mode: the default "deck" layout versus the scrolling "doc" layout. */
enum class ViewType { DEFAULT, SCROLL }

/** Options for reveal.js 5.0+ `scrollProgress`. Use the config property's `Boolean` form for on/off. */
enum class ScrollProgress { AUTO }

/** reveal.js 5.0+ scroll-layout mode. */
enum class ScrollLayout { COMPACT, FULL }

/** reveal.js 5.0+ scroll-snap mode. Use the config property's `Boolean` form to disable snapping. */
enum class ScrollSnap { PROXIMITY, MANDATORY }
