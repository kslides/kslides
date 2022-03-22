package com.kslides

import kotlinx.html.*

class PresentationConfig(init: Boolean = false) : AbstractConfig() {
  internal val menuConfig = MenuConfig()
  internal val slideConfig = SlideConfig()

  var title by ConfigProperty<String>(secondaryValues)
  var theme by ConfigProperty<Theme>(secondaryValues)
  var highlight by ConfigProperty<Highlight>(secondaryValues)
  var enableMenu by ConfigProperty<Boolean>(secondaryValues)
  var enableCodeCopy by ConfigProperty<Boolean>(secondaryValues)

  init {
    if (init) {
      // Only the default config is initialized with default values
      title = ""
      theme = Theme.BLACK
      highlight = Highlight.MONOKAI
      enableMenu = true
      enableCodeCopy = true

      // Initialize the slide config
      slideConfig.init()
    }
  }

  fun merge(other: PresentationConfig) {
    this.combine(other)
    this.menuConfig.combine(other.menuConfig)
    this.slideConfig.combine(other.slideConfig)
  }

  @HtmlTagMarker
  fun menu(block: MenuConfig.() -> Unit) = block.invoke(menuConfig)

  @HtmlTagMarker
  fun slides(block: SlideConfig.() -> Unit) = block.invoke(slideConfig)

  // Display presentation control arrows
  var controls by ConfigProperty<Boolean>(primaryValues) // true

  // Help the user learn the controls by providing hints, for example by
  // bouncing the down arrow when they first encounter a vertical slide
  var controlsTutorial by ConfigProperty<Boolean>(primaryValues) // true

  // Determines where controls appear, "edges" or "bottom-right"
  var controlsLayout by ConfigProperty<String>(primaryValues) // 'bottom-right'

  // Visibility rule for backwards navigation arrows; "faded", "hidden"
  // or "visible"
  var controlsBackArrows by ConfigProperty<String>(primaryValues) // 'faded'

  // Display a presentation progress bar
  var progress by ConfigProperty<Boolean>(primaryValues) // true

  // Display the page number of the current slide
  // - true:    Show slide number
  // - false:   Hide slide number
  //
  // Can optionally be set as a string that specifies the number formatting:
  // - "h.v":   Horizontal . vertical slide number (default)
  // - "h/v":   Horizontal / vertical slide number
  // - "c":   Flattened slide number
  // - "c/t":   Flattened slide number / total slides
  var slideNumber by ConfigProperty<Any>(primaryValues) // false

  // Can be used to limit the contexts in which the slide number appears
  // - "all":      Always show the slide number
  // - "print":    Only when printing to PDF
  // - "speaker":  Only in the speaker view
  var showSlideNumber by ConfigProperty<String>(primaryValues) // 'all'

  // Use 1 based indexing for # links to match slide number (default is zero based)
  var hashOneBasedIndex by ConfigProperty<Boolean>(primaryValues) // false

  // Add the current slide number to the URL hash so that reloading the
  // page/copying the URL will return you to the same slide
  var hash by ConfigProperty<Boolean>(primaryValues) // false

  // Flags if we should monitor the hash and change slides accordingly
  var respondToHashChanges by ConfigProperty<Boolean>(primaryValues) // true

  // Push each slide change to the browser history.  Implies `hash: true`
  var history by ConfigProperty<Boolean>(primaryValues) // false

  // Enable keyboard shortcuts for navigation
  var keyboard by ConfigProperty<Boolean>(primaryValues) // true

  // Optional function that blocks keyboard events when retuning false
  //
  // If you set this to 'focused', we will only capture keyboard events
  // for embdedded decks when they are in focus
  var keyboardCondition by ConfigProperty<String>(primaryValues) // null

  // Disables the default reveal.js slide layout (scaling and centering)
  // so that you can use custom CSS layout
  var disableLayout by ConfigProperty<Boolean>(primaryValues) // false

  // Enable the slide overview mode
  var overview by ConfigProperty<Boolean>(primaryValues) // true

  // Vertical centering of slides
  var center by ConfigProperty<Boolean>(primaryValues) // true

  // Enables touch navigation on devices with touch input
  var touch by ConfigProperty<Boolean>(primaryValues) // true

  // Loop the presentation
  var loop by ConfigProperty<Boolean>(primaryValues) // false

  // Change the presentation direction to be RTL
  var rtl by ConfigProperty<Boolean>(primaryValues) // false

  // Changes the behavior of our navigation directions.
  //
  // "default"
  // Left/right arrow keys step between horizontal slides, up/down
  // arrow keys step between vertical slides. Space key steps through
  // all slides (both horizontal and vertical).
  //
  // "linear"
  // Removes the up/down arrows. Left/right arrows step through all
  // slides (both horizontal and vertical).
  //
  // "grid"
  // When this is enabled, stepping left/right from a vertical stack
  // to an adjacent vertical stack will land you at the same vertical
  // index.
  //
  // Consider a deck with six slides ordered in two vertical stacks:
  // 1.1    2.1
  // 1.2    2.2
  // 1.3    2.3
  //
  // If you're on slide 1.3 and navigate right, you will normally move
  // from 1.3 -> 2.1. If "grid" is used, the same navigation takes you
  // from 1.3 -> 2.3.
  var navigationMode by ConfigProperty<String>(primaryValues) // 'default'

  // Randomizes the order of slides each time the presentation loads
  var shuffle by ConfigProperty<Boolean>(primaryValues) // false

  // Turns fragments on and off globally
  var fragments by ConfigProperty<Boolean>(primaryValues) // true

  // Flags whether to include the current fragment in the URL,
  // so that reloading brings you to the same fragment position
  var fragmentInURL by ConfigProperty<Boolean>(primaryValues) // true

  // Flags if the presentation is running in an embedded mode,
  // i.e. contained within a limited portion of the screen
  var embedded by ConfigProperty<Boolean>(primaryValues) // false

  // Flags if we should show a help overlay when the question-mark
  // key is pressed
  var help by ConfigProperty<Boolean>(primaryValues) // true

  // Flags if it should be possible to pause the presentation (blackout)
  var pause by ConfigProperty<Boolean>(primaryValues) // true

  // Flags if speaker notes should be visible to all viewers
  var showNotes by ConfigProperty<Boolean>(primaryValues) // false

  // Global override for autolaying embedded media (video/audio/iframe)
  // - null:   Media will only autoplay if data-autoplay is present
  // - true:   All media will autoplay, regardless of individual setting
  // - false:  No media will autoplay, regardless of individual setting
  var autoPlayMedia by ConfigProperty<Boolean>(primaryValues) // null

  // Global override for preloading lazy-loaded iframes
  // - null:   Iframes with data-src AND data-preload will be loaded when within
  //           the viewDistance, iframes with only data-src will be loaded when visible
  // - true:   All iframes with data-src will be loaded when within the viewDistance
  // - false:  All iframes with data-src will be loaded only when visible
  var preloadIframes by ConfigProperty<Boolean>(primaryValues) // null

  // Can be used to globally disable auto-animation
  var autoAnimate by ConfigProperty<Boolean>(primaryValues) // true

  // Optionally provide a custom element matcher that will be
  // used to dictate which elements we can animate between.
  var autoAnimateMatcher by ConfigProperty<String>(primaryValues) // null

  // Default settings for our auto-animate transitions, can be
  // overridden per-slide or per-element via data arguments
  var autoAnimateEasing by ConfigProperty<String>(primaryValues) // 'ease'
  var autoAnimateDuration by ConfigProperty<Float>(primaryValues) // 1.0
  var autoAnimateUnmatched by ConfigProperty<Boolean>(primaryValues) // true

  // CSS properties that can be auto-animated. Position & scale
  // is matched separately so there's no need to include styles
  // like top/right/bottom/left, width/height or margin.
  var autoAnimateStyles by ConfigProperty<List<String>>(primaryValues)
  //    var autoAnimateStyles: [
  //    'opacity',
  //    'color',
  //    'background-color',
  //    'padding',
  //    'font-size',
  //    'line-height',
  //    'letter-spacing',
  //    'border-width',
  //    'border-color',
  //    'border-radius',
  //    'outline',
  //    'outline-offset'
  //    ],

  // Controls automatic progression to the next slide
  // - 0:      Auto-sliding only happens if the data-autoslide HTML attribute
  //           is present on the current slide or fragment
  // - 1+:     All slides will progress automatically at the given interval
  // - false:  No auto-sliding, even if data-autoslide is present
  // var autoSlide: 0, // TODO

  // Stop auto-sliding after user input
  var autoSlideStoppable by ConfigProperty<Boolean>(primaryValues) // true

  // Use this method for navigation when auto-sliding (defaults to navigateNext)
  // var autoSlideMethod: null, // TODO

  // Specify the average time in seconds that you think you will spend
  // presenting each slide. This is used to show a pacing timer in the
  // speaker view
  var defaultTiming by ConfigProperty<Int>(primaryValues) // null

  // Enable slide navigation via mouse wheel
  var mouseWheel by ConfigProperty<Boolean>(primaryValues) // false

  // Opens links in an iframe preview overlay
  // Add `data-preview-link` and `data-preview-link="false"` to customise each link
  // individually
  var previewLinks by ConfigProperty<Boolean>(primaryValues) // false

  // Exposes the reveal.js API through window.postMessage
  var postMessage by ConfigProperty<Boolean>(primaryValues) // true

  // Dispatches all reveal.js events to the parent window through postMessage
  var postMessageEvents by ConfigProperty<Boolean>(primaryValues) // false

  // Focuses body when page changes visibility to ensure keyboard shortcuts work
  var focusBodyOnPageVisibilityChange by ConfigProperty<Boolean>(primaryValues) // true

  // Transition style
  var transition by ConfigProperty<Transition>(primaryValues) // 'slide'

  // Transition speed
  var transitionSpeed by ConfigProperty<Speed>(primaryValues) // 'default'

  // Transition style for full page slide backgrounds
  var backgroundTransition by ConfigProperty<Transition>(primaryValues) // 'fade'

  // The maximum number of pages a single slide can expand onto when printing
  // to PDF, unlimited by default
  var pdfMaxPagesPerSlide by ConfigProperty<Int>(primaryValues) // Number.POSITIVE_INFINITY

  // Prints each fragment on a separate slide
  var pdfSeparateFragments by ConfigProperty<Boolean>(primaryValues) // true

  // Offset used to reduce the height of content within exported PDF pages.
  // This exists to account for environment differences based on how you
  // print to PDF. CLI printing options, like phantomjs and wkpdf, can end
  // on precisely the total height of the document whereas in-browser
  // printing has to end one pixel before.
  var pdfPageHeightOffset by ConfigProperty<Int>(primaryValues) // -1

  // Number of slides away from the current that are visible
  var viewDistance by ConfigProperty<Int>(primaryValues) // 3

  // Number of slides away from the current that are visible on mobile
  // devices. It is advisable to set this to a lower number than
  // viewDistance in order to save resources.
  var mobileViewDistance by ConfigProperty<Int>(primaryValues) // 2

  // The display mode that will be used to show slides
  var display by ConfigProperty<String>(primaryValues) // 'block'

  // Hide cursor if inactive
  var hideInactiveCursor by ConfigProperty<Boolean>(primaryValues) // true

  // Time before the cursor is hidden (in ms)
  var hideCursorTime by ConfigProperty<Int>(primaryValues) // 5000
}