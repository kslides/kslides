package com.kslides.config

/**
 * Base class for all configuration types. Keeps reveal.js-native options and kslides-native
 * options in two separate maps so they can be serialized differently — reveal.js values are
 * emitted into the `Reveal.initialize({...})` call as-is; kslides values drive HTML generation
 * (page head, corner links, etc.) before ever reaching reveal.js.
 *
 * Subclasses declare properties via [ConfigProperty] delegated to one of these maps; the config
 * cascade ([merge]) then combines parent and child maps key-by-key.
 */
abstract class AbstractConfig {
  /** Values that correspond to documented [reveal.js config options](https://revealjs.com/config/). */
  val revealjsManagedValues = mutableMapOf<String, Any>()

  /** Values consumed by kslides itself (theme, highlight plugin, corner links, iframe sizing, etc.). */
  val kslidesManagedValues = mutableMapOf<String, Any>()

  /** Copy all entries from [other]'s maps into this config's maps. Later puts overwrite earlier ones. */
  fun merge(other: AbstractConfig) {
    revealjsManagedValues.putAll(other.revealjsManagedValues)
    kslidesManagedValues.putAll(other.kslidesManagedValues)
  }
}