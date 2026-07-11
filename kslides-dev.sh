#!/usr/bin/env bash
#
# kslides-dev.sh — live-reload dev loop for a kslides presentation.
#
# Watches Kotlin/CSS/HTML/Markdown sources and, on every change, restarts the app (recompiling
# and relaunching the Ktor server). Pair it with `output { enableHttp = true; devMode = true }`
# in your deck: the served page reconnects to the server over a websocket and refreshes the
# browser — landing back on the current slide — each time the server restarts.
#
# Usage:
#   ./kslides-dev.sh [options] [TASK] [WATCH_DIR ...]
#
# The first positional arg is the Gradle TASK (unless --task is given); any remaining positionals
# are watch directories. Environment variables (KSLIDES_DEV_TASK, KSLIDES_DEV_WATCH,
# KSLIDES_DEV_PORT) supply values when the matching arg is absent.
# Precedence: command-line args > environment variables > defaults.
#
# The defaults (the root project's `run` task, watching `src`) match a single-module kslides
# project generated from kslides-template — the layout a kslides user has. For a multi-module repo,
# name the module's task and source dir, e.g. `kslides-dev.sh :app:run app/src`.
#
# Portability: runs on macOS's default bash 3.2 and modern Linux bash. Polls once a second, so no
# external file-watcher is required.

set -euo pipefail

usage() {
  cat <<'EOF'
kslides-dev.sh — live-reload dev loop for a kslides presentation.

Usage:
  kslides-dev.sh [options] [TASK] [WATCH_DIR ...]

Options:
  -t, --task TASK    Gradle task to run                 (default: run)
  -w, --watch DIR    Directory to watch; repeatable     (default: src)
  -p, --port PORT    Server port to reclaim on restart  (default: 8080)
  -h, --help         Show this help and exit

Positional args: the first is the Gradle TASK (unless --task is given); any
remaining are watch directories.

Environment variables (used when the matching arg is absent):
  KSLIDES_DEV_TASK, KSLIDES_DEV_WATCH, KSLIDES_DEV_PORT

Precedence: command-line args > environment variables > defaults.

Examples:
  kslides-dev.sh                                 # root 'run' task, watch 'src' (kslides-template layout)
  kslides-dev.sh --port 9090                     # same, on a different port
  kslides-dev.sh :app:run app/src                # multi-module: run task + watch dir as positionals
  kslides-dev.sh --watch src --watch shared/src  # watch additional directories
EOF
}

die() {
  echo "Error: $1" >&2
  exit 1
}

# --- Parse arguments (CLI overrides env, which overrides the built-in defaults) ---------------
cli_task=""
cli_watch=""
cli_port=""
task_from_flag=0
positionals=""

while [ $# -gt 0 ]; do
  case "$1" in
    -t | --task)
      [ $# -ge 2 ] || die "--task requires a value"
      cli_task="$2"; task_from_flag=1; shift 2 ;;
    --task=*) cli_task="${1#--task=}"; task_from_flag=1; shift ;;
    -w | --watch)
      [ $# -ge 2 ] || die "--watch requires a value"
      cli_watch="$cli_watch $2"; shift 2 ;;
    --watch=*) cli_watch="$cli_watch ${1#--watch=}"; shift ;;
    -p | --port)
      [ $# -ge 2 ] || die "--port requires a value"
      cli_port="$2"; shift 2 ;;
    --port=*) cli_port="${1#--port=}"; shift ;;
    -h | --help) usage; exit 0 ;;
    --) shift; while [ $# -gt 0 ]; do positionals="$positionals $1"; shift; done ;;
    -*) die "unknown option: $1 (try --help)" ;;
    *) positionals="$positionals $1"; shift ;;
  esac
done

# Distribute positionals: first is the TASK (unless --task was given); the rest are watch dirs.
if [ -n "$positionals" ]; then
  # shellcheck disable=SC2086  # word-split intended: re-expand the collected positionals
  set -- $positionals
  if [ "$task_from_flag" -eq 0 ]; then
    cli_task="$1"; shift
  fi
  for dir in "$@"; do cli_watch="$cli_watch $dir"; done
fi
cli_watch="${cli_watch# }" # drop the leading space left by accumulation

TASK="${cli_task:-${KSLIDES_DEV_TASK:-run}}"
WATCH="${cli_watch:-${KSLIDES_DEV_WATCH:-src}}"
PORT="${cli_port:-${KSLIDES_DEV_PORT:-8080}}"

cd "$(dirname "$0")"
[ -x ./gradlew ] || {
  echo "Error: run this from the repo root (no executable ./gradlew here)." >&2
  exit 1
}

# Warn (but don't fail) on watch dirs that don't exist — usually a typo that would silently
# never trigger a rebuild.
for dir in $WATCH; do
  [ -d "$dir" ] || echo ">>> kslides-dev: warning: watch dir '$dir' does not exist" >&2
done

APP_PID=""
STAMP="$(mktemp)"

stop_app() {
  if [ -n "$APP_PID" ]; then
    kill "$APP_PID" 2>/dev/null || true
    APP_PID=""
  fi
  # The Gradle 'run' task forks a separate JVM, so also reclaim the port the old server holds.
  if command -v lsof >/dev/null 2>&1; then
    local pids
    pids="$(lsof -ti "tcp:$PORT" 2>/dev/null || true)"
    # shellcheck disable=SC2086  # word-split intended: kill every PID holding the port
    [ -n "$pids" ] && kill $pids 2>/dev/null || true
  fi
}

start_app() {
  touch "$STAMP"
  echo ">>> kslides-dev: starting $TASK (port $PORT)"
  ./gradlew "$TASK" &
  APP_PID=$!
}

changed() {
  # Any watched source whose mtime is newer than the last (re)start.
  # shellcheck disable=SC2086  # word-split intended: WATCH may name several dirs
  find $WATCH -type f \
    \( -name '*.kt' -o -name '*.kts' -o -name '*.css' -o -name '*.html' -o -name '*.md' \) \
    -newer "$STAMP" 2>/dev/null | head -1
}

cleanup() {
  echo
  echo ">>> kslides-dev: shutting down"
  stop_app
  rm -f "$STAMP"
}
trap cleanup EXIT
trap 'exit 130' INT TERM

echo ">>> kslides-dev: watching [$WATCH] — edit a slide and save to rebuild. Ctrl-C to stop."
start_app
while true; do
  sleep 1
  if [ -n "$(changed)" ]; then
    echo ">>> kslides-dev: change detected — restarting"
    stop_app
    start_app
  fi
done
