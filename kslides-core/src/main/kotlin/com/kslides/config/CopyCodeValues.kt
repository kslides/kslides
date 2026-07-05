package com.kslides.config

import kotlinx.serialization.Serializable

@Serializable
internal data class CopyCodeValues(
  var button: CopyCodeButton? = null,
  var display: CopyCodeDisplay? = null,
  var text: Text = Text(),
  var plaintextonly: Boolean? = null,
  var timeout: Int? = null,
  var style: Style = Style(),
  var window: Boolean? = null,
  var tooltip: Boolean? = null,
//  var iconsvg: IconSvg = IconSvg(),
//  var cssautoload: Boolean? = null,
//  var csspath: String? = null
)

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
  var scale: Int? = null,
  var offset: Int? = null,
  var radius: Int? = null,
)

//@Serializable
//internal data class IconSvg(
//  var copy: String? = null,
//  var copied: String? = null
//)
