package com.kslides

class SlideConfig : AbstractConfig() {
  var transition by ConfigProperty<Transition>(primaryValues)
  var transitionIn by ConfigProperty<Transition>(primaryValues)
  var transitionOut by ConfigProperty<Transition>(primaryValues)
  var transitionSpeed by ConfigProperty<Speed>(primaryValues)
  var backgroundColor by ConfigProperty<String>(primaryValues)
  var backgroundIframe by ConfigProperty<String>(primaryValues)
  var backgroundInteractive by ConfigProperty<Boolean>(primaryValues)
  var backgroundVideo by ConfigProperty<String>(primaryValues)

  fun init() {
    transition = Transition.SLIDE
    transitionIn = Transition.SLIDE
    transitionOut = Transition.SLIDE
    transitionSpeed = Speed.DEFAULT
    backgroundColor = ""
    backgroundIframe = ""
    backgroundInteractive = false
    backgroundVideo = ""
  }
}