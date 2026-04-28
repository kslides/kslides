package com.kslides.config

import com.kslides.KSlidesDslMarker

/**
 * Configuration for the [CopyCode reveal.js plugin](https://github.com/Martinomagnifico/reveal.js-copycode).
 * Only takes effect when [PresentationConfig.enableCodeCopy] is `true`.
 */
@KSlidesDslMarker
class CopyCodeConfig : AbstractConfig() {
  /** Default button label (e.g. `"Copy"`). */
  var copy by ConfigProperty<String>(revealjsManagedValues)

  /** Label shown immediately after a successful copy (e.g. `"Copied"`). */
  var copied by ConfigProperty<String>(revealjsManagedValues)

  /** Milliseconds the "Copied!" label is displayed before reverting to [copy]. Default 1000. */
  var timeout by ConfigProperty<Int>(revealjsManagedValues)

  /** Background color of the button in its default state. */
  var copybg by ConfigProperty<String>(revealjsManagedValues)

  /** Background color of the button after a successful copy. */
  var copiedbg by ConfigProperty<String>(revealjsManagedValues)

  /** Text color of the button in its default state. */
  var copycolor by ConfigProperty<String>(revealjsManagedValues)

  /** Text color of the button after a successful copy. */
  var copiedcolor by ConfigProperty<String>(revealjsManagedValues)
}