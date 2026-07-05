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
  /** Set to "always" by default. Can be set to "hover" to only show the button on hover, or "false" to disable the button entirely. */
  var button by CopyCodeProperty<CopyCodeButton>(revealjsManagedValues)

  /** The copy buttons display only text by default, but this setting can be changed to "icons" or "both". */
  var display by CopyCodeProperty<CopyCodeDisplay>(revealjsManagedValues)

  /** Milliseconds the "Copied!" label is displayed before reverting to [copy]. Default 1000. */
  var timeout by CopyCodeProperty<Int>(revealjsManagedValues)

  /** Set this to false to allow copying of rich text and styles. Default true. */
  var plaintextonly by CopyCodeProperty<Boolean>(revealjsManagedValues)

  /** Default button label (e.g. `"Copy"`). */
  var copy by CopyCodeProperty<String>(revealjsManagedValues)

  /** Label shown immediately after a successful copy (e.g. `"Copied"`). */
  var copied by CopyCodeProperty<String>(revealjsManagedValues)

  /** Background color of the button in its default state. */
  var copybg by CopyCodeProperty<String>(revealjsManagedValues)

  /** Background color of the button after a successful copy. */
  var copiedbg by CopyCodeProperty<String>(revealjsManagedValues)

  /** Text color of the button in its default state. */
  var copycolor by CopyCodeProperty<String>(revealjsManagedValues)

  /** Text color of the button after a successful copy. */
  var copiedcolor by CopyCodeProperty<String>(revealjsManagedValues)

  /** A CSS 'border' rule. Can be, for example "1px solid gray". */
  var copyborder by CopyCodeProperty<String>(revealjsManagedValues)

  /** A CSS 'border' rule. Can be, for example "1px solid green". */
  var copiedborder by CopyCodeProperty<String>(revealjsManagedValues)

  /** The scale of the buttons and window elements. */
  var scale by CopyCodeProperty<Int>(revealjsManagedValues)

  /** The offset (in em) from the top and the right. */
  var offset by CopyCodeProperty<Int>(revealjsManagedValues)

  /** The border-radius (in em) of the buttons. */
  var radius by CopyCodeProperty<Int>(revealjsManagedValues)

  /** Controls whether code blocks are displayed in a macOS-style window frame. Default false. */
  var window by CopyCodeProperty<Boolean>(revealjsManagedValues)

  /** Show a tooltip at the Copied state, for the icons-only display version. */
  var tooltip by CopyCodeProperty<Boolean>(revealjsManagedValues)
}
