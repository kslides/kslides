package com.kslides.config

import com.kslides.DiagramOutputType
import com.kslides.KSlidesDslMarker

/**
 * Rendering and sizing options for Kroki diagrams. Instantiated both as a standalone presentation
 * default and as the superclass of [com.kslides.DiagramDescription] used inside
 * [com.kslides.diagram] blocks.
 */
@KSlidesDslMarker
open class DiagramConfig : AbstractConfig() {
  /** Image format requested from Kroki. Defaults to [DiagramOutputType.SVG]. */
  var outputType by ConfigProperty<DiagramOutputType>(kslidesManagedValues)

  /** `width` attribute on the emitted `<img>`. Blank omits the attribute. */
  var width by ConfigProperty<String>(kslidesManagedValues)

  /** `height` attribute on the emitted `<img>`. Blank omits the attribute. */
  var height by ConfigProperty<String>(kslidesManagedValues)

  /** Inline CSS applied to the emitted `<img>`. */
  var style by ConfigProperty<String>(kslidesManagedValues)

  /** Accessible title text (shown as tooltip and read by screen readers). */
  var title by ConfigProperty<String>(kslidesManagedValues)

  /**
   * Per-diagram-type options forwarded to Kroki as `diagram_options`. See the
   * [Kroki docs](https://docs.kroki.io/kroki/setup/diagram-options/) for the keys each diagram
   * type supports.
   *
   * Note: like every [ConfigProperty], this **replaces** (does not deep-merge) across the config
   * cascade — a per-diagram `options` map fully overrides the presentation/global one rather than
   * adding to it. Set the complete map at the level you want it to take effect.
   */
  var options by ConfigProperty<Map<String, Any>>(kslidesManagedValues)

  internal fun assignDefaults() {
    outputType = DiagramOutputType.SVG
    width = ""
    height = ""
    style = ""
    title = ""
    options = emptyMap()
  }
}
