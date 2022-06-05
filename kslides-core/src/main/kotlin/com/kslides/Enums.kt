package com.kslides

enum class Highlight {
  MONOKAI, ZENBURN;

  val cssSrc get() = "plugin/highlight/${name.lowercase()}.css"
}

enum class PresentationTheme {
  BEIGE, BLACK, BLOOD, LEAGUE, MOON, NIGHT, SERIF, SIMPLE, SKY, SOLARIZED, WHITE;

  val cssSrc get() = "dist/theme/${name.lowercase()}.css"
}

enum class Speed {
  DEFAULT, FAST, SLOW, UNASSIGNED
}

enum class Transition {
  NONE, FADE, SLIDE, CONVEX, CONCAVE, ZOOM, UNASSIGNED;

  fun asInOut() = name.lowercase()
  fun asIn() = "${name.lowercase()}-in"
  fun asOut() = "${name.lowercase()}-out"
}

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
  HIGHLIGHT_CURRENT_BLUE;

  fun toOutput() = "class=\"fragment${if (this != NONE) (" " + name.lowercase().replace('_', '-')) else ""}\""
}

enum class NavigationMode {
  DEFAULT, LINEAR, GRID
}

enum class TargetPlatform(s: String = "") {
  JUNIT, CANVAS, JS, JSIR("js-ir"), JAVA;

  val queryVal: String

  init {
    this.queryVal = s.ifEmpty { this.name.lowercase() }
  }
}

enum class PlaygroundTheme {
  DEFAULT, IDEA, DARCULA
}

enum class PlaygroundMode(s: String = "") {
  KOTLIN, JS, JAVA, GROOVY, XML, C, SHELL, SWIFT, OBJC("obj-c");

  val queryVal: String

  init {
    this.queryVal = s.ifEmpty { this.name.lowercase() }
  }
}

enum class Crosslink {
  ENABLED, DISABLED
}

enum class HrefTarget {
  SELF, BLANK, PARENT, TOP;

  val htmlVal get() = "_${name.lowercase()}"
}