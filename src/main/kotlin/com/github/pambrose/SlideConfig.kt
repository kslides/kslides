package com.github.pambrose

import kotlinx.html.SECTION

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

        fun SECTION.applyConfig(config: SlideConfig) {
            if (config.transition != Transition.Slide)
                attributes["data-transition"] = config.transition.asInOut()
            else {
                if (config.transitionIn != Transition.Slide || config.transitionOut != Transition.Slide)
                    attributes["data-transition"] = "${config.transitionIn.asIn()} ${config.transitionOut.asOut()}"
            }

            if (config.speed != Speed.Default)
                attributes["data-transition-speed"] = config.speed.name.toLowerCase()

            if (config.backgroundColor.isNotEmpty())
                attributes["data-background"] = config.backgroundColor

            if (config.backgroundIframe.isNotEmpty()) {
                attributes["data-background-iframe"] = config.backgroundIframe

                if (config.backgroundInteractive)
                    attributes["data-background-interactive"] = ""
            }

            if (config.backgroundVideo.isNotEmpty())
                attributes["data-background-video"] = config.backgroundVideo
        }
    }
}

