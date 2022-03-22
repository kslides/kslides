package com.kslides

enum class Highlight {
  MONOKAI, ZENBURN
}

enum class Theme {
  BEIGE, BLACK, BLOOD, LEAGUE, MOON, NIGHT, SERIF, SIMPLE, SKY, SOLARIZED, WHITE
}

enum class Speed {
  DEFAULT, FAST, SLOW
}

enum class Transition {
  NONE, FADE, SLIDE, CONVEX, CONCAVE, ZOOM;

  fun asInOut() = name.toLower()
  fun asIn() = "${name.toLower()}-in"
  fun asOut() = "${name.toLower()}-out"
}