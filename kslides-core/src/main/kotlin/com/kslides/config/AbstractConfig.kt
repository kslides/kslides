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

  /**
   * Merge [other] into this config **in place**: every entry from [other]'s two maps is copied into
   * this config's maps, with later puts overwriting earlier keys. This is how the global →
   * presentation → slide cascade resolves (parent merged first, then child).
   *
   * Values are copied by reference — a collection-valued option (e.g. a `Map`/`List`) is shared with
   * [other], not deep-copied. That is safe because config collection values are only ever replaced,
   * never mutated in place. Note this is distinct from (and name-collides with) the `Map.merge`
   * extension elsewhere; this one mutates the receiver rather than returning a new map.
   */
  fun merge(other: AbstractConfig) {
    revealjsManagedValues.putAll(other.revealjsManagedValues)
    kslidesManagedValues.putAll(other.kslidesManagedValues)
  }
}
