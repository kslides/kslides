package com.kslides

class SlideConfig : AbstractConfig() {
  var transition by ConfigProperty<Transition>(unmanagedValues)
  var transitionIn by ConfigProperty<Transition>(unmanagedValues)
  var transitionOut by ConfigProperty<Transition>(unmanagedValues)
  var transitionSpeed by ConfigProperty<Speed>(unmanagedValues)
  var backgroundColor by ConfigProperty<String>(unmanagedValues)
  var backgroundIframe by ConfigProperty<String>(unmanagedValues)
  var backgroundInteractive by ConfigProperty<Boolean>(unmanagedValues)
  var backgroundVideo by ConfigProperty<String>(unmanagedValues)
  var markdownSeparator by ConfigProperty<String>(unmanagedValues)
  var markdownVerticalSeparator by ConfigProperty<String>(unmanagedValues)
  var markdownNotesSeparator by ConfigProperty<String>(unmanagedValues)

  fun init() {
    transition = Transition.SLIDE
    transitionIn = Transition.SLIDE
    transitionOut = Transition.SLIDE
    transitionSpeed = Speed.DEFAULT
    backgroundColor = ""
    backgroundIframe = ""
    backgroundInteractive = false
    backgroundVideo = ""
    markdownSeparator = ""
    markdownVerticalSeparator = ""
    markdownNotesSeparator = "^Note:"
  }
}