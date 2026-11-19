package io.github.protasm.jvmud.engine.instance.session.login;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

/** Selects optional host-managed login flows without owning connections or Persona attachment. */
public final class ManagedLoginPolicies {
    private ManagedLoginPolicies() {}

    /** Creates the explicitly selected policy, or leaves login to authored LPC when none is selected. */
    public static Optional<ManagedLoginSession> create(
            String policy, Path mudlibRoot, String gameName, Consumer<String> output) {
        if (policy == null) {
            return Optional.empty();
        }
        return switch (policy) {
            case "filesystem_accounts" -> Optional.of(new FilesystemAccountLoginSession(mudlibRoot, gameName, output));
            default -> throw new IllegalStateException("Unknown host session policy: " + policy);
        };
    }
}
