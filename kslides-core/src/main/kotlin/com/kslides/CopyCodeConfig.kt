package com.kslides

class CopyCodeConfig : AbstractConfig() {

  var copy by ConfigProperty<String>(unmanagedValues) // 'Copy'
  var copied by ConfigProperty<String>(unmanagedValues) // 'Copied'

  // The timeout is the time in milliseconds for the "Copied!"-state to revert back to "Copy"
  var timeout by ConfigProperty<Int>(unmanagedValues) // 1000

  var copybg by ConfigProperty<String>(unmanagedValues) // 'orange'
  var copiedbg by ConfigProperty<String>(unmanagedValues) // 'green'
  var copycolor by ConfigProperty<String>(unmanagedValues) // 'black'
  var copiedcolor by ConfigProperty<String>(unmanagedValues) // 'white'
}