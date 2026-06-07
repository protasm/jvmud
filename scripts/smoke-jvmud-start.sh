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
  -mudlib-dir mudlibs/lp245 \
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
    if "Attached player 1 as obj/player#clone" not in transcript:
        raise AssertionError(f"did not attach configured mudlib player object:\n{transcript}")
    if "What is your name: " not in transcript:
        raise AssertionError(f"vanilla player logon did not capture initial input:\n{transcript}")

    sock.sendall(b"smoketest\n")
    transcript = read_until(sock, "> ")
    if "New character." not in transcript:
        raise AssertionError(f"logon name input did not reach obj/player.logon2:\n{transcript}")
    if "Password: " not in transcript:
        raise AssertionError(f"logon name input did not request a password:\n{transcript}")

    sock.sendall(b"secret1\n")
    transcript = read_until(sock, "> ")
    if "Password: (again) " not in transcript:
        raise AssertionError(f"first password input did not request confirmation:\n{transcript}")

    sock.sendall(b"secret1\n")
    transcript = read_until(sock, "> ")
    if "Please enter your email address" not in transcript:
        raise AssertionError(f"password confirmation did not request email:\n{transcript}")

    sock.sendall(b"none\n")
    transcript = read_until(sock, "> ")
    if "Are you, male, female or other" not in transcript:
        raise AssertionError(f"email input did not request gender:\n{transcript}")

    sock.sendall(b"o\n")
    transcript = read_until(sock, "> ")
    if "Welcome, Creature!" not in transcript:
        raise AssertionError(f"gender input did not complete login:\n{transcript}")

    sock.sendall(b"look\n")
    transcript = read_until(sock, "> ")
    if "You are in the local village church." not in transcript:
        raise AssertionError(f"look did not render the village church:\n{transcript}")

    sock.sendall(b"south\n")
    transcript = read_until(sock, "> ")
    if "You are at an open green place south of the village church." not in transcript:
        raise AssertionError(f"south did not move to the village green:\n{transcript}")
    if "You can't do that." in transcript:
        raise AssertionError(f"south moved but was reported as unhandled:\n{transcript}")

    sock.sendall(b"west\n")
    transcript = read_until(sock, "> ")
    if "An old humpbacked bridge." not in transcript:
        raise AssertionError(f"west did not move to the humpbacked bridge:\n{transcript}")
    if "You can't do that." in transcript:
        raise AssertionError(f"west moved but was reported as unhandled:\n{transcript}")

    sock.sendall(b"west\n")
    transcript = read_until(sock, "> ")
    if "You are in the wilderness outside the village." not in transcript:
        raise AssertionError(f"west from bridge did not move to the wilderness:\n{transcript}")
    if "Error:" in transcript or "You can't do that." in transcript:
        raise AssertionError(f"wilderness movement reported an error:\n{transcript}")

    sock.sendall(b"west\n")
    transcript = read_until(sock, "> ")
    if "You are in a big forest." not in transcript:
        raise AssertionError(f"west from wilderness did not move to the forest:\n{transcript}")
    if "Error:" in transcript or "You can't do that." in transcript:
        raise AssertionError(f"forest movement reported an error:\n{transcript}")

    sock.sendall(b"west\n")
    transcript = read_until(sock, "> ")
    if "A small clearing. There are trees all around you." not in transcript:
        raise AssertionError(f"west from forest did not move to the clearing:\n{transcript}")
    if "Error:" in transcript or "You can't do that." in transcript:
        raise AssertionError(f"clearing movement reported an error:\n{transcript}")

    sock.sendall(b"west\n")
    transcript = read_until(sock, "> ")
    if "You are in a big forest." not in transcript:
        raise AssertionError(f"west from clearing did not move to the next forest room:\n{transcript}")
    if "Error:" in transcript or "You can't do that." in transcript:
        raise AssertionError(f"second forest movement reported an error:\n{transcript}")

    sock.sendall(b"west\n")
    transcript = read_until(sock, "> ")
    if "The forest gets light here, and slopes down to the west." not in transcript:
        raise AssertionError(f"west from second forest did not move to the mountain slope:\n{transcript}")
    if "Error:" in transcript or "You can't do that." in transcript:
        raise AssertionError(f"mountain slope movement reported an error:\n{transcript}")

    sock.sendall(b"west\n")
    transcript = read_until(sock, "> ")
    if "You are in the orc valley. This place is inhabited by orcs." not in transcript:
        raise AssertionError(f"west from mountain slope did not move to the orc valley:\n{transcript}")
    if "Error:" in transcript or "You can't do that." in transcript:
        raise AssertionError(f"orc valley movement reported an error:\n{transcript}")

    sock.sendall(b"east\n")
    transcript = read_until(sock, "> ")
    if "The forest gets light here, and slopes down to the west." not in transcript:
        raise AssertionError(f"east from orc valley did not return to the mountain slope:\n{transcript}")
    if "Error:" in transcript or "You can't do that." in transcript:
        raise AssertionError(f"orc valley exit reported an error:\n{transcript}")

    sock.sendall(b"/quit\n")

PY

echo "jvmud-start smoke passed"
