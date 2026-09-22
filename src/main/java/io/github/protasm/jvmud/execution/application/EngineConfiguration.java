package io.github.protasm.jvmud.execution.application;

import java.nio.file.Path;
import java.util.Objects;

/** Application endpoints and private control state, independent of mudlib manifests. */
public record EngineConfiguration(String bindAddress, int playerPort, int adminPort, Path stateDirectory,
        boolean traceLoads, Path mudlibDirectory, String publicHost) {
    /** Omits a public hostname, so directories refer players to the host they already connected to. */
    public EngineConfiguration(String bindAddress, int playerPort, int adminPort, Path stateDirectory,
            boolean traceLoads, Path mudlibDirectory) {
        this(bindAddress, playerPort, adminPort, stateDirectory, traceLoads, mudlibDirectory, null);
    }
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
        if (publicHost != null && !publicHost.matches("[A-Za-z0-9][A-Za-z0-9.:-]*"))
            throw new IllegalArgumentException("Public host must be a hostname or IP address without a port, scheme, or path.");
    }
}
