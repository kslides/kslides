package com.kslides

import com.github.pambrose.common.util.*
import kotlinx.html.*

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

  internal fun applyConfig(section: SECTION) {
    if (transition != Transition.SLIDE)
      section.attributes["data-transition"] = transition.asInOut()
    else {
      if (transitionIn != Transition.SLIDE || transitionOut != Transition.SLIDE)
        section.attributes["data-transition"] = "${transitionIn.asIn()} ${transitionOut.asOut()}"
    }

    if (transitionSpeed != Speed.DEFAULT)
      section.attributes["data-transition-speed"] = transitionSpeed.name.toLower()

    if (backgroundColor.isNotEmpty())
      section.attributes["data-background-color"] = backgroundColor

    if (backgroundIframe.isNotEmpty()) {
      section.attributes["data-background-iframe"] = backgroundIframe

      if (backgroundInteractive)
        section.attributes["data-background-interactive"] = ""
    }

    if (backgroundVideo.isNotEmpty())
      section.attributes["data-background-video"] = backgroundVideo
  }
}