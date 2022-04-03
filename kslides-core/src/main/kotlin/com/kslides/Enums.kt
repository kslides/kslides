package com.kslides

import com.github.pambrose.common.util.*

enum class Highlight {
  MONOKAI, ZENBURN
}

enum class Theme {
  BEIGE, BLACK, BLOOD, LEAGUE, MOON, NIGHT, SERIF, SIMPLE, SKY, SOLARIZED, WHITE
}

enum class Speed {
  DEFAULT, FAST, SLOW, UNASSIGNED
}

enum class Transition {
  NONE, FADE, SLIDE, CONVEX, CONCAVE, ZOOM, UNASSIGNED;

  fun asInOut() = name.toLower()
  fun asIn() = "${name.toLower()}-in"
  fun asOut() = "${name.toLower()}-out"
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

  fun toOutput() = "class=\"fragment${if (this != NONE) (" " + name.toLower().replace('_', '-')) else ""}\""
}