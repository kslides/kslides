package com.kslides

class SlideConfig {

  var transition: Transition = Transition.SLIDE
  var transitionIn: Transition = Transition.SLIDE
  var transitionOut: Transition = Transition.SLIDE
  var transitionSpeed: Speed = Speed.DEFAULT
  var backgroundColor: String = ""
  var backgroundIframe: String = ""
  var backgroundInteractive: Boolean = false
  var backgroundVideo: String = ""

  fun transition(speed: Speed = Speed.DEFAULT) {
    this.transitionSpeed = speed
  }

  fun transition(transition: Transition, speed: Speed = Speed.DEFAULT) {
    this.transition = transition
    this.transitionSpeed = speed
  }

  fun transition(transitionIn: Transition, transitionOut: Transition, speed: Speed = Speed.DEFAULT) {
    this.transitionIn = transitionIn
    this.transitionOut = transitionOut
    this.transitionSpeed = speed
  }

  companion object {
    fun slideConfig(block: SlideConfig.() -> Unit) = SlideConfig().apply(block)
  }
}