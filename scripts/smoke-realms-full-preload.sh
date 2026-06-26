#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
ROOT_DIR=$(dirname -- "$SCRIPT_DIR")
CONFIG_FILE="$ROOT_DIR/mudlibs/realmsmud/jvmud/realmsmud.full.config"
LOG_FILE="${1:-$ROOT_DIR/target/realms-full-preload.log}"
TIMEOUT_SECONDS="${JVMUD_REALMS_FULL_PRELOAD_TIMEOUT:-120}"

"$SCRIPT_DIR/generate-realms-full-init.sh" >/dev/null
mkdir -p "$(dirname -- "$LOG_FILE")"

set +e
"$ROOT_DIR/scripts/jvmud-start" --trace-startup-loads "$CONFIG_FILE" > "$LOG_FILE" 2>&1 &
pid=$!

elapsed=0
status=
while kill -0 "$pid" 2>/dev/null; do
  if grep -q '^JVMud mudlib listening on ' "$LOG_FILE"; then
    kill "$pid" 2>/dev/null || true
    wait "$pid" 2>/dev/null
    status=0
    break
  fi
  if [ "$elapsed" -ge "$TIMEOUT_SECONDS" ]; then
    kill "$pid" 2>/dev/null || true
    wait "$pid" 2>/dev/null
    status=124
    break
  fi
  sleep 1
  elapsed=$((elapsed + 1))
done

if [ -z "${status:-}" ]; then
  wait "$pid"
  status=$?
fi
set -e

case "$status" in
  0)
    ;;
  124)
    if ! grep -q '^JVMud mudlib listening on ' "$LOG_FILE"; then
      echo "Full preload did not reach server startup before timeout. See $LOG_FILE" >&2
      exit 1
    fi
    ;;
  *)
    echo "Full preload boot failed with status $status. See $LOG_FILE" >&2
    tail -n 80 "$LOG_FILE" >&2 || true
    exit "$status"
    ;;
esac

summary=$(grep '^preload manifest ' "$LOG_FILE" | tail -n 1 || true)
if [ -z "$summary" ]; then
  echo "Full preload summary was not printed. See $LOG_FILE" >&2
  exit 1
fi

echo "$summary"

skipped=$(printf '%s\n' "$summary" | sed -n 's/.* skipped \([0-9][0-9]*\) object(s).*/\1/p')
if [ -z "$skipped" ]; then
  echo "Could not parse skipped object count. See $LOG_FILE" >&2
  exit 1
fi

if [ "$skipped" -ne 0 ]; then
  echo "Full preload found skipped object(s). See $LOG_FILE" >&2
  grep '^preload manifest ' "$LOG_FILE" | tail -n 1 >&2 || true
  grep '^startup compile .*failed' "$LOG_FILE" >&2 || true
  grep '^startup object .*failed' "$LOG_FILE" >&2 || true
  exit 1
fi
