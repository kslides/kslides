package com.kslides

abstract class AbstractConfig {
  val presentationVals = mutableMapOf<String, Any>()
  val revealVals = mutableMapOf<String, Any>()

  fun combine(other: AbstractConfig) {
    presentationVals.putAll(other.presentationVals)
    revealVals.putAll(other.revealVals)
  }
}