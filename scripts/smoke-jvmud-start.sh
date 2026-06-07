#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$ROOT_DIR"

PORT=$(
  python3 - <<'PY'
import socket

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
    sock.bind(("127.0.0.1", 0))
    print(sock.getsockname()[1])
PY
)

TMP_BASE=${TMPDIR:-/tmp}
TMP_BASE=${TMP_BASE%/}
LOG_FILE=$(mktemp "$TMP_BASE/jvmud-start-smoke.XXXXXX")
SERVER_PID=

cleanup() {
  status=$1
  if [[ -n "${SERVER_PID}" ]] && kill -0 "$SERVER_PID" 2>/dev/null; then
    kill "$SERVER_PID" 2>/dev/null || true
    wait "$SERVER_PID" 2>/dev/null || true
  fi
  if [[ "$status" -eq 0 ]]; then
    rm -f "$LOG_FILE"
  else
    echo "server log: $LOG_FILE" >&2
  fi
}
trap 'status=$?; cleanup "$status"; exit "$status"' EXIT

./jvmud-start \
  -mudlib-dir mudlib \
  -port "$PORT" \
  -host 127.0.0.1 \
  -config jvmud/config \
  >"$LOG_FILE" 2>&1 &
SERVER_PID=$!

python3 - "$PORT" <<'PY'
import socket
import sys
import time

port = int(sys.argv[1])


def connect_with_retry(deadline_seconds=30):
    deadline = time.monotonic() + deadline_seconds
    last_error = None
    while time.monotonic() < deadline:
        try:
            sock = socket.create_connection(("127.0.0.1", port), timeout=1)
            sock.settimeout(5)
            return sock
        except OSError as exc:
            last_error = exc
            time.sleep(0.25)
    raise RuntimeError(f"server did not accept a connection: {last_error}")


def read_until(sock, marker):
    chunks = []
    while True:
        data = sock.recv(4096)
        if not data:
            break
        chunks.append(data.decode("utf-8", errors="replace"))
        transcript = "".join(chunks)
        if marker in transcript:
            return transcript
    return "".join(chunks)


with connect_with_retry() as sock:
    transcript = read_until(sock, "> ")
    if "JVMud telnet." not in transcript:
        raise AssertionError(f"missing telnet greeting:\n{transcript}")
    if "Attached player" not in transcript:
        raise AssertionError(f"missing player attachment:\n{transcript}")

    sock.sendall(b"look\n")
    transcript = read_until(sock, "> ")
    if "You are at an open green place south of the village church." not in transcript:
        raise AssertionError(f"look did not render the village green:\n{transcript}")

    sock.sendall(b"north\n")
    transcript = read_until(sock, "> ")
    if "You are in the local village church." not in transcript:
        raise AssertionError(f"north did not move to the church:\n{transcript}")

    sock.sendall(b"/quit\n")

PY

echo "jvmud-start smoke passed"
