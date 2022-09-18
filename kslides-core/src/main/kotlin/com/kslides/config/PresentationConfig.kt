package com.kslides.config

import com.kslides.Highlight
import com.kslides.HrefTarget
import com.kslides.KSlidesDslMarker
import com.kslides.PresentationTheme
import com.kslides.Speed
import com.kslides.Transition

class PresentationConfig : AbstractConfig() {

  // The "normal" size of the presentation, aspect ratio will
  // be preserved when the presentation is scaled to fit different
  // resolutions. Can be specified using percentage units.
  var width by ConfigProperty<Any>(revealjsManagedValues) // 960
  var height by ConfigProperty<Any>(revealjsManagedValues) // 700

  // Factor of the display size that should remain empty around
  // the content
  var margin by ConfigProperty<Float>(revealjsManagedValues) // 0.04

  // Bounds for smallest/largest possible scale to apply to content
  var minScale by ConfigProperty<Float>(revealjsManagedValues) // 0.04
  var maxScale by ConfigProperty<Float>(revealjsManagedValues) // 0.04

  // Display presentation control arrows
  var controls by ConfigProperty<Boolean>(revealjsManagedValues) // true

  // Help the user learn the controls by providing hints, for example by
  // bouncing the down arrow when they first encounter a vertical slide
  var controlsTutorial by ConfigProperty<Boolean>(revealjsManagedValues) // true

  // Determines where controls appear, "edges" or "bottom-right"
  var controlsLayout by ConfigProperty<String>(revealjsManagedValues) // 'bottom-right'

  // Visibility rule for backwards navigation arrows; "faded", "hidden"
  // or "visible"
  var controlsBackArrows by ConfigProperty<String>(revealjsManagedValues) // 'faded'

  // Display a presentation progress bar
  var progress by ConfigProperty<Boolean>(revealjsManagedValues) // true

  // Can be used to limit the contexts in which the slide number appears
  // - "all":      Always show the slide number
  // - "print":    Only when printing to PDF
  // - "speaker":  Only in the speaker view
  var showSlideNumber by ConfigProperty<String>(revealjsManagedValues) // 'all'

  // Use 1 based indexing for # links to match slide number (default is zero based)
  var hashOneBasedIndex by ConfigProperty<Boolean>(revealjsManagedValues) // false

  // Add the current slide number to the URL hash so that reloading the
  // page/copying the URL will return you to the same slide
  var hash by ConfigProperty<Boolean>(revealjsManagedValues) // false

  // Flags if we should monitor the hash and change slides accordingly
  var respondToHashChanges by ConfigProperty<Boolean>(revealjsManagedValues) // true

  // Push each slide change to the browser history.  Implies `hash: true`
  var history by ConfigProperty<Boolean>(revealjsManagedValues) // false

  // Enable keyboard shortcuts for navigation
  var keyboard by ConfigProperty<Boolean>(revealjsManagedValues) // true

  // Optional function that blocks keyboard events when retuning false
  //
  // If you set this to 'focused', we will only capture keyboard events
  // for embdedded decks when they are in focus
  var keyboardCondition by ConfigProperty<String>(revealjsManagedValues) // null

  // Disables the default reveal.js slide layout (scaling and centering)
  // so that you can use custom CSS layout
  var disableLayout by ConfigProperty<Boolean>(revealjsManagedValues) // false

  // Enable the slide overview mode
  var overview by ConfigProperty<Boolean>(revealjsManagedValues) // true

  // Vertical centering of slides
  var center by ConfigProperty<Boolean>(revealjsManagedValues) // true

  // Enables touch navigation on devices with touch input
  var touch by ConfigProperty<Boolean>(revealjsManagedValues) // true

  // Loop the presentation
  var loop by ConfigProperty<Boolean>(revealjsManagedValues) // false

  // Change the presentation direction to be RTL
  var rtl by ConfigProperty<Boolean>(revealjsManagedValues) // false

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
  var navigationMode by ConfigProperty<String>(revealjsManagedValues) // 'default'

  // Randomizes the order of slides each time the presentation loads
  var shuffle by ConfigProperty<Boolean>(revealjsManagedValues) // false

  // Turns fragments on and off globally
  var fragments by ConfigProperty<Boolean>(revealjsManagedValues) // true

  // Flags whether to include the current fragment in the URL,
  // so that reloading brings you to the same fragment position
  var fragmentInURL by ConfigProperty<Boolean>(revealjsManagedValues) // true

  // Flags if the presentation is running in an embedded mode,
  // i.e. contained within a limited portion of the screen
  var embedded by ConfigProperty<Boolean>(revealjsManagedValues) // false

  // Flags if we should show a help overlay when the question-mark
  // key is pressed
  var help by ConfigProperty<Boolean>(revealjsManagedValues) // true

  // Flags if it should be possible to pause the presentation (blackout)
  var pause by ConfigProperty<Boolean>(revealjsManagedValues) // true

  // Flags if speaker notes should be visible to all viewers
  var showNotes by ConfigProperty<Boolean>(revealjsManagedValues) // false

  // Global override for autolaying embedded media (video/audio/iframe)
  // - null:   Media will only autoplay if data-autoplay is present
  // - true:   All media will autoplay, regardless of individual setting
  // - false:  No media will autoplay, regardless of individual setting
  var autoPlayMedia by ConfigProperty<Boolean>(revealjsManagedValues) // null

  // Global override for preloading lazy-loaded iframes
  // - null:   Iframes with data-src AND data-preload will be loaded when within
  //           the viewDistance, iframes with only data-src will be loaded when visible
  // - true:   All iframes with data-src will be loaded when within the viewDistance
  // - false:  All iframes with data-src will be loaded only when visible
  var preloadIframes by ConfigProperty<Boolean>(revealjsManagedValues) // null

  // Can be used to globally disable auto-animation
  var autoAnimate by ConfigProperty<Boolean>(revealjsManagedValues) // true

  // Optionally provide a custom element matcher that will be
  // used to dictate which elements we can animate between.
  var autoAnimateMatcher by ConfigProperty<String>(revealjsManagedValues) // null

  // Default settings for our auto-animate transitions, can be
  // overridden per-slide or per-element via data arguments
  var autoAnimateEasing by ConfigProperty<String>(revealjsManagedValues) // 'ease'
  var autoAnimateDuration by ConfigProperty<Float>(revealjsManagedValues) // 1.0
  var autoAnimateUnmatched by ConfigProperty<Boolean>(revealjsManagedValues) // true

  // CSS properties that can be auto-animated. Position & scale
  // is matched separately so there's no need to include styles
  // like top/right/bottom/left, width/height or margin.
  var autoAnimateStyles by ConfigProperty<List<String>>(revealjsManagedValues)
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

  // Stop auto-sliding after user input
  var autoSlideStoppable by ConfigProperty<Boolean>(revealjsManagedValues) // true

  // Use this method for navigation when auto-sliding (defaults to navigateNext)
  // var autoSlideMethod: null, // TODO

  // Specify the average time in seconds that you think you will spend
  // presenting each slide. This is used to show a pacing timer in the
  // speaker view
  var defaultTiming by ConfigProperty<Int>(revealjsManagedValues) // null

  // Enable slide navigation via mouse wheel
  var mouseWheel by ConfigProperty<Boolean>(revealjsManagedValues) // false

  // Opens links in an iframe preview overlay
  // Add `data-preview-link` and `data-preview-link="false"` to customise each link
  // individually
  var previewLinks by ConfigProperty<Boolean>(revealjsManagedValues) // false

  // Exposes the reveal.js API through window.postMessage
  var postMessage by ConfigProperty<Boolean>(revealjsManagedValues) // true

  // Dispatches all reveal.js events to the parent window through postMessage
  var postMessageEvents by ConfigProperty<Boolean>(revealjsManagedValues) // false

  // Focuses body when page changes visibility to ensure keyboard shortcuts work
  var focusBodyOnPageVisibilityChange by ConfigProperty<Boolean>(revealjsManagedValues) // true

  // Transition style
  var transition by ConfigProperty<Transition>(revealjsManagedValues) // 'slide'

  // Transition speed
  var transitionSpeed by ConfigProperty<Speed>(revealjsManagedValues) // 'default'

  // Transition style for full page slide backgrounds
  var backgroundTransition by ConfigProperty<Transition>(revealjsManagedValues) // 'fade'

  // The maximum number of pages a single slide can expand onto when printing
  // to PDF, unlimited by default
  var pdfMaxPagesPerSlide by ConfigProperty<Int>(revealjsManagedValues) // Number.POSITIVE_INFINITY

  // Prints each fragment on a separate slide
  var pdfSeparateFragments by ConfigProperty<Boolean>(revealjsManagedValues) // true

  // Offset used to reduce the height of content within exported PDF pages.
  // This exists to account for environment differences based on how you
  // print to PDF. CLI printing options, like phantomjs and wkpdf, can end
  // on precisely the total height of the document whereas in-browser
  // printing has to end one pixel before.
  var pdfPageHeightOffset by ConfigProperty<Int>(revealjsManagedValues) // -1

  // Number of slides away from the current that are visible
  var viewDistance by ConfigProperty<Int>(revealjsManagedValues) // 3

  // Number of slides away from the current that are visible on mobile
  // devices. It is advisable to set this to a lower number than
  // viewDistance in order to save resources.
  var mobileViewDistance by ConfigProperty<Int>(revealjsManagedValues) // 2

  // The display mode that will be used to show slides
  var display by ConfigProperty<String>(revealjsManagedValues) // 'block'

  // Hide cursor if inactive
  var hideInactiveCursor by ConfigProperty<Boolean>(revealjsManagedValues) // true

  // Time before the cursor is hidden (in ms)
  var hideCursorTime by ConfigProperty<Int>(revealjsManagedValues) // 5000

  // These options have default values set within kslides
  var title by ConfigProperty<String>(kslidesManagedValues)
  var theme by ConfigProperty<PresentationTheme>(kslidesManagedValues)
  var highlight by ConfigProperty<Highlight>(kslidesManagedValues)
  var enableSpeakerNotes by ConfigProperty<Boolean>(kslidesManagedValues)
  var enableZoom by ConfigProperty<Boolean>(kslidesManagedValues)
  var enableSearch by ConfigProperty<Boolean>(kslidesManagedValues)
  var enableMarkdown by ConfigProperty<Boolean>(kslidesManagedValues)
  var enableHighlight by ConfigProperty<Boolean>(kslidesManagedValues)
  var enableMathKatex by ConfigProperty<Boolean>(kslidesManagedValues)
  var enableMathJax2 by ConfigProperty<Boolean>(kslidesManagedValues)
  var enableMathJax3 by ConfigProperty<Boolean>(kslidesManagedValues)
  var enableCodeCopy by ConfigProperty<Boolean>(kslidesManagedValues)
  var enableMenu by ConfigProperty<Boolean>(kslidesManagedValues)

  var topLeftHref by ConfigProperty<String>(kslidesManagedValues)
  var topLeftTarget by ConfigProperty<HrefTarget>(kslidesManagedValues)
  var topLeftTitle by ConfigProperty<String>(kslidesManagedValues)
  var topLeftSvg by ConfigProperty<String>(kslidesManagedValues)
  var topLeftSvgSrc by ConfigProperty<String>(kslidesManagedValues)
  var topLeftSvgClass by ConfigProperty<String>(kslidesManagedValues)
  var topLeftSvgStyle by ConfigProperty<String>(kslidesManagedValues)
  var topLeftText by ConfigProperty<String>(kslidesManagedValues)
  var topRightHref by ConfigProperty<String>(kslidesManagedValues)
  var topRightTarget by ConfigProperty<HrefTarget>(kslidesManagedValues)
  var topRightTitle by ConfigProperty<String>(kslidesManagedValues)
  var topRightSvg by ConfigProperty<String>(kslidesManagedValues)
  var topRightSvgSrc by ConfigProperty<String>(kslidesManagedValues)
  var topRightSvgClass by ConfigProperty<String>(kslidesManagedValues)
  var topRightSvgStyle by ConfigProperty<String>(kslidesManagedValues)
  var topRightText by ConfigProperty<String>(kslidesManagedValues)
  var gaPropertyId by ConfigProperty<String>(kslidesManagedValues)

  // Controls automatic progression to the next slide
  // - 0:      Auto-sliding only happens if the data-autoslide HTML attribute
  //           is present on the current slide or fragment
  // - 1+:     All slides will progress automatically at the given interval
  // - false:  No auto-sliding, even if data-autoslide is present
  var autoSlide by ConfigProperty<Any>(kslidesManagedValues)  // 0

  // Display the page number of the current slide
  // - true:    Show slide number
  // - false:   Hide slide number
  //
  // Can optionally be set as a string that specifies the number formatting:
  // - "h.v":   Horizontal . vertical slide number (default)
  // - "h/v":   Horizontal / vertical slide number
  // - "c":   Flattened slide number
  // - "c/t":   Flattened slide number / total slides
  var slideNumber by ConfigProperty<Any>(kslidesManagedValues) // false

  internal val menuConfig = MenuConfig()
  internal val copyCodeConfig = CopyCodeConfig()
  internal val slideConfig = SlideConfig()
  internal val playgroundConfig = PlaygroundConfig()
  internal val plotlyIframeConfig = PlotlyIframeConfig()
  internal val diagramConfig = DiagramConfig()

  // Only the global default config is initialized with default values
  internal fun assignDefaults() {
    title = ""
    theme = PresentationTheme.BLACK
    highlight = Highlight.MONOKAI
    enableSpeakerNotes = false
    enableZoom = true
    enableSearch = true
    enableMarkdown = true
    enableHighlight = true
    enableMathKatex = false
    enableMathJax2 = false
    enableMathJax3 = false
    enableCodeCopy = true
    enableMenu = false

    topLeftHref = ""
    topLeftTarget = HrefTarget.BLANK
    topLeftTitle = "View source on Github"
    topLeftSvg =
      """
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 55 55">
          <path fill="currentColor" stroke="none" d="M27.5 11.2a16.3 16.3 0 0 0-5.1 31.7c.8.2 1.1-.3 1.1-.7v-2.8c-4.5 1-5.5-2.2-5.5-2.2-.7-1.9-1.8-2.4-1.8-2.4-1.5-1 .1-1 .1-1 1.6.1 2.5 1.7 2.5 1.7 1.5 2.5 3.8 1.8 4.7 1.4.2-1 .6-1.8 1-2.2-3.5-.4-7.3-1.8-7.3-8 0-1.8.6-3.3 1.6-4.4-.1-.5-.7-2.1.2-4.4 0 0 1.4-.4 4.5 1.7a15.6 15.6 0 0 1 8.1 0c3.1-2 4.5-1.7 4.5-1.7.9 2.3.3 4 .2 4.4 1 1 1.6 2.6 1.6 4.3 0 6.3-3.8 7.7-7.4 8 .6.6 1.1 1.6 1.1 3v4.6c0 .4.3.9 1.1.7a16.3 16.3 0 0 0-5.2-31.7"></path>
        </svg>
      """
    topLeftSvgSrc = ""
    topLeftSvgClass = "top-left-svg"
    topLeftSvgStyle = ""
    topLeftText = ""

    // Doesn't appear when href is assigned an empty string
    topRightHref = ""
    topRightTarget = HrefTarget.SELF
    topLeftTitle = ""
    topRightSvg = ""
    topRightSvgSrc = ""
    topRightSvgClass = "top-right-svg"
    topRightSvgStyle = ""
    topRightText = ""

    gaPropertyId = ""

    autoSlide = 0
    slideNumber = false

    slideConfig.assignDefaults()
    playgroundConfig.assignDefaults()
    plotlyIframeConfig.assignDefaults()
    diagramConfig.assignDefaults()
  }

  internal fun mergeConfig(other: PresentationConfig) {
    this.merge(other)
    this.menuConfig.merge(other.menuConfig)
    this.copyCodeConfig.merge(other.copyCodeConfig)
    this.slideConfig.merge(other.slideConfig)
    this.playgroundConfig.merge(other.playgroundConfig)
    this.plotlyIframeConfig.merge(other.plotlyIframeConfig)
    this.diagramConfig.merge(other.diagramConfig)
  }

  @KSlidesDslMarker
  fun menuConfig(block: MenuConfig.() -> Unit) = menuConfig.block()

  @KSlidesDslMarker
  fun copyCodeConfig(block: CopyCodeConfig.() -> Unit) = copyCodeConfig.block()

  @KSlidesDslMarker
  fun slideConfig(block: SlideConfig.() -> Unit) = slideConfig.block()

  @KSlidesDslMarker
  fun playgroundConfig(block: PlaygroundConfig.() -> Unit) = playgroundConfig.block()

  @KSlidesDslMarker
  fun plotlyIframeConfig(block: PlotlyIframeConfig.() -> Unit) = plotlyIframeConfig.block()

  @KSlidesDslMarker
  fun diagramConfig(block: DiagramConfig.() -> Unit) = diagramConfig.block()
}