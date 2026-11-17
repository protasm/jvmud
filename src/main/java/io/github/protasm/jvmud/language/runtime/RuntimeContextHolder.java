package io.github.protasm.jvmud.language.runtime;

/** Thread-local binding point for the active {@link RuntimeContext}. */
public final class RuntimeContextHolder {
    private static final ThreadLocal<RuntimeContext> CURRENT = new ThreadLocal<>();

    private RuntimeContextHolder() {}

    /** Returns the active execution contract, or {@code null} outside an LPC invocation. */
    public static RuntimeContext current() {
        return CURRENT.get();
    }

    /** Returns the active execution contract, reporting a missing host binding explicitly. */
    public static RuntimeContext requireCurrent() {
        RuntimeContext context = CURRENT.get();
        if (context == null) {
            throw new IllegalStateException("RuntimeContext has not been installed for this thread.");
        }
        return context;
    }

    /** Installs a host binding; {@code null} clears it when execution returns to unbound Java code. */
    public static void setCurrent(RuntimeContext context) {
        if (context == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(context);
        }
    }
}
