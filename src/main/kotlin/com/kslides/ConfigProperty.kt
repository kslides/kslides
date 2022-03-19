package com.kslides

import kotlin.reflect.*

class ConfigProperty<T>(val configMap: MutableMap<String, Any>) {
  var configName = ""

  operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
    return if (configMap.containsKey(property.name))
      configMap[property.name] as T
    else
      throw IllegalStateException("Config property ${property.name} has not been set")
  }

  operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    configName = property.name
    configMap[configName] = value as Any
  }

  override fun toString() = "$configName: ${configMap[configName]}"
}