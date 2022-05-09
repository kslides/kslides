package com.kslides.config

abstract class AbstractConfig {
  val revealjsManagedValues = mutableMapOf<String, Any>()
  val kslidesManagedValues = mutableMapOf<String, Any>()

  fun merge(other: AbstractConfig) {
    revealjsManagedValues.putAll(other.revealjsManagedValues)
    kslidesManagedValues.putAll(other.kslidesManagedValues)
  }
}