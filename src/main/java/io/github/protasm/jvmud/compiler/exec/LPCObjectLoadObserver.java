package io.github.protasm.jvmud.compiler.exec;

import java.nio.file.Path;

/** Observes host-side LPC object load attempts for diagnostics. */
public interface LPCObjectLoadObserver {
    /** Observer that ignores all load events. */
    LPCObjectLoadObserver NONE = new LPCObjectLoadObserver() {};

    /**
     * Called before the runtime starts loading a shared LPC object.
     *
     * @param objectId canonical mudlib object id
     * @param sourcePath resolved source path
     * @param depth current nested load depth
     */
    default void objectLoadStarted(String objectId, Path sourcePath, int depth) {}

    /**
     * Called after the runtime finishes a shared LPC object load attempt.
     *
     * @param objectId canonical mudlib object id
     * @param sourcePath resolved source path
     * @param depth nested load depth reported at start
     * @param loaded whether the object was loaded or supplied successfully
     * @param elapsedNanos elapsed load time in nanoseconds
     */
    default void objectLoadFinished(String objectId, Path sourcePath, int depth, boolean loaded, long elapsedNanos) {}
}
