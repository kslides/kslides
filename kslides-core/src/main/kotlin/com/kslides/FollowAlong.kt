@file:Suppress("MatchingDeclarationName")

package com.kslides

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.routing.Route
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import java.util.concurrent.ConcurrentHashMap

/**
 * Internal support for the [com.kslides.config.OutputConfig.followAlong] presenting feature:
 * the websocket path, the client script injected into served pages, and the server route that
 * relays the presenter's position to the audience.
 *
 * The audience follows the presenter. The presenter opens the deck with `?present=<token>` and
 * publishes reveal.js state (slide + fragment) on every navigation; the server validates the
 * token, remembers the deck's last-known state, and broadcasts to every connected viewer. A
 * viewer who navigates on their own "breaks away" (client-side) and can rejoin the live
 * position with one click; late joiners receive the current position on connect. Viewers are
 * read-only by construction — the server ignores every frame a viewer sends.
 *
 * One presenter per presentation: a new presenter connection with a valid token supersedes the
 * previous one (forgiving for the page-refresh case), and the superseded client demotes itself
 * to a viewer instead of reconnecting as presenter.
 */
internal object FollowAlong {
  internal val logger = KotlinLogging.logger {}

  const val FOLLOW_PATH = "/kslides-follow"

  /** Query parameter carrying the presenter token; shared by the client script and the URL log. */
  const val PRESENT_PARAM = "present"

  // Close reason sent to a presenter superseded by a newer presenter connection. The client
  // checks for this exact text to demote itself to a viewer rather than reconnect-and-retake.
  internal const val REPLACED_REASON = "kslides-presenter-replaced"

  private const val LIVE_STATUS = """{"type":"status","live":true}"""
  private const val OFFLINE_STATUS = """{"type":"status","live":false}"""

  /** Per-presentation sync state, scoped to one server instance (created per route registration). */
  internal class DeckSync {
    val viewers: MutableSet<DefaultWebSocketServerSession> = ConcurrentHashMap.newKeySet()

    @Volatile
    var presenter: DefaultWebSocketServerSession? = null

    /** The presenter's most recent state envelope, relayed verbatim to late joiners. */
    @Volatile
    var lastState: String? = null

    fun statusJson() = if (presenter != null) LIVE_STATUS else OFFLINE_STATUS
  }

  /**
   * Registers the follow-along websocket. Presenters (`role=presenter`) must supply the
   * matching [token]; their text frames are stored as the deck's last-known state and broadcast
   * to every viewer of the same presentation `path`. Viewers receive the live status and the
   * last-known state on connect, then follow the broadcasts.
   */
  internal fun Route.kslidesFollowRoute(
    kslides: KSlides,
    token: String,
  ) {
    val decks = ConcurrentHashMap<String, DeckSync>()

    webSocket(FOLLOW_PATH) {
      val path = call.request.queryParameters["path"] ?: ""
      if (path !in kslides.presentationMap) {
        close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Unknown presentation path"))
        return@webSocket
      }

      val deck = decks.computeIfAbsent(path) { DeckSync() }
      if (call.request.queryParameters["role"] == "presenter")
        servePresenter(deck, token)
      else
        serveViewer(deck)
    }
  }

  private suspend fun DefaultWebSocketServerSession.servePresenter(
    deck: DeckSync,
    token: String,
  ) {
    if (call.request.queryParameters["token"] != token) {
      logger.warn { "Rejected presenter connection with invalid token" }
      close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid presenter token"))
      return
    }

    // Single presenter per deck: a valid newcomer supersedes the previous connection.
    deck.presenter?.let { previous ->
      runCatching { previous.close(CloseReason(CloseReason.Codes.NORMAL, REPLACED_REASON)) }
    }
    deck.presenter = this
    logger.info { "Presenter connected" }
    deck.broadcast(deck.statusJson())

    try {
      for (frame in incoming) {
        if (frame is Frame.Text) {
          val state = frame.readText()
          // The client publishes from both onopen and reveal's ready event, so identical
          // consecutive states are common; a single compare replaces an N-viewer fanout.
          if (state != deck.lastState) {
            deck.lastState = state
            deck.broadcast(state)
          }
        }
      }
    } finally {
      // Only clear if this connection is still the active presenter (it may have been
      // superseded). broadcast never suspends, so this also runs to completion when an abrupt
      // disconnect (browser navigation, network drop) cancels the handler coroutine.
      if (deck.presenter === this) {
        deck.presenter = null
        deck.broadcast(deck.statusJson())
        logger.info { "Presenter disconnected" }
      }
    }
  }

  private suspend fun DefaultWebSocketServerSession.serveViewer(deck: DeckSync) {
    deck.viewers += this
    try {
      send(deck.statusJson())
      deck.lastState?.let { send(it) }
      // Viewers are read-only by construction: drain and ignore anything they send.
      for (frame in incoming) {
        // ignore
      }
    } finally {
      deck.viewers -= this
    }
  }

  // Non-suspending fan-out: the payload is encoded once, and a full or closed viewer channel
  // drops the frame instead of stalling the presenter's read loop behind one slow connection.
  // A viewer that misses a frame resyncs on the next navigation (or via lastState on reconnect).
  private fun DeckSync.broadcast(message: String) {
    val data = message.toByteArray()
    viewers.forEach { viewer ->
      viewer.outgoing.trySend(Frame.Text(true, data))
    }
  }

  // Styling for the audience/presenter badge, emitted through the standard head-CSS pipeline
  // (like Mermaid.headCss) so deck authors can override or hide it with ordinary css{} rules.
  internal val badgeCss =
    """
    .kslides-follow-badge {
      position: fixed;
      bottom: 14px;
      left: 50%;
      transform: translateX(-50%);
      z-index: 12;
      font: 12px/1.4 sans-serif;
      padding: 4px 12px;
      border-radius: 12px;
      background: rgba(0, 0, 0, 0.55);
      color: #fff;
      user-select: none;
    }
    """.trimIndent()

  // Injected only for HTTP renders when followAlong is on (see Page.generateBody). Plain ES5 so
  // it runs in the browser without a build step. Skips print view entirely so exported PDFs and
  // ?print-pdf never carry the badge or a websocket connection. NOTE: page scripts pass through
  // an XML parser during DOM serialization, which a bare '<' aborts — ampersands are repaired by
  // InternalUtils.rawSource, but '<' cannot be.
  val clientScript =
    """
    (function () {
      if (/print-pdf/.test(location.search)) return;

      var tokenMatch = location.search.match(/[?&]$PRESENT_PARAM=([^&]+)/);
      var token = tokenMatch ? decodeURIComponent(tokenMatch[1]) : null;
      var isPresenter = !!token;
      var following = !isPresenter;
      var applying = false;
      var live = false;
      var lastState = null;
      var ws = null;
      var badge = null;
      var retryDelay = 1000;

      function ensureBadge() {
        if (badge) return badge;
        badge = document.createElement('div');
        badge.className = 'kslides-follow-badge';
        badge.onclick = function () {
          if (isPresenter || following) return;
          following = true;
          if (lastState) applyState(lastState);
          updateBadge();
        };
        document.body.appendChild(badge);
        return badge;
      }

      function updateBadge() {
        var b = ensureBadge();
        if (isPresenter) {
          b.textContent = '● PRESENTING';
          b.style.cursor = 'default';
        } else if (!following) {
          b.textContent = '⏸ Paused — click to rejoin';
          b.style.cursor = 'pointer';
        } else if (live) {
          b.textContent = '● LIVE';
          b.style.cursor = 'default';
        } else {
          b.textContent = 'presenter offline';
          b.style.cursor = 'default';
        }
      }

      function applyState(state) {
        if (!window.Reveal) return;
        if (!Reveal.isReady()) return;
        applying = true;
        try { Reveal.setState(state); } catch (e) { /* ignore */ }
        setTimeout(function () { applying = false; }, 0);
      }

      function publish() {
        if (!ws) return;
        if (ws.readyState !== 1) return;
        if (!window.Reveal) return;
        if (!Reveal.isReady()) return;
        ws.send(JSON.stringify({ type: 'state', state: Reveal.getState() }));
      }

      // Role is checked at dispatch time (not registration), so a presenter demoted to viewer
      // keeps working: publishing stops and break-away detection takes over.
      function onNav() {
        if (isPresenter) { publish(); return; }
        if (applying) return;
        if (!following) return;
        following = false;
        updateBadge();
      }

      if (window.Reveal) {
        Reveal.on('ready', function () {
          if (isPresenter) { publish(); return; }
          if (!following) return;
          if (lastState) applyState(lastState);
        });
        Reveal.on('slidechanged', onNav);
        Reveal.on('fragmentshown', onNav);
        Reveal.on('fragmenthidden', onNav);
      }

      function demote(why) {
        console.warn('kslides: ' + why + '; continuing as viewer');
        isPresenter = false;
        following = true;
        updateBadge();
        setTimeout(connect, 250);
      }

      function connect() {
        var proto = location.protocol === 'https:' ? 'wss:' : 'ws:';
        var qs = '?path=' + encodeURIComponent(location.pathname);
        if (isPresenter) qs = qs + '&role=presenter&token=' + encodeURIComponent(token);
        else qs = qs + '&role=viewer';
        ws = new WebSocket(proto + '//' + location.host + '$FOLLOW_PATH' + qs);
        ws.onopen = function () {
          retryDelay = 1000;
          if (isPresenter) publish();
          updateBadge();
        };
        ws.onmessage = function (ev) {
          if (isPresenter) return;
          var msg;
          try { msg = JSON.parse(ev.data); } catch (e) { return; }
          if (msg.type === 'state') {
            if (msg.state) {
              lastState = msg.state;
              if (following) applyState(msg.state);
            }
          } else if (msg.type === 'status') {
            live = !!msg.live;
            updateBadge();
          }
        };
        ws.onclose = function (ev) {
          if (isPresenter) {
            if (ev.reason === '$REPLACED_REASON') { demote('another presenter took over'); return; }
            if (ev.code === 1008) { demote('presenter token rejected'); return; }
          }
          // Exponential backoff with jitter: after a talk ends, N audience tabs must not hammer
          // the stopped (or restarting) server in lockstep every second.
          setTimeout(connect, retryDelay * (0.75 + Math.random() * 0.5));
          retryDelay = Math.min(15000, retryDelay * 2);
        };
        ws.onerror = function () { try { ws.close(); } catch (e) { /* ignore */ } };
      }

      // A corner link (or any other in-deck link) to another deck is a full page load, and the
      // new page reads its role from the query string — so without the token the presenter
      // silently lands as a viewer. Carry it across same-origin links. External links and
      // in-page '#' navigation are left alone: the former must not leak the token, the latter
      // never reloads.
      function carryTokenAcrossLinks() {
        if (!isPresenter) return;
        var carried = /[?&]$PRESENT_PARAM=/;
        // forEach, not an indexed loop: its less-than comparison would abort the XML
        // serialization of this script. (Spelled out because the character cannot appear here
        // either.)
        [].forEach.call(document.querySelectorAll('a[href]'), function (a) {
          var raw = a.getAttribute('href');
          if (!raw) return;
          if (raw.charAt(0) === '#') return;
          if (a.origin !== location.origin) return;
          if (carried.test(a.search)) return;
          var sep = a.search ? '&' : '?';
          a.href = a.origin + a.pathname + a.search + sep + '$PRESENT_PARAM=' + encodeURIComponent(token) + a.hash;
        });
      }
      if (document.readyState === 'loading')
        document.addEventListener('DOMContentLoaded', carryTokenAcrossLinks);
      else
        carryTokenAcrossLinks();

      // Chrome's back-forward cache freezes navigated-away pages instead of destroying them,
      // leaving their websocket open at the network layer — the server would never see the
      // presenter leave. Close eagerly on pagehide; on a bfcache restore the pending reconnect
      // timer fires and the page rejoins with its original role.
      window.addEventListener('pagehide', function () {
        try { if (ws) ws.close(); } catch (e) { /* ignore */ }
      });

      connect();
    })();
    """.trimIndent()

  // Precomputed once: generatePage runs per HTTP request under the render lock, so the ~6KB
  // re-indent must not happen there.
  internal val indentedClientScript by lazy { clientScript.prependIndent("\t\t") }
}
