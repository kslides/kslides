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
 * @param T the property type (any Kotlin type; cast is unchecked on read).
 * @param configMap the backing map, typically [AbstractConfig.revealjsManagedValues] or
 *   [AbstractConfig.kslidesManagedValues].
 */
class ConfigProperty<T>(
  private val configMap: MutableMap<String, Any>,
) {
  /** Name of the last property written through this delegate. Used by [toString] for debugging. */
  var configName = ""

  /**
   * @throws IllegalStateException if the property has not been assigned.
   */
  @Suppress("UNCHECKED_CAST")
  operator fun getValue(
    thisRef: Any?,
    property: KProperty<*>,
  ) = if (configMap.containsKey(property.name))
    configMap[property.name] as T
  else
    throw IllegalStateException("Config property ${property.name} has not been set")

  operator fun setValue(
    thisRef: Any?,
    property: KProperty<*>,
    value: T,
  ) {
    configName = property.name
    configMap[configName] = value as Any
  }

  override fun toString() = "$configName: ${configMap[configName]}"
}
