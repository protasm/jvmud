#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
ROOT_DIR=$(dirname -- "$SCRIPT_DIR")

"$SCRIPT_DIR/generate-realms-full-init.sh" >/dev/null

cd "$ROOT_DIR"
mvn -q compile dependency:build-classpath \
  -Dmdep.outputFile=target/jvmud-cli-classpath.txt >/dev/null

CP="target/classes:$(cat target/jvmud-cli-classpath.txt)"
exec java -cp "$CP" io.github.protasm.jvmud.cli.RealmsFullCompileScan "$@"
