package com.kslides.config

import com.kslides.*

/**
 * Presentation-level configuration — mirrors the [reveal.js config options](https://revealjs.com/config/)
 * plus kslides-specific settings for theme, corner links, Google Analytics, and nested
 * sub-configs ([menuConfig], [copyCodeConfig], [slideConfig], [playgroundConfig],
 * [letsPlotIframeConfig], [diagramConfig]).
 *
 * Configured at three levels that merge in order: global → presentation → slide. The global
 * instance is seeded with kslides defaults via [assignDefaults]; subsequent presentation- and
 * slide-level instances only carry overrides and are merged on top.
 */
@KSlidesDslMarker
class PresentationConfig : AbstractConfig() {
  /**
   * "Normal" presentation width. Aspect ratio is preserved when scaling to the viewport. May be
   * an integer (pixels) or a percentage string. reveal.js default: 960.
   */
  var width by ConfigProperty<Any>(revealjsManagedValues)

  /**
   * "Normal" presentation height. Same notes as [width]. reveal.js default: 700.
   */
  var height by ConfigProperty<Any>(revealjsManagedValues)

  /** Fraction of the display size left empty around the content. reveal.js default: 0.04. */
  var margin by ConfigProperty<Float>(revealjsManagedValues)

  /** Lower bound on the scale factor applied to fit content in the viewport. */
  var minScale by ConfigProperty<Float>(revealjsManagedValues)

  /** Upper bound on the scale factor applied to fit content in the viewport. */
  var maxScale by ConfigProperty<Float>(revealjsManagedValues)

  /** Display the arrow controls for navigation. */
  var controls by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * Help the user learn the controls with hints (e.g. bouncing the down arrow the first time a
   * vertical stack is encountered).
   */
  var controlsTutorial by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Position of the navigation arrows. `"edges"` or `"bottom-right"`. */
  var controlsLayout by ConfigProperty<String>(revealjsManagedValues)

  /** Visibility of the backwards-navigation arrows. `"faded"`, `"hidden"`, or `"visible"`. */
  var controlsBackArrows by ConfigProperty<String>(revealjsManagedValues)

  /** Display the presentation progress bar. */
  var progress by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * Context in which the slide number is displayed. `"all"`, `"print"` (PDF only), or
   * `"speaker"` (speaker view only).
   */
  var showSlideNumber by ConfigProperty<String>(revealjsManagedValues)

  /** When `true`, `#` URL links use 1-based indexing to match the slide number. */
  var hashOneBasedIndex by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * When `true`, the current slide is appended to the URL hash so reloading the page or
   * copying the URL returns the viewer to the same slide.
   */
  var hash by ConfigProperty<Boolean>(revealjsManagedValues)

  /** When `true`, reveal.js watches the URL hash and navigates on changes. */
  var respondToHashChanges by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Push every slide change into browser history. Implies [hash] = `true`. */
  var history by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Enable keyboard shortcuts for navigation. */
  var keyboard by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * Optional keyboard-event gate. If set to `"focused"`, embedded decks only receive keyboard
   * events when they have focus.
   */
  var keyboardCondition by ConfigProperty<String>(revealjsManagedValues)

  /**
   * Disable reveal.js's default scaling and centering so that a custom CSS layout can take over.
   */
  var disableLayout by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Enable the slide overview (keyboard: `o` or `esc`). */
  var overview by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Vertically center slide content. */
  var center by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Enable touch navigation on touch-capable devices. */
  var touch by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Loop the presentation back to the first slide after the last. */
  var loop by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Right-to-left presentation direction. */
  var rtl by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * Navigation mode. One of:
   * - `"default"` — left/right steps horizontal, up/down steps vertical, space steps everything.
   * - `"linear"` — up/down arrows removed; left/right step through every slide.
   * - `"grid"` — stepping horizontally between vertical stacks preserves the vertical index.
   */
  var navigationMode by ConfigProperty<String>(revealjsManagedValues)

  /** Randomize slide order on each page load. */
  var shuffle by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Globally enable/disable fragments. */
  var fragments by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Include the current fragment index in the URL so reloads land on the same fragment. */
  var fragmentInURL by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Flag the presentation as running in an embedded (non-full-window) context. */
  var embedded by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Show a help overlay when the question-mark key is pressed. */
  var help by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Allow pausing the presentation (blackout) with the `b` / `.` keys. */
  var pause by ConfigProperty<Boolean>(revealjsManagedValues)

  /** When `true`, speaker notes are rendered inline for all viewers instead of in the speaker view. */
  var showNotes by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * Global override for autoplay on embedded media.
   * - default (unset) — autoplay only when `data-autoplay` is present on the element.
   * - `true` — every media element autoplays.
   * - `false` — no media autoplays, regardless of `data-autoplay`.
   */
  var autoPlayMedia by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * Global override for iframe lazy loading.
   * - default (unset) — iframes with `data-src` and `data-preload` load within [viewDistance];
   *   others load when visible.
   * - `true` — every `data-src` iframe loads within [viewDistance].
   * - `false` — every `data-src` iframe loads only when visible.
   */
  var preloadIframes by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Globally disable auto-animate transitions. Individual slides can still opt in via `data-auto-animate`. */
  var autoAnimate by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Optional custom element matcher used to decide which elements animate between slides. */
  var autoAnimateMatcher by ConfigProperty<String>(revealjsManagedValues)

  /** Default CSS easing function for auto-animate transitions. */
  var autoAnimateEasing by ConfigProperty<String>(revealjsManagedValues)

  /** Default duration (seconds) for auto-animate transitions. */
  var autoAnimateDuration by ConfigProperty<Float>(revealjsManagedValues)

  /** When `true`, elements not matched by auto-animate fade in/out. */
  var autoAnimateUnmatched by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * CSS properties auto-animate can transition. Position and scale are handled separately, so
   * `top`/`left`/`width`/`height`/`margin` are not needed here.
   */
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

  /** When `true`, user input halts auto-sliding until the next manual navigation. */
  var autoSlideStoppable by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * Typical time in seconds spent on each slide. Drives the pacing timer in the speaker view;
   * has no effect on actual navigation.
   */
  var defaultTiming by ConfigProperty<Int>(revealjsManagedValues)

  /** Enable slide navigation via the mouse wheel. */
  var mouseWheel by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * When `true`, links open in an iframe preview overlay. Individual links can opt out with
   * `data-preview-link="false"`.
   */
  var previewLinks by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Expose the reveal.js API via `window.postMessage`. */
  var postMessage by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Dispatch every reveal.js event to the parent window via `postMessage`. */
  var postMessageEvents by ConfigProperty<Boolean>(revealjsManagedValues)

  /** When `true`, focus the document body on visibility changes so keyboard shortcuts work. */
  var focusBodyOnPageVisibilityChange by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Default slide-transition style (see [Transition]). */
  var transition by ConfigProperty<Transition>(revealjsManagedValues)

  /** Default slide-transition speed (see [Speed]). */
  var transitionSpeed by ConfigProperty<Speed>(revealjsManagedValues)

  /** Default transition style for full-page slide backgrounds. */
  var backgroundTransition by ConfigProperty<Transition>(revealjsManagedValues)

  /** Maximum number of pages a single slide can span when printing to PDF (unlimited by default). */
  var pdfMaxPagesPerSlide by ConfigProperty<Int>(revealjsManagedValues)

  /** When `true`, each fragment is printed on a separate PDF page. */
  var pdfSeparateFragments by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * Pixel offset subtracted from each exported PDF page's content height to avoid clipping in
   * specific PDF renderers (CLI tools like phantomjs and wkpdf tend to render one pixel differently
   * from in-browser printing).
   */
  var pdfPageHeightOffset by ConfigProperty<Int>(revealjsManagedValues)

  /** Number of slides in each direction that remain in the DOM for smooth transitions. */
  var viewDistance by ConfigProperty<Int>(revealjsManagedValues)

  /** Like [viewDistance] but for mobile devices; typically lower to conserve resources. */
  var mobileViewDistance by ConfigProperty<Int>(revealjsManagedValues)

  /** CSS `display` value used when showing slides. */
  var display by ConfigProperty<String>(revealjsManagedValues)

  /** Hide the mouse cursor when it is inactive. */
  var hideInactiveCursor by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Milliseconds of inactivity before the cursor is hidden (requires [hideInactiveCursor]). */
  var hideCursorTime by ConfigProperty<Int>(revealjsManagedValues)

  /** Browser tab title. Blank omits the `<title>` element. */
  var title by ConfigProperty<String>(kslidesManagedValues)

  /** reveal.js theme. */
  var theme by ConfigProperty<PresentationTheme>(kslidesManagedValues)

  /** Code-highlighting theme (requires [enableHighlight]). */
  var highlight by ConfigProperty<Highlight>(kslidesManagedValues)

  /** Load the reveal.js speaker-notes plugin. */
  var enableSpeakerNotes by ConfigProperty<Boolean>(kslidesManagedValues)

  /** Load the reveal.js zoom plugin (keyboard: alt+click). */
  var enableZoom by ConfigProperty<Boolean>(kslidesManagedValues)

  /** Load the reveal.js search plugin (keyboard: `ctrl+shift+f`). */
  var enableSearch by ConfigProperty<Boolean>(kslidesManagedValues)

  /** Load the reveal.js Markdown plugin (required for [com.kslides.slide.MarkdownSlide]). */
  var enableMarkdown by ConfigProperty<Boolean>(kslidesManagedValues)

  /** Load the reveal.js highlight.js plugin for syntax-highlighted code blocks. */
  var enableHighlight by ConfigProperty<Boolean>(kslidesManagedValues)

  /** Load the math plugin with KaTeX support. */
  var enableMathKatex by ConfigProperty<Boolean>(kslidesManagedValues)

  /** Load the math plugin with MathJax 2 support. */
  var enableMathJax2 by ConfigProperty<Boolean>(kslidesManagedValues)

  /** Load the math plugin with MathJax 3 support. */
  var enableMathJax3 by ConfigProperty<Boolean>(kslidesManagedValues)

  /** Load the CopyCode plugin (adds a copy button to code blocks). See [CopyCodeConfig]. */
  var enableCodeCopy by ConfigProperty<Boolean>(kslidesManagedValues)

  /** Load the reveal.js-menu plugin. See [MenuConfig]. */
  var enableMenu by ConfigProperty<Boolean>(kslidesManagedValues)

  /** URL for the top-left corner link. Blank hides the corner element. */
  var topLeftHref by ConfigProperty<String>(kslidesManagedValues)

  /** `<a target>` value for the top-left corner link. */
  var topLeftTarget by ConfigProperty<HrefTarget>(kslidesManagedValues)

  /** Tooltip / screen-reader title for the top-left corner link. */
  var topLeftTitle by ConfigProperty<String>(kslidesManagedValues)

  /** Inline SVG rendered inside the top-left corner link. Mutually useful with [topLeftSvgSrc]. */
  var topLeftSvg by ConfigProperty<String>(kslidesManagedValues)

  /** URL of an image to display in the top-left corner link (alternative to [topLeftSvg]). */
  var topLeftSvgSrc by ConfigProperty<String>(kslidesManagedValues)

  /** CSS class on the top-left corner `<img>`/SVG. */
  var topLeftSvgClass by ConfigProperty<String>(kslidesManagedValues)

  /** Inline CSS on the top-left corner `<img>`/SVG. */
  var topLeftSvgStyle by ConfigProperty<String>(kslidesManagedValues)

  /** Text rendered inside the top-left corner link (after any SVG). */
  var topLeftText by ConfigProperty<String>(kslidesManagedValues)

  /** URL for the top-right corner link. Blank hides the corner element. */
  var topRightHref by ConfigProperty<String>(kslidesManagedValues)

  /** `<a target>` value for the top-right corner link. */
  var topRightTarget by ConfigProperty<HrefTarget>(kslidesManagedValues)

  /** Tooltip / screen-reader title for the top-right corner link. */
  var topRightTitle by ConfigProperty<String>(kslidesManagedValues)

  /** Inline SVG rendered inside the top-right corner link. */
  var topRightSvg by ConfigProperty<String>(kslidesManagedValues)

  /** URL of an image for the top-right corner link. */
  var topRightSvgSrc by ConfigProperty<String>(kslidesManagedValues)

  /** CSS class on the top-right corner `<img>`/SVG. */
  var topRightSvgClass by ConfigProperty<String>(kslidesManagedValues)

  /** Inline CSS on the top-right corner `<img>`/SVG. */
  var topRightSvgStyle by ConfigProperty<String>(kslidesManagedValues)

  /** Text rendered inside the top-right corner link (after any SVG). */
  var topRightText by ConfigProperty<String>(kslidesManagedValues)

  /** Google Analytics property id. When non-blank, injects the `gtag.js` loader and config. */
  var gaPropertyId by ConfigProperty<String>(kslidesManagedValues)

  /**
   * Auto-slide behavior.
   * - `0` — auto-slide only on slides/fragments marked with `data-autoslide`.
   * - `n > 0` — advance every `n` milliseconds.
   * - `false` — never auto-slide.
   */
  var autoSlide by ConfigProperty<Any>(kslidesManagedValues)

  /**
   * Slide-number display.
   * - `true` — show using the default format.
   * - `false` — hide.
   * - Format string — `"h.v"`, `"h/v"`, `"c"`, `"c/t"` (see reveal.js docs).
   */
  var slideNumber by ConfigProperty<Any>(kslidesManagedValues)

  /** Jump-to-slide UI (reveal.js 4.5+). Allows typing a slide number to jump directly. */
  var jumpToSlide by ConfigProperty<Boolean>(kslidesManagedValues)

  /** Presentation view mode (reveal.js 5.0+): classic deck vs scrolling document. */
  var view by ConfigProperty<ViewType>(kslidesManagedValues)

  /** Scroll-view layout (reveal.js 5.0+). */
  var scrollLayout by ConfigProperty<ScrollLayout>(kslidesManagedValues)

  /**
   * Scroll-progress indicator in scroll view.
   * - `ScrollProgress.AUTO` — show while scrolling, hide when idle.
   * - `true` — always show.
   * - `false` — never show.
   */
  var scrollProgress by ConfigProperty<Any>(kslidesManagedValues)

  /** Minimum viewport width in pixels before switching to scroll view. */
  var scrollActivationWidth by ConfigProperty<Int>(kslidesManagedValues)

  /**
   * Scroll-snap behavior (reveal.js 5.0+).
   * - `false` — continuous scrolling.
   * - `ScrollSnap.PROXIMITY` — snap when close to a slide boundary.
   * - `ScrollSnap.MANDATORY` — always snap to the nearest boundary.
   */
  var scrollSnap by ConfigProperty<Any>(kslidesManagedValues)

  internal val menuConfig = MenuConfig()
  internal val copyCodeConfig = CopyCodeConfig()
  internal val slideConfig = SlideConfig()
  internal val playgroundConfig = PlaygroundConfig()
  internal val letsPlotIframeConfig = LetsPlotIframeConfig()
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
    topRightTitle = ""
    topRightSvg = ""
    topRightSvgSrc = ""
    topRightSvgClass = "top-right-svg"
    topRightSvgStyle = ""
    topRightText = ""

    gaPropertyId = ""

    autoSlide = 0
    slideNumber = false

    jumpToSlide = true

    view = ViewType.DEFAULT
    scrollLayout = ScrollLayout.FULL
    scrollProgress = ScrollProgress.AUTO
    scrollActivationWidth = 1
    scrollSnap = ScrollSnap.MANDATORY

    slideConfig.assignDefaults()
    playgroundConfig.assignDefaults()
    letsPlotIframeConfig.assignDefaults()
    diagramConfig.assignDefaults()
  }

  internal fun mergeConfig(other: PresentationConfig) {
    this.merge(other)
    this.menuConfig.merge(other.menuConfig)
    this.copyCodeConfig.merge(other.copyCodeConfig)
    this.slideConfig.merge(other.slideConfig)
    this.playgroundConfig.merge(other.playgroundConfig)
    this.letsPlotIframeConfig.merge(other.letsPlotIframeConfig)
    this.diagramConfig.merge(other.diagramConfig)
  }

  /** Configure the reveal.js-menu plugin. Only takes effect when [enableMenu] is `true`. */
  fun menuConfig(block: MenuConfig.() -> Unit) = menuConfig.block()

  /** Configure the CopyCode plugin. Only takes effect when [enableCodeCopy] is `true`. */
  fun copyCodeConfig(block: CopyCodeConfig.() -> Unit) = copyCodeConfig.block()

  /** Configure the default [SlideConfig] values applied to every slide in the scope. */
  fun slideConfig(block: SlideConfig.() -> Unit) = slideConfig.block()

  /** Configure defaults for [com.kslides.playground] iframes. */
  fun playgroundConfig(block: PlaygroundConfig.() -> Unit) = playgroundConfig.block()

  /** Configure defaults for `letsPlot{}` iframes (from the `kslides-letsplot` module). */
  fun letsPlotIframeConfig(block: LetsPlotIframeConfig.() -> Unit) = letsPlotIframeConfig.block()

  /** Configure defaults for [com.kslides.diagram] Kroki images. */
  fun diagramConfig(block: DiagramConfig.() -> Unit) = diagramConfig.block()
}
