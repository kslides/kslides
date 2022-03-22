package com.kslides

abstract class AbstractConfig {
  val primaryValues = mutableMapOf<String, Any>()
  val secondaryValues = mutableMapOf<String, Any>()

  fun combine(other: AbstractConfig) {
    primaryValues.putAll(other.primaryValues)
    secondaryValues.putAll(other.secondaryValues)
  }
}