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
) {
  /** Copy every set (non-null) field from [other] on top of this instance, so a later config wins per field. */
  fun mergeFrom(other: CopyCodeValues) {
    other.button?.let { button = it }
    other.display?.let { display = it }
    other.plaintextonly?.let { plaintextonly = it }
    other.timeout?.let { timeout = it }
    other.window?.let { window = it }
    other.tooltip?.let { tooltip = it }
    text.mergeFrom(other.text)
    style.mergeFrom(other.style)
  }
}

@Serializable
internal data class Text(
  var copy: String? = null,
  var copied: String? = null,
) {
  fun mergeFrom(other: Text) {
    other.copy?.let { copy = it }
    other.copied?.let { copied = it }
  }
}

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
) {
  fun mergeFrom(other: Style) {
    other.copybg?.let { copybg = it }
    other.copiedbg?.let { copiedbg = it }
    other.copycolor?.let { copycolor = it }
    other.copiedcolor?.let { copiedcolor = it }
    other.copyborder?.let { copyborder = it }
    other.copiedborder?.let { copiedborder = it }
    other.scale?.let { scale = it }
    other.offset?.let { offset = it }
    other.radius?.let { radius = it }
  }
}
