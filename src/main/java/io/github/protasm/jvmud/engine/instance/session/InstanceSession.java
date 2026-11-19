package io.github.protasm.jvmud.engine.instance.session;

import io.github.protasm.jvmud.engine.instance.session.login.ManagedLoginSession;
import io.github.protasm.jvmud.engine.model.identity.PlayerRecord;
import io.github.protasm.jvmud.engine.model.identity.SessionID;
import io.github.protasm.jvmud.engine.model.identity.SessionRecord;

import java.util.Objects;
import java.util.Optional;

/**
 * Stable handle for one connection throughout login, Persona attachment and disconnect.
 *
 * <p>All mutable state belongs to {@link InstanceSessions}. This handle keeps no second actor,
 * profile or attachment copy, so an LPC rebind is immediately visible to transport and engine
 * callers holding the same handle. Read it on the owning instance's execution queue; transports
 * enter through {@link io.github.protasm.jvmud.engine.instance.InstanceHost}.</p>
 */
public final class InstanceSession {
    private final InstanceSessions owner;
    private final SessionID id;

    InstanceSession(InstanceSessions owner, SessionID id) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.id = Objects.requireNonNull(id, "id");
    }

    /** Returns the engine's typed connection identity. */
    public SessionID id() {
        return id;
    }

    /** Returns the connection identity used by authored compatibility APIs. */
    public String sessionId() {
        return id.value();
    }

    /** Returns whether the owner still recognizes this connection. */
    public boolean isOpen() {
        return owner.session(id.value()).orElse(null) == this;
    }

    /** Returns the current connection record while the session remains open. */
    public Optional<SessionRecord> record() {
        return isOpen() ? owner.sessionRecord(id.value()) : Optional.empty();
    }

    /** Returns the current Player record while the session remains open. */
    public Optional<PlayerRecord> player() {
        return isOpen() ? owner.playerRecordForSession(id.value()) : Optional.empty();
    }

    /** Returns the selected Persona when attachment has completed. */
    public Optional<PersonaAttachment> attachment() {
        return isOpen() ? owner.attachment(id.value()) : Optional.empty();
    }

    /** Returns the current authored Persona behavior, requiring completed attachment. */
    public Object personaObject() {
        return attachment().orElseThrow(() -> new IllegalStateException("Session has no attached Persona.")).behavior();
    }

    /** Returns the optional managed login policy before Persona attachment. */
    public Optional<ManagedLoginSession> login() {
        return isOpen() ? owner.login(id.value()) : Optional.empty();
    }

    /** Returns the connection's remote address, if supplied by its transport. */
    public Optional<String> remoteAddress() {
        return record().flatMap(SessionRecord::remoteAddress);
    }
}
