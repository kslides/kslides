package com.kslides.config

import com.kslides.KSlidesDslMarker
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CopyCodeButton {
  @SerialName("always")
  ALWAYS,

  @SerialName("hover")
  HOVER,

  @SerialName("false")
  FALSE,
}

@Serializable
enum class CopyCodeDisplay {
  @SerialName("text")
  TEXT,

  @SerialName("icons")
  ICONS,

  @SerialName("both")
  BOTH,
}

/**
 * Configuration for the [CopyCode reveal.js plugin](https://github.com/Martinomagnifico/reveal.js-copycode).
 * Only takes effect when [PresentationConfig.enableCodeCopy] is `true`.
 */
@KSlidesDslMarker
class CopyCodeConfig : AbstractConfig() {
  /**
   * Not supported: [CopyCodeConfig] exposes every copycode option as a typed property and serializes
   * them via a strict schema, so there is no place for an untyped raw value. Use the typed properties
   * instead.
   */
  @Deprecated(
    "copyCodeConfig exposes all options as typed properties; revealjsOption is not supported here.",
    level = DeprecationLevel.ERROR,
  )
  override fun revealjsOption(
    key: String,
    value: Any,
  ): Nothing = error("copyCodeConfig does not support revealjsOption; use the typed properties")

  /** The typed, serializable backing for every copycode option. Each property below delegates to a field here. */
  internal val values = CopyCodeValues()

  /** Set to "always" by default. Can be set to "hover" to only show the button on hover, or "false" to disable the button entirely. */
  var button: CopyCodeButton by CopyCodeField(values::button)

  /** The copy buttons display only text by default, but this setting can be changed to "icons" or "both". */
  var display: CopyCodeDisplay by CopyCodeField(values::display)

  /** Milliseconds the "Copied!" label is displayed before reverting to [copy]. Default 1000. */
  var timeout: Int by CopyCodeField(values::timeout)

  /** Set this to false to allow copying of rich text and styles. Default true. */
  var plaintextonly: Boolean by CopyCodeField(values::plaintextonly)

  /** Default button label (e.g. `"Copy"`). */
  var copy: String by CopyCodeField(values.text::copy)

  /** Label shown immediately after a successful copy (e.g. `"Copied"`). */
  var copied: String by CopyCodeField(values.text::copied)

  /** Background color of the button in its default state. */
  var copybg: String by CopyCodeField(values.style::copybg)

  /** Background color of the button after a successful copy. */
  var copiedbg: String by CopyCodeField(values.style::copiedbg)

  /** Text color of the button in its default state. */
  var copycolor: String by CopyCodeField(values.style::copycolor)

  /** Text color of the button after a successful copy. */
  var copiedcolor: String by CopyCodeField(values.style::copiedcolor)

  /** A CSS 'border' rule. Can be, for example "1px solid gray". */
  var copyborder: String by CopyCodeField(values.style::copyborder)

  /** A CSS 'border' rule. Can be, for example "1px solid green". */
  var copiedborder: String by CopyCodeField(values.style::copiedborder)

  /** The scale of the buttons and window elements. */
  var scale: Double by CopyCodeField(values.style::scale)

  /** The offset (in em) from the top and the right. */
  var offset: Double by CopyCodeField(values.style::offset)

  /** The border-radius (in em) of the buttons. */
  var radius: Double by CopyCodeField(values.style::radius)

  /** Controls whether code blocks are displayed in a macOS-style window frame. Default false. */
  var window: Boolean by CopyCodeField(values::window)

  /** Show a tooltip at the Copied state, for the icons-only display version. */
  var tooltip: Boolean by CopyCodeField(values::tooltip)

  /** Merge the copycode values from [other] on top of this config, so a later config wins per field. */
  override fun merge(other: AbstractConfig) {
    super.merge(other)
    if (other is CopyCodeConfig) values.mergeFrom(other.values)
  }
}
