package io.github.protasm.jvmud.execution.application;

import java.nio.file.Path;
import java.util.Objects;

/** Application endpoints and private control state, independent of mudlib manifests. */
public record EngineConfiguration(String bindAddress, int playerPort, int adminPort, Path stateDirectory,
        boolean traceLoads, Path mudlibDirectory) {
    /** Uses the working directory's mudlibs folder for administrator-visible mudlibs. */
    public EngineConfiguration(String bindAddress, int playerPort, int adminPort, Path stateDirectory, boolean traceLoads) {
        this(bindAddress, playerPort, adminPort, stateDirectory, traceLoads, Path.of("mudlibs"));
    }
    /** Validates independent ports; zero asks the OS to allocate a free port for embedding. */
    public EngineConfiguration {
        Objects.requireNonNull(bindAddress, "bindAddress");
        if (playerPort < 0 || playerPort > 65535 || adminPort < 0 || adminPort > 65535 || playerPort != 0 && playerPort == adminPort)
            throw new IllegalArgumentException("Engine player and admin ports must be distinct and in range 0–65535.");
        stateDirectory = stateDirectory.toAbsolutePath().normalize();
        mudlibDirectory = Objects.requireNonNull(mudlibDirectory, "mudlibDirectory").toAbsolutePath().normalize();
    }
}
