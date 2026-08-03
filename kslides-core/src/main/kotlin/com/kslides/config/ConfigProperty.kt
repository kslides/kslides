package com.kslides.config

import kotlin.reflect.KProperty

/**
 * Property delegate for kslides config options. Stores each assignment into the supplied
 * [configMap] keyed by the Kotlin property name, which lets the config cascade work by simple
 * map merge and lets [AbstractConfig] enumerate which options were actually set (rather than
 * emitting every reveal.js default into the generated JS).
 *
 * Reading an unset property throws — config instances that have not been seeded via
 * [AbstractConfig.merge] or explicit assignment will fail loudly rather than silently return a
 * default.
 *
 * Implementation detail of the config cascade — `internal`; consumers configure options through the
 * typed properties on each config class, not by constructing this delegate.
 *
 * @param T the property type (any Kotlin type; cast is unchecked on read).
 * @param configMap the backing map, one of [AbstractConfig]'s two internal value maps.
 * @param validator optional check run on every assignment, receiving the value and the property
 *   name. Used by properties whose values land verbatim in generated CSS (see [requireCssLength]),
 *   so a malformed value fails at the assignment site instead of corrupting the rendered page.
 *   [AbstractConfig.merge] copies map entries directly and bypasses this — safe, because the values
 *   it copies were validated where they were first assigned.
 */
internal class ConfigProperty<T>(
  private val configMap: MutableMap<String, Any>,
  private val validator: ((value: T, propertyName: String) -> Unit)? = null,
) {
  /**
   * @throws IllegalStateException if the property has not been assigned.
   */
  @Suppress("UNCHECKED_CAST")
  operator fun getValue(
    thisRef: Any?,
    property: KProperty<*>,
  ): T =
    // The backing map only ever holds non-null values, so a single lookup with `?:` distinguishes
    // "unset" from "set" without a second containsKey() probe.
    (
      configMap[property.name]
      ?: throw IllegalStateException("Config property ${property.name} has not been set")
    ) as T

  /**
   * @throws IllegalArgumentException if a [validator] was supplied and rejects [value].
   */
  operator fun setValue(
    thisRef: Any?,
    property: KProperty<*>,
    value: T,
  ) {
    validator?.invoke(value, property.name)
    configMap[property.name] = value as Any
  }
}
