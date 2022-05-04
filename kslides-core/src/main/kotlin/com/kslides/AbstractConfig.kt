package com.kslides

abstract class AbstractConfig {
  val unmanagedValues = mutableMapOf<String, Any>()
  val managedValues = mutableMapOf<String, Any>()

  fun merge(other: AbstractConfig) {
    unmanagedValues.putAll(other.unmanagedValues)
    managedValues.putAll(other.managedValues)
  }
}