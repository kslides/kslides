@file:Suppress("MatchingDeclarationName")

package com.kslides

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.send

/**
 * Internal support for the [com.kslides.config.OutputConfig.devMode] live-reload feature: the
 * websocket path, the client script injected into served pages, and the server route that drives
 * reloads.
 *
 * The browser follows the server. The client connects to [RELOAD_PATH] and remembers the boot
 * epoch the server sends on connect; a restarted JVM has a new epoch, so the next successful
 * reconnect reloads the page, while a transient blip (same epoch) does not. The current
 * slide/fragment is persisted to `sessionStorage` on every change and restored after the reload,
 * so authoring lands back where it left off without relying on reveal.js's `hash` option.
 */
internal object LiveReload {
  const val RELOAD_PATH = "/kslides-reload"

  // Injected only for HTTP renders when devMode is on (see Page.generateBody). Plain ES5 so it runs
  // in the browser without a build step.
  val clientScript =
    """
    (function () {
      var stateKey = "kslides-devmode-state:" + location.pathname;
      var firstEpoch = null;

      function persist() {
        try {
          if (window.Reveal) {
            if (Reveal.isReady()) sessionStorage.setItem(stateKey, JSON.stringify(Reveal.getState()));
          }
        } catch (e) { /* ignore */ }
      }
      function restore() {
        try {
          var saved = sessionStorage.getItem(stateKey);
          if (saved) {
            if (window.Reveal) Reveal.setState(JSON.parse(saved));
          }
        } catch (e) { /* ignore */ }
      }
      if (window.Reveal) {
        Reveal.on('ready', restore);
        Reveal.on('slidechanged', persist);
        Reveal.on('fragmentshown', persist);
        Reveal.on('fragmenthidden', persist);
      }

      function connect() {
        var proto = location.protocol === 'https:' ? 'wss:' : 'ws:';
        var ws = new WebSocket(proto + '//' + location.host + '$RELOAD_PATH');
        ws.onmessage = function (ev) {
          if (firstEpoch === null) firstEpoch = ev.data;
          else if (ev.data !== firstEpoch) { persist(); location.reload(); }
        };
        ws.onclose = function () { setTimeout(connect, 500); };
        ws.onerror = function () { try { ws.close(); } catch (e) { /* ignore */ } };
      }
      connect();
    })();
    """.trimIndent()

  /**
   * Registers the live-reload websocket. On connect it sends [epoch] (the server's boot id) and
   * holds the socket open until the client disconnects or this server dies on restart.
   */
  internal fun Route.kslidesReloadRoute(epoch: String) {
    webSocket(RELOAD_PATH) {
      send(epoch)
      closeReason.await()
    }
  }
}
