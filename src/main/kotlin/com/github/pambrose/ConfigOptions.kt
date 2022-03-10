package com.github.pambrose

import kotlin.reflect.*

class ConfigOptions {
    val plugins = mutableListOf("RevealZoom", "RevealSearch", "RevealMarkdown", "RevealHighlight")
    val configMap = mutableMapOf<String, Any>()

    // Display presentation control arrows
    var controls by ConfigProperty<Boolean>(configMap) // true,

    // Help the user learn the controls by providing hints, for example by
    // bouncing the down arrow when they first encounter a vertical slide
    var controlsTutorial by ConfigProperty<Boolean>(configMap) // true,

    // Determines where controls appear, "edges" or "bottom-right"
    var controlsLayout by ConfigProperty<String>(configMap) // 'bottom-right',

    // Visibility rule for backwards navigation arrows; "faded", "hidden"
    // or "visible"
    var controlsBackArrows by ConfigProperty<String>(configMap) // 'faded',

    // Display a presentation progress bar
    var progress by ConfigProperty<Boolean>(configMap) // true,

    // Display the page number of the current slide
    // - true:    Show slide number
    // - false:   Hide slide number
    //
    // Can optionally be set as a string that specifies the number formatting:
    // - "h.v":   Horizontal . vertical slide number (default)
    // - "h/v":   Horizontal / vertical slide number
    // - "c":   Flattened slide number
    // - "c/t":   Flattened slide number / total slides
    //
    // Alternatively, you can provide a function that returns the slide
    // number for the current slide. The function should take in a slide
    // object and return an array with one string [slideNumber] or
    // three strings [n1,delimiter,n2]. See #formatSlideNumber().
    var slideNumber by ConfigProperty<Boolean>(configMap) // false,

    // Can be used to limit the contexts in which the slide number appears
    // - "all":      Always show the slide number
    // - "print":    Only when printing to PDF
    // - "speaker":  Only in the speaker view
    var showSlideNumber by ConfigProperty<String>(configMap) // 'all',

    // Use 1 based indexing for # links to match slide number (default is zero
    // based)
    var hashOneBasedIndex by ConfigProperty<Boolean>(configMap) // false,

    // Add the current slide number to the URL hash so that reloading the
    // page/copying the URL will return you to the same slide
    var hash by ConfigProperty<Boolean>(configMap) // false,

    // Flags if we should monitor the hash and change slides accordingly
    var respondToHashChanges by ConfigProperty<Boolean>(configMap) // true,

    // Push each slide change to the browser history.  Implies `hash: true`
    var history by ConfigProperty<Boolean>(configMap) // false,

    // Enable keyboard shortcuts for navigation
    var keyboard by ConfigProperty<Boolean>(configMap) // true,

    // Optional function that blocks keyboard events when retuning false
    //
    // If you set this to 'focused', we will only capture keyboard events
    // for embdedded decks when they are in focus
    var keyboardCondition by ConfigProperty<String>(configMap) // null,

    // Disables the default reveal.js slide layout (scaling and centering)
    // so that you can use custom CSS layout
    var disableLayout by ConfigProperty<Boolean>(configMap) // false,

    // Enable the slide overview mode
    var overview by ConfigProperty<Boolean>(configMap) // true,

    // Vertical centering of slides
    var center by ConfigProperty<Boolean>(configMap) // true,

    // Enables touch navigation on devices with touch input
    var touch by ConfigProperty<Boolean>(configMap) // true,

    // Loop the presentation
    var loop by ConfigProperty<Boolean>(configMap) // false,

    // Change the presentation direction to be RTL
    var rtl by ConfigProperty<Boolean>(configMap) // false,

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
    var navigationMode by ConfigProperty<String>(configMap) // 'default',

    // Randomizes the order of slides each time the presentation loads
    var shuffle by ConfigProperty<Boolean>(configMap) // false,

    // Turns fragments on and off globally
    var fragments by ConfigProperty<Boolean>(configMap) // true,

    // Flags whether to include the current fragment in the URL,
    // so that reloading brings you to the same fragment position
    var fragmentInURL by ConfigProperty<Boolean>(configMap) // true,

    // Flags if the presentation is running in an embedded mode,
    // i.e. contained within a limited portion of the screen
    var embedded by ConfigProperty<Boolean>(configMap) // false,

    // Flags if we should show a help overlay when the question-mark
    // key is pressed
    var help by ConfigProperty<Boolean>(configMap) // true,

    // Flags if it should be possible to pause the presentation (blackout)
    var pause by ConfigProperty<Boolean>(configMap) // true,

    // Flags if speaker notes should be visible to all viewers
    var showNotes by ConfigProperty<Boolean>(configMap) // false,

    // Global override for autolaying embedded media (video/audio/iframe)
    // - null:   Media will only autoplay if data-autoplay is present
    // - true:   All media will autoplay, regardless of individual setting
    // - false:  No media will autoplay, regardless of individual setting
    var autoPlayMedia by ConfigProperty<Boolean>(configMap) // null,

    // Global override for preloading lazy-loaded iframes
    // - null:   Iframes with data-src AND data-preload will be loaded when within
    //           the viewDistance, iframes with only data-src will be loaded when visible
    // - true:   All iframes with data-src will be loaded when within the viewDistance
    // - false:  All iframes with data-src will be loaded only when visible
    var preloadIframes by ConfigProperty<Boolean>(configMap) // null,

    // Can be used to globally disable auto-animation
    var autoAnimate by ConfigProperty<Boolean>(configMap) // true,

    // Optionally provide a custom element matcher that will be
    // used to dictate which elements we can animate between.
    var autoAnimateMatcher by ConfigProperty<String>(configMap) // null,

    // Default settings for our auto-animate transitions, can be
    // overridden per-slide or per-element via data arguments
    var autoAnimateEasing by ConfigProperty<String>(configMap) // 'ease',
    var autoAnimateDuration by ConfigProperty<Float>(configMap) // 1.0,
    var autoAnimateUnmatched by ConfigProperty<Boolean>(configMap) // true,

    // CSS properties that can be auto-animated. Position & scale
    // is matched separately so there's no need to include styles
    // like top/right/bottom/left, width/height or margin.
    var autoAnimateStyles by ConfigProperty<List<String>>(configMap)
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
    var autoSlideStoppable by ConfigProperty<Boolean>(configMap) // true,

    // Use this method for navigation when auto-sliding (defaults to navigateNext)
    // var autoSlideMethod: null, // TODO

    // Specify the average time in seconds that you think you will spend
    // presenting each slide. This is used to show a pacing timer in the
    // speaker view
    var defaultTiming by ConfigProperty<Int>(configMap) // null,

    // Enable slide navigation via mouse wheel
    var mouseWheel by ConfigProperty<Boolean>(configMap) // false,

    // Opens links in an iframe preview overlay
    // Add `data-preview-link` and `data-preview-link="false"` to customise each link
    // individually
    var previewLinks by ConfigProperty<Boolean>(configMap) // false,

    // Exposes the reveal.js API through window.postMessage
    var postMessage by ConfigProperty<Boolean>(configMap) // true,

    // Dispatches all reveal.js events to the parent window through postMessage
    var postMessageEvents by ConfigProperty<Boolean>(configMap) // false,

    // Focuses body when page changes visibility to ensure keyboard shortcuts work
    var focusBodyOnPageVisibilityChange by ConfigProperty<Boolean>(configMap) // true,

    // Transition style
    var transition by ConfigProperty<Transition>(configMap) //: 'slide', // none/fade/slide/convex/concave/zoom

    // Transition speed
    var transitionSpeed by ConfigProperty<Speed>(configMap) // 'default', // default/fast/slow

    // Transition style for full page slide backgrounds
    var backgroundTransition by ConfigProperty<Transition>(configMap) // 'fade', // none/fade/slide/convex/concave/zoom

    // The maximum number of pages a single slide can expand onto when printing
    // to PDF, unlimited by default
    var pdfMaxPagesPerSlide by ConfigProperty<Int>(configMap) // Number.POSITIVE_INFINITY,

    // Prints each fragment on a separate slide
    var pdfSeparateFragments by ConfigProperty<Boolean>(configMap) // true,

    // Offset used to reduce the height of content within exported PDF pages.
    // This exists to account for environment differences based on how you
    // print to PDF. CLI printing options, like phantomjs and wkpdf, can end
    // on precisely the total height of the document whereas in-browser
    // printing has to end one pixel before.
    var pdfPageHeightOffset by ConfigProperty<Int>(configMap) // -1,

    // Number of slides away from the current that are visible
    var viewDistance by ConfigProperty<Int>(configMap) // 3,

    // Number of slides away from the current that are visible on mobile
    // devices. It is advisable to set this to a lower number than
    // viewDistance in order to save resources.
    var mobileViewDistance by ConfigProperty<Int>(configMap) // 2,

    // The display mode that will be used to show slides
    var display by ConfigProperty<String>(configMap) // 'block',

    // Hide cursor if inactive
    var hideInactiveCursor by ConfigProperty<Boolean>(configMap) // true,

    // Time before the cursor is hidden (in ms)
    var hideCursorTime by ConfigProperty<Int>(configMap) // 5000

    fun toJS() =
        buildString {
            appendLine()
            appendLine("\tReveal.initialize({")
            configMap.forEach { (k, v) ->
                when (v) {
                    is Boolean, is Number -> append("\t\t$k: $v")
                    is String -> append("\t\t$k: '$v'")
                    is Transition -> append("\t\t$k: '${v.name.toLower()}'")
                    is Speed -> append("\t\t$k: '${v.name.toLower()}'")
                    is List<*> -> append("\t\t$k: [${v.joinToString(", ") { "'$it'" }}]")
                    else -> throw IllegalArgumentException("Invalid value for $k: $v")
                }
                appendLine(",")
            }
            if (configMap.isNotEmpty())
                appendLine()
            appendLine("\t\tplugins: [${plugins.joinToString(", ")}]")
            appendLine("\t});")
            appendLine()
        }
}

class ConfigProperty<T>(val configMap: MutableMap<String, Any>) {
    var configName = ""

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return if (configName.isNotEmpty() && configMap.containsKey(configName))
            configMap[configName] as T
        else
            throw IllegalStateException("Config property ${property.name} has not been set")
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        configName = property.name
        configMap[configName] = value as Any
    }

    override fun toString() = "$configName: ${configMap[configName]}"
}