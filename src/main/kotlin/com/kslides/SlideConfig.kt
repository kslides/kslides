package com.kslides

class SlideConfig private constructor() {

  var transition: Transition = Transition.SLIDE
    private set

  var transitionIn: Transition = Transition.SLIDE
    private set

  var transitionOut: Transition = Transition.SLIDE
    private set

  var speed: Speed = Speed.DEFAULT
    private set

  var backgroundColor: String = ""
  var backgroundIframe: String = ""
  var backgroundInteractive: Boolean = false
  var backgroundVideo: String = ""

  fun transition(speed: Speed = Speed.DEFAULT) {
    this.speed = speed
  }

  fun transition(transition: Transition, speed: Speed = Speed.DEFAULT) {
    this.transition = transition
    this.speed = speed
  }

  fun transition(transitionIn: Transition, transitionOut: Transition, speed: Speed = Speed.DEFAULT) {
    this.transitionIn = transitionIn
    this.transitionOut = transitionOut
    this.speed = speed
  }

  companion object {
    fun slideConfig(block: SlideConfig.() -> Unit) = SlideConfig().apply(block)
  }
}