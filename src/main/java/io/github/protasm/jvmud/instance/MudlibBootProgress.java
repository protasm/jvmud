package io.github.protasm.jvmud.instance;

/**
 * Receives host-visible progress events while a mudlib is being booted.
 *
 * <p>The callback is intentionally outside mudlib semantics: it reports JVMud's startup work to the
 * local host process without requiring the mudlib to print anything or change driver-facing
 * behavior.</p>
 */
public interface MudlibBootProgress {
    /** Returns a progress callback that ignores all boot events. */
    static MudlibBootProgress none() {
        return new MudlibBootProgress() {};
    }

    /**
     * Called immediately before JVMud attempts to compile or retrieve a preload object.
     *
     * @param kind origin of the preload request
     * @param sourcePath mudlib source path without a trailing {@code .c}
     */
    default void preloadStarted(PreloadKind kind, String sourcePath) {}

    /**
     * Called after JVMud finishes attempting to compile or retrieve a preload object.
     *
     * @param kind origin of the preload request
     * @param sourcePath mudlib source path without a trailing {@code .c}
     * @param loaded whether the object was successfully loaded
     */
    default void preloadFinished(PreloadKind kind, String sourcePath, boolean loaded) {}

    /**
     * Called when a preload object could not be compiled or initialized.
     *
     * @param kind origin of the preload request
     * @param sourcePath mudlib source path without a trailing {@code .c}
     * @param error failure reported by the compiler or mudlib initialization
     */
    default void preloadFailed(PreloadKind kind, String sourcePath, Throwable error) {}

    /** Identifies which startup declaration requested a preload. */
    enum PreloadKind {
        /** A direct {@code preload_objects} entry from the JVMud mudlib configuration. */
        CONFIGURED_OBJECT,

        /** An object listed inside the configured {@code preload_file}. */
        MANIFEST_OBJECT
    }

}
