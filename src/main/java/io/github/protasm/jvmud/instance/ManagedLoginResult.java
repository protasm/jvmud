package io.github.protasm.jvmud.instance;

import java.util.Optional;

/** Outcome of consuming one line in a host-managed login flow. */
record ManagedLoginResult(boolean shouldDisconnect, Optional<InstancePersona> replacement) {
    /** Continues the current login flow. */
    static ManagedLoginResult continueLogin() {
        return new ManagedLoginResult(false, Optional.empty());
    }

    /** Requests transport disconnection without attaching a Persona. */
    static ManagedLoginResult disconnectSession() {
        return new ManagedLoginResult(true, Optional.empty());
    }

    /** Completes login by replacing the temporary flow with an attached Persona. */
    static ManagedLoginResult replaceWith(InstancePersona persona) {
        return new ManagedLoginResult(false, Optional.of(persona));
    }
}
