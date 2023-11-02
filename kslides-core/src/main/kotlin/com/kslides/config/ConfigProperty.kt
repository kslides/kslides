package com.kslides.config

import kotlin.reflect.KProperty

class ConfigProperty<T>(private val configMap: MutableMap<String, Any>) {
  var configName = ""

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
