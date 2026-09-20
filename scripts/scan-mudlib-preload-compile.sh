#!/usr/bin/env sh
set -eu

if [ "$#" -lt 1 ] || [ "$#" -gt 2 ]; then
  echo "Usage: scripts/scan-mudlib-preload-compile.sh <config-file> [report-file]" >&2
  exit 64
fi

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
ROOT_DIR=$(dirname -- "$SCRIPT_DIR")

cd "$ROOT_DIR"
mvn -q compile dependency:build-classpath \
  -Dmdep.outputFile=target/jvmud-maintenance-classpath.txt >/dev/null

CP="target/classes:$(cat target/jvmud-maintenance-classpath.txt)"
exec java -cp "$CP" io.github.protasm.jvmud.language.diagnostics.MudlibPreloadCompileScan "$@"
