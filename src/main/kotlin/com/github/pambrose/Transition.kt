package com.github.pambrose

enum class Transition() {
    None,
    Fade,
    Slide,
    Convex,
    Concave,
    Zoom;

    fun asInOut() = name.toLowerCase()
    fun asIn() = "${name.toLowerCase()}-in"
    fun asOut() = "${name.toLowerCase()}-out"
}