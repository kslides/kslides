package com.kslides.config

import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

/**
 * Delegates a [CopyCodeConfig] property straight to its backing field on a [CopyCodeValues]
 * instance. The [ref] property reference makes the property/field type match compiler-enforced, so
 * there is no name-keyed dispatch and no unchecked cast — the field and the exposed property cannot
 * drift apart. Reading a property that was never assigned throws, matching the other config
 * delegates (see [ConfigProperty]).
 */
internal class CopyCodeField<T : Any>(
  private val ref: KMutableProperty0<T?>,
) {
  /**
   * @throws IllegalStateException if the property has not been assigned.
   */
  operator fun getValue(
    thisRef: Any?,
    property: KProperty<*>,
  ): T = ref.get() ?: error("CopyCode property ${property.name} has not been set")

  operator fun setValue(
    thisRef: Any?,
    property: KProperty<*>,
    value: T,
  ) = ref.set(value)
}
