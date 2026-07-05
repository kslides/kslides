package com.kslides.config

import kotlinx.serialization.Serializable

@Serializable
internal data class CopyCodeValues(
  var button: CopyCodeButton? = null,
  var display: CopyCodeDisplay? = null,
  val text: Text = Text(),
  var plaintextonly: Boolean? = null,
  var timeout: Int? = null,
  val style: Style = Style(),
  var window: Boolean? = null,
  var tooltip: Boolean? = null,
//  var iconsvg: IconSvg = IconSvg(),
//  var cssautoload: Boolean? = null,
//  var csspath: String? = null
) {
  /** Copy every set (non-null) field from [other] on top of this instance, so a later config wins per field. */
  @Suppress("CyclomaticComplexMethod")
  fun mergeFrom(other: CopyCodeValues) {
    other.button?.let { button = it }
    other.display?.let { display = it }
    other.plaintextonly?.let { plaintextonly = it }
    other.timeout?.let { timeout = it }
    other.window?.let { window = it }
    other.tooltip?.let { tooltip = it }
    other.text.copy?.let { text.copy = it }
    other.text.copied?.let { text.copied = it }
    other.style.copybg?.let { style.copybg = it }
    other.style.copiedbg?.let { style.copiedbg = it }
    other.style.copycolor?.let { style.copycolor = it }
    other.style.copiedcolor?.let { style.copiedcolor = it }
    other.style.copyborder?.let { style.copyborder = it }
    other.style.copiedborder?.let { style.copiedborder = it }
    other.style.scale?.let { style.scale = it }
    other.style.offset?.let { style.offset = it }
    other.style.radius?.let { style.radius = it }
  }
}

@Serializable
internal data class Text(
  var copy: String? = null,
  var copied: String? = null,
)

@Serializable
internal data class Style(
  var copybg: String? = null,
  var copiedbg: String? = null,
  var copycolor: String? = null,
  var copiedcolor: String? = null,
  var copyborder: String? = null,
  var copiedborder: String? = null,
  var scale: Double? = null,
  var offset: Double? = null,
  var radius: Double? = null,
)

//@Serializable
//internal data class IconSvg(
//  var copy: String? = null,
//  var copied: String? = null
//)
