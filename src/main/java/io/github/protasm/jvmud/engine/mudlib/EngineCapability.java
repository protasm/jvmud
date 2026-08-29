package io.github.protasm.jvmud.engine.mudlib;

/** Host-resource capabilities that are not exposed to a mudlib unless its manifest opts in. */
public enum EngineCapability {
    /** Read and mutate files below the configured mudlib root. */
    MUDLIB_FILES,
    /** Open and use configured database connections. */
    DATABASE,
    /** Inspect and rebind connected sessions. */
    SESSION_CONTROL,
    /** Request hosted-world transfer, shutdown, or similar instance-level control. */
    HOST_CONTROL
}
