package com.kslides.config

import com.kslides.*
import com.kslides.Transition.SLIDE
import com.kslides.Utils.INDENT_TOKEN
import kotlinx.html.*

class SlideConfig : AbstractConfig() {
  var transition by ConfigProperty<Transition>(revealjsManagedValues)
  var transitionIn by ConfigProperty<Transition>(revealjsManagedValues)
  var transitionOut by ConfigProperty<Transition>(revealjsManagedValues)
  var transitionSpeed by ConfigProperty<Speed>(revealjsManagedValues)
  var background by ConfigProperty<String>(revealjsManagedValues)
  var backgroundImage by ConfigProperty<String>(revealjsManagedValues)
  var backgroundColor by ConfigProperty<String>(revealjsManagedValues)
  var backgroundSize by ConfigProperty<String>(revealjsManagedValues)
  var backgroundPosition by ConfigProperty<String>(revealjsManagedValues)
  var backgroundRepeat by ConfigProperty<String>(revealjsManagedValues)
  var backgroundOpacity by ConfigProperty<Double>(revealjsManagedValues)
  var backgroundTransition by ConfigProperty<Transition>(revealjsManagedValues)
  var backgroundIframe by ConfigProperty<String>(revealjsManagedValues)
  var backgroundInteractive by ConfigProperty<Boolean>(revealjsManagedValues)
  var backgroundVideo by ConfigProperty<String>(revealjsManagedValues)
  var backgroundVideoLoop by ConfigProperty<Boolean>(revealjsManagedValues)
  var backgroundVideoMuted by ConfigProperty<Boolean>(revealjsManagedValues)

  // MarkdownSlide only items
  var markdownCharset by ConfigProperty<String>(revealjsManagedValues)
  var markdownSeparator by ConfigProperty<String>(revealjsManagedValues)
  var markdownVerticalSeparator by ConfigProperty<String>(revealjsManagedValues)
  var markdownNotesSeparator by ConfigProperty<String>(revealjsManagedValues)

  // MarkdownSlide and HtmlSlide only items
  var indentToken by ConfigProperty<String>(revealjsManagedValues)
  var disableTrimIndent by ConfigProperty<Boolean>(revealjsManagedValues)

  internal fun assignDefaults() {
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

    markdownCharset = ""
    markdownSeparator = ""
    markdownVerticalSeparator = ""
    markdownNotesSeparator = "^Notes?:"

    indentToken = INDENT_TOKEN  // Token for adjusting markdown content indentation
    disableTrimIndent = false   // Disable calling of trimIndent() on markdown content
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
      section.attributes["data-transition-speed"] = transitionSpeed.name.lowercase()

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