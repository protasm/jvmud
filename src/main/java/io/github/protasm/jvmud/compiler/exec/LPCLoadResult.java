package io.github.protasm.jvmud.compiler.exec;

import java.util.Objects;
import java.util.Optional;

/**
 * Result wrapper for loading LPC objects, allowing callers to handle failures gracefully.
 */
public final class LPCLoadResult {
    private final LPCObjectHandle handle;
    private final Throwable error;

    private LPCLoadResult(LPCObjectHandle handle, Throwable error) {
        this.handle = handle;
        this.error = error;
    }

    public static LPCLoadResult success(LPCObjectHandle handle) {
        return new LPCLoadResult(Objects.requireNonNull(handle, "handle"), null);
    }

    public static LPCLoadResult failure(Throwable error) {
        return new LPCLoadResult(null, Objects.requireNonNull(error, "error"));
    }

    public boolean succeeded() {
        return handle != null;
    }

    public Optional<LPCObjectHandle> handle() {
        return Optional.ofNullable(handle);
    }

    public Optional<Throwable> error() {
        return Optional.ofNullable(error);
    }
}
