---
icon: lucide/radio-tower
---

# Follow-along presenting

When presenting remotely or to a large room, the audience either watches a screen-share (low fidelity, no way to look back) or opens the deck URL themselves and is immediately on the wrong slide. Follow-along presenting fills that gap: every attendee opens the plain deck URL, and their browser follows the presenter's slide and fragment position live.

reveal.js used to cover this with the multiplex plugin, which is retired — kslides can fill the niche because HTTP mode already ships a real Ktor server.

## Enabling it

```kotlin
--8<-- "Presenting.kt:follow-along"
```

At startup the server logs one presenter URL per deck:

```
Follow-along presenting enabled. Presenter URLs:
  http://localhost:8080/?present=1de9b0…
```

- **The presenter** opens that URL. Their navigation (slides *and* fragments) is published to the server, and a `● PRESENTING` badge confirms the role.
- **The audience** opens the plain deck URL — no token, nothing to configure. A `● LIVE` badge shows they're following; when no presenter is connected it reads `presenter offline`.
- **Break away / rejoin**: a viewer who navigates on their own stops following (`⏸ Paused — click to rejoin`); clicking the badge snaps back to the presenter's current position. Late joiners land on the current slide immediately.

## How it works

The same pattern as [dev-mode live reload](output.md): the `followAlong` flag gates a `/kslides-follow` websocket route plus a small client script injected into served pages. The server keeps one in-memory sync state per presentation — the active presenter, the connected viewers, and the last-known position for late joiners. Viewers are read-only by construction: the server ignores every frame they send, so only the token-validated presenter can move the deck.

One presenter per presentation: connecting with a valid token supersedes the previous presenter connection (so a page refresh just works), and the superseded tab demotes itself to a viewer.

## Details worth knowing

- **HTTP mode only.** Follow-along needs the live server; the client script is never emitted into filesystem output, `?print-pdf` view, or [exported PDFs](pdf-export.md).
- **The token is lightweight, demo-grade auth.** It travels in the URL and is generated fresh per launch (or set `presenterToken` for a predictable URL). Anyone with the token can present; anyone without it can only watch.
- **Disconnect handling.** The client closes its socket on `pagehide` (covering browsers that freeze navigated-away pages instead of destroying them), and the server pings connections to reap dead ones — so the audience reliably sees `presenter offline` when the presenter leaves, and `● LIVE` again when they return.
- **Run it anywhere.** The fat JAR on a laptop or a small VPS is enough — attendees just need the URL.
