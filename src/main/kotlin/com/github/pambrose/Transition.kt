package com.github.pambrose

enum class Transition {
    None, Fade, Slide, Convex, Concave, Zoom;

    fun asInOut() = name.toLower()
    fun asIn() = "${name.toLower()}-in"
    fun asOut() = "${name.toLower()}-out"
}