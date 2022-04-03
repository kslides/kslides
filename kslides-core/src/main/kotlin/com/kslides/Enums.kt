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