package com.kslides.config

class CopyCodeConfig : AbstractConfig() {
  var copy by ConfigProperty<String>(revealjsManagedValues) // 'Copy'
  var copied by ConfigProperty<String>(revealjsManagedValues) // 'Copied'

  // The timeout is the time in milliseconds for the "Copied!"-state to revert back to "Copy"
  var timeout by ConfigProperty<Int>(revealjsManagedValues) // 1000

  var copybg by ConfigProperty<String>(revealjsManagedValues) // 'orange'
  var copiedbg by ConfigProperty<String>(revealjsManagedValues) // 'green'
  var copycolor by ConfigProperty<String>(revealjsManagedValues) // 'black'
  var copiedcolor by ConfigProperty<String>(revealjsManagedValues) // 'white'
}