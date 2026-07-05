package com.kslides.config

import kotlin.reflect.KProperty

internal typealias CopyCodeAssignment = CopyCodeValues.() -> Unit

internal class CopyCodeProperty<T>(
  private val configMap: MutableMap<String, Any>,
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
    (configMap[property.name] ?: error("CopyCode property ${property.name} has not been set")) as T

  @Suppress("CyclomaticComplexMethod")
  operator fun setValue(
    thisRef: Any?,
    property: KProperty<*>,
    value: T,
  ) {
    val lambda: CopyCodeAssignment = {
      when (property.name) {
        CopyCodeConfig::button.name -> button = value as CopyCodeButton
        CopyCodeConfig::display.name -> display = value as CopyCodeDisplay
        CopyCodeConfig::copy.name -> text.copy = value as String
        CopyCodeConfig::copied.name -> text.copied = value as String
        CopyCodeConfig::timeout.name -> timeout = value as Int
        CopyCodeConfig::plaintextonly.name -> plaintextonly = value as Boolean
        CopyCodeConfig::copybg.name -> style.copybg = value as String
        CopyCodeConfig::copiedbg.name -> style.copiedbg = value as String
        CopyCodeConfig::copycolor.name -> style.copycolor = value as String
        CopyCodeConfig::copiedcolor.name -> style.copiedcolor = value as String
        CopyCodeConfig::copyborder.name -> style.copyborder = value as String
        CopyCodeConfig::copiedborder.name -> style.copiedborder = value as String
        CopyCodeConfig::scale.name -> style.scale = value as Int
        CopyCodeConfig::offset.name -> style.offset = value as Int
        CopyCodeConfig::radius.name -> style.radius = value as Int
        CopyCodeConfig::window.name -> window = value as Boolean
        CopyCodeConfig::tooltip.name -> tooltip = value as Boolean
        else -> error("CopyCode property ${property.name} is not supported")
      }
    }
    configMap[property.name] = lambda
  }
}
