package io.github.protasm.jvmud.engine.instance.session.login;

import java.util.Objects;
import java.util.Optional;

/** An explicit login outcome; Persona attachment remains the hosting instance's responsibility. */
public record ManagedLoginResult(boolean shouldDisconnect, Optional<ManagedPersonaProfile> profile) {
    /** Creates an outcome containing at most one next step. */
    public ManagedLoginResult {
        Objects.requireNonNull(profile, "profile");
        if (shouldDisconnect && profile.isPresent()) {
            throw new IllegalArgumentException("A login cannot attach and disconnect in the same outcome.");
        }
    }

    /** Continues the current login flow without attaching a Persona. */
    public static ManagedLoginResult continueLogin() {
        return new ManagedLoginResult(false, Optional.empty());
    }

    /** Requests disconnection without attaching a Persona. */
    public static ManagedLoginResult disconnectSession() {
        return new ManagedLoginResult(true, Optional.empty());
    }

    /** Delivers the resolved mudlib profile for the instance to attach. */
    public static ManagedLoginResult authenticated(ManagedPersonaProfile profile) {
        return new ManagedLoginResult(false, Optional.of(profile));
    }
}
