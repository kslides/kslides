package com.kslides.config

import com.kslides.KSlidesDslMarker

/**
 * Configuration for the [reveal.js menu plugin](https://github.com/denehyg/reveal.js-menu).
 * Only takes effect when [PresentationConfig.enableMenu] is `true`.
 */
@KSlidesDslMarker
class MenuConfig : AbstractConfig() {
  /** Side of the viewport the menu opens from. `"left"` or `"right"`. */
  var side by ConfigProperty<String>(revealjsManagedValues)

  /**
   * Menu width. Accepts a named size (`"normal"`, `"wide"`, `"third"`, `"half"`, `"full"`) or
   * any valid CSS length.
   */
  var width by ConfigProperty<String>(revealjsManagedValues)

  /** When `true`, add slide numbers to the titles in the menu list. */
  var numbers by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * CSS selector used to extract a slide's title. Defaults to the first heading
   * (`h1, h2, h3, h4, h5, h6`). A `data-menu-title` attribute or an element with
   * `class="menu-title"` on a slide takes precedence.
   */
  var titleSelector by ConfigProperty<String>(revealjsManagedValues)

  /**
   * When `true`, slides without a matching title selector fall back to using the first bit of
   * text content as the menu label.
   */
  var useTextContentForMissingTitles by ConfigProperty<Boolean>(revealjsManagedValues)

  /** When `true`, slides without any resolvable title are omitted from the menu entirely. */
  var hideMissingTitles by ConfigProperty<Boolean>(revealjsManagedValues)

  /** When `true`, show visited / current-slide progress markers next to menu items. */
  var markers by ConfigProperty<Boolean>(revealjsManagedValues)

  /** Enable support for custom menu panels. See the menu plugin's docs for the panel shape. */
  var custom by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * Enable the themes menu panel. `true` uses the default theme list; pass a custom theme list
   * via `revealjsManagedValues` directly for non-trivial setups.
   */
  var themes by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * Base directory for the default theme CSS files. Only used when [themes] is `true` and no
   * custom list is provided.
   */
  var themesPath by ConfigProperty<String>(revealjsManagedValues)

  /** Enable the transitions menu panel. */
  var transitions by ConfigProperty<Boolean>(revealjsManagedValues)

  /** When `true`, render a hamburger button on the slides to open the menu. */
  var openButton by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * When `true`, clicking the slide number opens the menu. Requires
   * [PresentationConfig.slideNumber] to be enabled.
   */
  var openSlideNumber by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * When `true`, the user can open and navigate the menu with the keyboard. Reveal.js keyboard
   * shortcuts are suspended while the menu is open.
   */
  var keyboard by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * When `true`, the menu stays open until explicitly closed (Esc or the `m` key when
   * [keyboard] is on). Otherwise normal user actions dismiss it.
   */
  var sticky by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * When `true`, menu panels auto-open during keyboard navigation. Only applies when both
   * [keyboard] and [sticky] are enabled.
   */
  var autoOpen by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * When `true`, the menu is not created until `RevealMenu.init()` is called manually. Defers
   * creation of all panels and the open button.
   */
  var delayInit by ConfigProperty<Boolean>(revealjsManagedValues)

  /** When `true`, the menu is visible immediately after initialization. */
  var openOnInit by ConfigProperty<Boolean>(revealjsManagedValues)

  /**
   * When `true` (the default), the menu plugin auto-loads Font Awesome. Set to `false` if your
   * presentation already loads Font Awesome to avoid a duplicate fetch.
   */
  var loadIcons by ConfigProperty<Boolean>(revealjsManagedValues)
}