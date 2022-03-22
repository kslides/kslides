package com.kslides

class MenuConfig : AbstractConfig() {

  // Specifies which side of the presentation the menu will
  // be shown. Use 'left' or 'right'.
  var side by ConfigProperty<String>(primaryValues) // 'left'

  // Specifies the width of the menu.
  // Can be one of the following:
  // 'normal', 'wide', 'third', 'half', 'full', or
  // any valid css length value
  var width by ConfigProperty<String>(primaryValues) // 'normal',

  // Add slide numbers to the titles in the slide list.
  // Use 'true' or format string (same as reveal.js slide numbers)
  var numbers by ConfigProperty<Boolean>(primaryValues) // false,

  // Specifies which slide elements will be used for generating
  // the slide titles in the menu. The default selects the first
  // heading element found in the slide, but you can specify any
  // valid css selector and the text from the first matching
  // element will be used.
  // Note: that a section data-menu-title attribute or an element
  // with a menu-title class will take precedence over this option
  var titleSelector by ConfigProperty<String>(primaryValues) // 'h1, h2, h3, h4, h5, h6',

  // If slides do not have a matching title, attempt to use the
  // start of the text content as the title instead
  var useTextContentForMissingTitles by ConfigProperty<Boolean>(primaryValues) // false,

  // Hide slides from the menu that do not have a title.
  // Set to 'true' to only list slides with titles.
  var hideMissingTitles by ConfigProperty<Boolean>(primaryValues) // false,

  // Adds markers to the slide titles to indicate the
  // progress through the presentation. Set to 'false'
  // to hide the markers.
  var markers by ConfigProperty<Boolean>(primaryValues) // true,

  // Specify custom panels to be included in the menu, by
  // providing an array of objects with 'title', 'icon'
  // properties, and either a 'src' or 'content' property.
  var custom by ConfigProperty<Boolean>(primaryValues) // false,

  // Specifies the themes that will be available in the themes
  // menu panel. Set to 'true' to show the themes menu panel
  // with the default themes list. Alternatively, provide an
  // array to specify the themes to make available in the
  // themes menu panel, for example...
  //
  // [
  //     { name: 'Black', theme: 'dist/theme/black.css' },
  //     { name: 'White', theme: 'dist/theme/white.css' },
  //     { name: 'League', theme: 'dist/theme/league.css' },
  //     {
  //       name: 'Dark',
  //       theme: 'lib/reveal.js/dist/theme/black.css',
  //       highlightTheme: 'lib/reveal.js/plugin/highlight/monokai.css'
  //     },
  //     {
  //       name: 'Code: Zenburn',
  //       highlightTheme: 'lib/reveal.js/plugin/highlight/zenburn.css'
  //     }
  // ]
  //
  // Note: specifying highlightTheme without a theme will
  // change the code highlight theme while leaving the
  // presentation theme unchanged.
  var themes by ConfigProperty<Boolean>(primaryValues) // true,

  // Specifies the path to the default theme files. If your
  // presentation uses a different path to the standard reveal
  // layout then you need to provide this option, but only
  // when 'themes' is set to 'true'. If you provide your own
  // list of themes or 'themes' is set to 'false' the
  // 'themesPath' option is ignored.
  var themesPath by ConfigProperty<String>(primaryValues) // 'dist/theme/',

  // Specifies if the transitions menu panel will be shown.
  // Set to 'true' to show the transitions menu panel with
  // the default transitions list. Alternatively, provide an
  // array to specify the transitions to make available in
  // the transitions panel, for example...
  // ['None', 'Fade', 'Slide']
  var transitions by ConfigProperty<Boolean>(primaryValues) // false,

  // Adds a menu button to the slides to open the menu panel.
// Set to 'false' to hide the button.
  var openButton by ConfigProperty<Boolean>(primaryValues) // true,

  // If 'true' allows the slide number in the presentation to
  // open the menu panel. The reveal.js slideNumber option must
  // be displayed for this to take effect
  var openSlideNumber by ConfigProperty<Boolean>(primaryValues) // false,

  // If true allows the user to open and navigate the menu using
  // the keyboard. Standard keyboard interaction with reveal
  // will be disabled while the menu is open.
  var keyboard by ConfigProperty<Boolean>(primaryValues) // true,

  // Normally the menu will close on user actions such as
  // selecting a menu item, or clicking the presentation area.
  // If 'true', the sticky option will leave the menu open
  // until it is explicitly closed, that is, using the close
  // button or pressing the ESC or m key (when the keyboard
  // interaction option is enabled).
  var sticky by ConfigProperty<Boolean>(primaryValues) // false,

  // If 'true' standard menu items will be automatically opened
  // when navigating using the keyboard. Note: this only takes
  // effect when both the 'keyboard' and 'sticky' options are enabled.
  var autoOpen by ConfigProperty<Boolean>(primaryValues) // true,

  // If 'true' the menu will not be created until it is explicitly
  // requested by calling RevealMenu.init(). Note this will delay
  // the creation of all menu panels, including custom panels, and
  // the menu button.
  var delayInit by ConfigProperty<Boolean>(primaryValues) // false,

  // If 'true' the menu will be shown when the menu is initialised.
  var openOnInit by ConfigProperty<Boolean>(primaryValues) // false,

  // By default the menu will load it's own font-awesome library
  // icons. If your presentation needs to load a different
  // font-awesome library the 'loadIcons' option can be set to false
  // and the menu will not attempt to load the font-awesome library.
  var loadIcons by ConfigProperty<Boolean>(primaryValues) // true
}