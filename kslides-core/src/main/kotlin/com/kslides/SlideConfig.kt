package com.kslides

import com.github.pambrose.common.util.*
import com.kslides.Transition.SLIDE
import kotlinx.html.*

class SlideConfig : AbstractConfig() {
  var transition by ConfigProperty<Transition>(unmanagedValues)
  var transitionIn by ConfigProperty<Transition>(unmanagedValues)
  var transitionOut by ConfigProperty<Transition>(unmanagedValues)
  var transitionSpeed by ConfigProperty<Speed>(unmanagedValues)
  var background by ConfigProperty<String>(unmanagedValues)
  var backgroundImage by ConfigProperty<String>(unmanagedValues)
  var backgroundColor by ConfigProperty<String>(unmanagedValues)
  var backgroundSize by ConfigProperty<String>(unmanagedValues)
  var backgroundPosition by ConfigProperty<String>(unmanagedValues)
  var backgroundRepeat by ConfigProperty<String>(unmanagedValues)
  var backgroundOpacity by ConfigProperty<Double>(unmanagedValues)
  var backgroundTransition by ConfigProperty<Transition>(unmanagedValues)
  var backgroundIframe by ConfigProperty<String>(unmanagedValues)
  var backgroundInteractive by ConfigProperty<Boolean>(unmanagedValues)
  var backgroundVideo by ConfigProperty<String>(unmanagedValues)
  var backgroundVideoLoop by ConfigProperty<Boolean>(unmanagedValues)
  var backgroundVideoMuted by ConfigProperty<Boolean>(unmanagedValues)

  // Markdown-only items
  var markdownSeparator by ConfigProperty<String>(unmanagedValues)
  var markdownVerticalSeparator by ConfigProperty<String>(unmanagedValues)
  var markdownNotesSeparator by ConfigProperty<String>(unmanagedValues)

  internal fun init() {
    transition = Transition.UNASSIGNED
    transitionIn = Transition.UNASSIGNED
    transitionOut = Transition.UNASSIGNED
    transitionSpeed = Speed.UNASSIGNED
    background = ""
    backgroundColor = ""

    backgroundImage = ""
    backgroundSize = ""
    backgroundPosition = ""
    backgroundRepeat = ""
    backgroundOpacity = -1.0

    backgroundTransition = Transition.UNASSIGNED
    backgroundIframe = ""
    backgroundInteractive = false

    backgroundVideo = ""
    backgroundVideoLoop = false
    backgroundVideoMuted = false

    markdownSeparator = ""
    markdownVerticalSeparator = ""
    markdownNotesSeparator = "^Notes?:"
  }

  internal fun applyConfig(section: SECTION) {
    if (transition != Transition.UNASSIGNED)
      section.attributes["data-transition"] = transition.asInOut()
    else
      when {
        transitionIn != Transition.UNASSIGNED && transitionOut != Transition.UNASSIGNED ->
          section.attributes["data-transition"] = "${transitionIn.asIn()} ${transitionOut.asOut()}"
        transitionIn != Transition.UNASSIGNED ->
          section.attributes["data-transition"] = "${transitionIn.asIn()} ${SLIDE.asOut()}"
        transitionOut != Transition.UNASSIGNED ->
          section.attributes["data-transition"] = "${SLIDE.asIn()} ${transitionOut.asOut()}"
      }

    if (transitionSpeed != Speed.UNASSIGNED)
      section.attributes["data-transition-speed"] = transitionSpeed.name.toLower()

    if (background.isNotBlank())
      section.attributes["data-background"] = background

    if (backgroundColor.isNotBlank())
      section.attributes["data-background-color"] = backgroundColor

    if (backgroundImage.isNotBlank())
      section.attributes["data-background-image"] = backgroundImage

    if (backgroundSize.isNotBlank())
      section.attributes["data-background-size"] = backgroundSize

    if (backgroundPosition.isNotBlank())
      section.attributes["data-background-position"] = backgroundPosition

    if (backgroundRepeat.isNotBlank())
      section.attributes["data-background-repeat"] = backgroundRepeat

    if (backgroundOpacity != -1.0) {
      require(backgroundOpacity in 0.0..1.0) { "backgroundOpacity must be between 0.0 and 1.0" }
      section.attributes["data-background-opacity"] = backgroundOpacity.toString()
    }

    if (backgroundTransition != Transition.UNASSIGNED)
      section.attributes["data-background-transition"] = backgroundTransition.asInOut()

    if (backgroundIframe.isNotBlank()) {
      section.attributes["data-background-iframe"] = backgroundIframe

      if (backgroundInteractive)
        section.attributes["data-background-interactive"] = ""
    }

    if (backgroundVideo.isNotBlank()) {
      section.attributes["data-background-video"] = backgroundVideo
      if (backgroundVideoLoop)
        section.attributes["data-background-video-loop"] = ""
      if (backgroundVideoMuted)
        section.attributes["data-background-video-muted"] = ""
    }
  }

  internal fun applyMarkdownItems(section: SECTION) {
    if (markdownSeparator.isNotBlank())
      section.attributes["data-separator"] = markdownSeparator

    if (markdownVerticalSeparator.isNotBlank())
      section.attributes["data-separator-vertical"] = markdownVerticalSeparator

    // If any of the data-separator values are defined, then plain "---" in markdown will not work
    // So do not define data-separator-notes unless using other data-separator values
    if (markdownNotesSeparator.isNotBlank() && markdownSeparator.isNotBlank() && markdownVerticalSeparator.isNotBlank())
      section.attributes["data-separator-notes"] = markdownNotesSeparator
  }
}