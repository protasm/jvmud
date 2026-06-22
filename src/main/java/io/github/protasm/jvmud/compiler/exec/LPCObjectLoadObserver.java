package io.github.protasm.jvmud.compiler.exec;

import java.nio.file.Path;

/** Observes host-side LPC object load and compile attempts for diagnostics. */
public interface LPCObjectLoadObserver {
    /** Observer that ignores all diagnostic events. */
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

    /**
     * Called when a shared LPC object load fails with an exception.
     *
     * @param objectId canonical mudlib object id
     * @param sourcePath resolved source path
     * @param depth nested load depth reported at start
     * @param failure exception or error that prevented the load
     */
    default void objectLoadFailed(String objectId, Path sourcePath, int depth, Throwable failure) {}

    /**
     * Called before the runtime compiles a source file through {@link LPCRuntime#compile(Path)}.
     *
     * @param objectId canonical mudlib object id
     * @param sourcePath resolved source path
     */
    default void objectCompileStarted(String objectId, Path sourcePath) {}

    /**
     * Called after the runtime finishes a source compilation attempt.
     *
     * @param objectId canonical mudlib object id
     * @param sourcePath resolved source path
     * @param compiled whether compilation completed without reported problems
     * @param elapsedNanos elapsed compile time in nanoseconds
     */
    default void objectCompileFinished(String objectId, Path sourcePath, boolean compiled, long elapsedNanos) {}

    /**
     * Called when a source compilation attempt fails with an exception.
     *
     * @param objectId canonical mudlib object id
     * @param sourcePath resolved source path
     * @param failure exception or error that prevented compilation
     */
    default void objectCompileFailed(String objectId, Path sourcePath, Throwable failure) {}
}
