package com.kslides.config

import com.kslides.KSlidesDslMarker

/**
 * iframe sizing and caching settings for `letsPlot{}` DSL calls. Merged with global +
 * presentation defaults at render time.
 *
 * The [width] / [height] here must stay compatible with the plot size passed to `letsPlot()`
 * (the `Dimensions` argument) — mismatched values will cause clipping or whitespace around
 * the plot.
 */
@KSlidesDslMarker
class LetsPlotIframeConfig : AbstractConfig() {
  /** iframe `width` attribute. Defaults to `"100%"` of the slide area. */
  var width by ConfigProperty<String>(kslidesManagedValues)

  /** iframe `height` attribute. Blank omits the attribute. */
  var height by ConfigProperty<String>(kslidesManagedValues)

  /** Inline CSS applied to the iframe. Useful for borders while tuning size. */
  var style by ConfigProperty<String>(kslidesManagedValues)

  /** Accessible title text for screen readers. */
  var title by ConfigProperty<String>(kslidesManagedValues)

  /**
   * When `true`, the rendered plot HTML is cached for the lifetime of the [com.kslides.KSlides]
   * instance so the same figure is not recomputed on every HTTP request. Set to `false` for
   * plots whose data changes over time.
   */
  var staticContent by ConfigProperty<Boolean>(kslidesManagedValues)

  internal fun assignDefaults() {
    width = "100%"
    height = ""
    style = ""
    title = ""
    staticContent = false
  }

  companion object {
    /** Convenience invoker so callers can build an ad-hoc config with `LetsPlotIframeConfig { ... }`. */
    inline operator fun invoke(
      action: LetsPlotIframeConfig.() -> Unit,
    ): LetsPlotIframeConfig = LetsPlotIframeConfig().apply(action)
  }
}
