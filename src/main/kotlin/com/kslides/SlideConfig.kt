package com.kslides

class SlideConfig private constructor() {

    var transition: Transition = Transition.Slide
        private set

    var transitionIn: Transition = Transition.Slide
        private set

    var transitionOut: Transition = Transition.Slide
        private set

    var speed: Speed = Speed.Default
        private set

    var backgroundColor: String = ""
    var backgroundIframe: String = ""
    var backgroundInteractive: Boolean = false
    var backgroundVideo: String = ""

    fun transition(speed: Speed = Speed.Default) {
        this.speed = speed
    }

    fun transition(transition: Transition, speed: Speed = Speed.Default) {
        this.transition = transition
        this.speed = speed
    }

    fun transition(transitionIn: Transition, transitionOut: Transition, speed: Speed = Speed.Default) {
        this.transitionIn = transitionIn
        this.transitionOut = transitionOut
        this.speed = speed
    }

    companion object {
        fun slideConfig(block: SlideConfig.() -> Unit) = SlideConfig().apply(block)
    }
}