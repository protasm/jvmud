# Shared by the distribution launchers; paths supplied by users stay relative
# to their current directory, even when the launcher lives elsewhere.
JVMUD_ROOT=$(dirname -- "$JVMUD_SCRIPT_DIR")
if [ -n "${JAVA_HOME:-}" ]; then
    JVMUD_JAVA="$JAVA_HOME/bin/java"
else
    JVMUD_JAVA=$(command -v java || true)
fi
if [ -z "$JVMUD_JAVA" ] || [ ! -x "$JVMUD_JAVA" ]; then
    echo "JVMud requires Java 21 or newer. Install Java and set JAVA_HOME or add java to PATH." >&2
    exit 1
fi
if ! JVMUD_JAVA_SETTINGS=$("$JVMUD_JAVA" -XshowSettings:properties -version 2>&1); then
    echo "Unable to run Java. Check JAVA_HOME and your Java installation." >&2
    exit 1
fi
JVMUD_JAVA_VERSION=$(printf '%s\n' "$JVMUD_JAVA_SETTINGS" | sed -n 's/^[[:space:]]*java.specification.version = //p')
case "$JVMUD_JAVA_VERSION" in
    ''|*[!0-9]*)
        echo "JVMud requires Java 21 or newer; unable to use Java version '$JVMUD_JAVA_VERSION'." >&2
        exit 1 ;;
esac
if [ "$JVMUD_JAVA_VERSION" -lt 21 ]; then
    echo "JVMud requires Java 21 or newer; found Java $JVMUD_JAVA_VERSION." >&2
    exit 1
fi
if [ ! -d "$JVMUD_ROOT/lib" ]; then
    echo "Missing JVMud lib directory. Extract the complete distribution before running it." >&2
    exit 1
fi
