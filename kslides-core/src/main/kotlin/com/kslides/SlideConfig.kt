package com.kslides

import com.github.pambrose.common.util.*
import kotlinx.html.*

class SlideConfig : AbstractConfig() {
  var transition by ConfigProperty<Transition>(unmanagedValues)
  var transitionIn by ConfigProperty<Transition>(unmanagedValues)
  var transitionOut by ConfigProperty<Transition>(unmanagedValues)
  var transitionSpeed by ConfigProperty<Speed>(unmanagedValues)
  var background by ConfigProperty<String>(unmanagedValues)
  var backgroundColor by ConfigProperty<String>(unmanagedValues)
  var backgroundIframe by ConfigProperty<String>(unmanagedValues)
  var backgroundInteractive by ConfigProperty<Boolean>(unmanagedValues)
  var backgroundVideo by ConfigProperty<String>(unmanagedValues)
  var markdownSeparator by ConfigProperty<String>(unmanagedValues)
  var markdownVerticalSeparator by ConfigProperty<String>(unmanagedValues)
  var markdownNotesSeparator by ConfigProperty<String>(unmanagedValues)

  fun init() {
    transition = Transition.UNASSIGNED
    transitionIn = Transition.UNASSIGNED
    transitionOut = Transition.UNASSIGNED
    transitionSpeed = Speed.UNASSIGNED
    background = ""
    backgroundColor = ""
    backgroundIframe = ""
    backgroundInteractive = false
    backgroundVideo = ""
    markdownSeparator = ""
    markdownVerticalSeparator = ""
    markdownNotesSeparator = "^Notes?:"
  }

  internal fun applyConfig(section: SECTION) {
    if (transition != Transition.UNASSIGNED)
      section.attributes["data-transition"] = transition.asInOut()
    else {
      if (transitionIn != Transition.UNASSIGNED && transitionOut != Transition.UNASSIGNED)
        section.attributes["data-transition"] = "${transitionIn.asIn()} ${transitionOut.asOut()}"
      else if (transitionIn != Transition.UNASSIGNED)
        section.attributes["data-transition"] = "${transitionIn.asIn()} ${Transition.SLIDE.asOut()}"
      else if (transitionOut != Transition.UNASSIGNED)
        section.attributes["data-transition"] = "${Transition.SLIDE.asIn()} ${transitionOut.asOut()}"
    }

    if (transitionSpeed != Speed.UNASSIGNED)
      section.attributes["data-transition-speed"] = transitionSpeed.name.toLower()

    if (background.isNotBlank())
      section.attributes["data-background"] = background

    if (backgroundColor.isNotBlank())
      section.attributes["data-background-color"] = backgroundColor

    if (backgroundIframe.isNotBlank()) {
      section.attributes["data-background-iframe"] = backgroundIframe

      if (backgroundInteractive)
        section.attributes["data-background-interactive"] = ""
    }

    if (backgroundVideo.isNotBlank())
      section.attributes["data-background-video"] = backgroundVideo
  }
}