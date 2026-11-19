package io.github.protasm.jvmud.engine.instance.session;

import io.github.protasm.jvmud.engine.instance.session.login.ManagedLoginSession;
import io.github.protasm.jvmud.engine.model.identity.PersonaID;
import io.github.protasm.jvmud.engine.model.identity.PersonaRecord;
import io.github.protasm.jvmud.engine.model.identity.PlayerID;
import io.github.protasm.jvmud.engine.model.identity.PlayerRecord;
import io.github.protasm.jvmud.engine.model.identity.SessionID;
import io.github.protasm.jvmud.engine.model.identity.SessionRecord;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Authoritative connection, Player and Persona attachment state for one hosted mudlib.
 *
 * <p>Opening a connection, selecting a Persona, rebinding authored behavior and closing the
 * connection are explicit transitions here. Every lookup, input capture, negotiated protocol and
 * output route observes those same transitions. The public session handles carry no mutable copy
 * of the selected actor.</p>
 *
 * <p>All operations are confined to the instance's execution queue. This owner coordinates engine
 * identity and connection state; it does not interpret authored commands or execute LPC callbacks.</p>
 */
public final class InstanceSessions {
    private final Map<SessionID, Connection> connections = new LinkedHashMap<>();
    private final Map<PlayerID, PlayerRecord> players = new LinkedHashMap<>();
    private final Map<PersonaID, PersonaRecord> personas = new LinkedHashMap<>();
    private final Map<Object, Connection> connectionsByBehavior = new IdentityHashMap<>();
    private final Map<Object, PersonaID> personaIdsByBehavior = new IdentityHashMap<>();
    private final Map<Object, StringBuilder> pendingOutputByBehavior = new IdentityHashMap<>();
    private final Function<Object, String> objectReference;
    private final Consumer<String> deliveredText;
    private final Clock clock;
    private Consumer<AttachmentChange> attachmentListener = ignored -> {};

    /** Creates an owner whose delivered text is also observed by the runtime transcript. */
    public InstanceSessions(Function<Object, String> objectReference, Consumer<String> deliveredText) {
        this(objectReference, deliveredText, Clock.systemUTC());
    }

    /** Creates an owner with an explicit clock for deterministic connection/activity tests. */
    public InstanceSessions(Function<Object, String> objectReference, Consumer<String> deliveredText, Clock clock) {
        this.objectReference = Objects.requireNonNull(objectReference, "objectReference");
        this.deliveredText = Objects.requireNonNull(deliveredText, "deliveredText");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * Observes committed attachment transitions on the instance queue.
     * A listener failure does not undo the completed identity/index change.
     */
    public void setAttachmentListener(Consumer<AttachmentChange> listener) {
        attachmentListener = Objects.requireNonNull(listener, "listener");
    }

    /** Opens a connection before login has selected any Persona. Repeated opening retains its handle. */
    public InstanceSession open(String id, String remoteAddress, Consumer<String> output) {
        SessionID sessionId = new SessionID(id);
        Connection existing = connections.get(sessionId);
        if (existing != null) {
            existing.output = output != null ? output : ignored -> {};
            return existing.handle;
        }
        return open(sessionId, new PlayerID("session-player/" + sessionId.value()), remoteAddress, output).handle;
    }

    private Connection open(SessionID id, PlayerID playerId, String remoteAddress, Consumer<String> output) {
        Instant now = clock.instant();
        SessionRecord record = new SessionRecord(id, playerId, normalizedText(remoteAddress), now);
        Connection connection = new Connection(new InstanceSession(this, id), record,
                output != null ? output : ignored -> {});
        connections.put(id, connection);
        updatePlayer(connection, Optional.empty(), Optional.empty());
        return connection;
    }

    /** Selects a managed login policy while the connection has no attached Persona. */
    public void beginLogin(String id, ManagedLoginSession login) {
        Connection connection = requireConnection(id);
        if (connection.attachment != null) {
            throw new IllegalStateException("An attached session cannot enter login without detaching its Persona.");
        }
        connection.login = Objects.requireNonNull(login, "login");
    }

    /** Returns a stable snapshot of open connections for orderly instance shutdown. */
    public List<InstanceSession> openSessions() {
        return connections.values().stream().map(connection -> connection.handle).toList();
    }

    /** Returns the live handle for the requested connection. */
    public Optional<InstanceSession> session(String id) {
        Connection connection = connection(id);
        return connection == null ? Optional.empty() : Optional.of(connection.handle);
    }

    /** Returns the live connection controlling an authored behavior object. */
    public Optional<InstanceSession> sessionFor(Object behavior) {
        Connection connection = connectionsByBehavior.get(behavior);
        return connection == null ? Optional.empty() : Optional.of(connection.handle);
    }

    /** Returns the current complete Persona attachment, if login has selected one. */
    public Optional<PersonaAttachment> attachment(String id) {
        Connection connection = connection(id);
        return connection == null ? Optional.empty() : Optional.ofNullable(connection.attachment);
    }

    /** Returns the managed login policy selected for an unattached connection. */
    public Optional<ManagedLoginSession> login(String id) {
        Connection connection = connection(id);
        return connection == null ? Optional.empty() : Optional.ofNullable(connection.login);
    }

    /**
     * Attaches or rebinds the selected Persona without replacing the connection's identity.
     *
     * <p>The output route, negotiated protocols and connection timestamps remain with the
     * connection. Captured input belonging to the former behavior is cleared before the new
     * behavior can capture its own input. A second connection selecting the same behavior replaces its former
     * controller rather than leaving two competing attachment indexes.</p>
     */
    public InstanceSession attach(String id, PersonaAttachment selected) {
        Objects.requireNonNull(selected, "selected");
        Connection connection = requireConnection(id);
        Connection previousController = connectionsByBehavior.get(selected.behavior());
        Optional<AttachmentChange> displaced = Optional.empty();
        if (previousController != null && previousController != connection) {
            displaced = closeConnection(previousController, AttachmentChange.Cause.CONNECTION_CLOSED);
        }
        PersonaAttachment previous = connection.attachment;
        if (previous != null && previous.behavior() != selected.behavior()) connection.pendingInput = null;
        detachPersona(connection);
        PlayerID playerId = connection.record.playerId();
        PersonaRecord persona = new PersonaRecord(selected.personaRecord().id(),
                selected.personaRecord().entity(), Optional.of(playerId),
                selected.personaRecord().mudlibBehaviorProjection());
        connection.attachment = new PersonaAttachment(persona, selected.objectId(), selected.behavior(), selected.profile());
        connection.login = null;
        SessionRecord old = connection.record;
        connection.record = new SessionRecord(old.id(), old.playerId(), old.remoteAddress(), old.connectedAt(),
                clock.instant(), Optional.of(persona.id()));
        personas.put(persona.id(), persona);
        personaIdsByBehavior.put(selected.behavior(), persona.id());
        connectionsByBehavior.put(selected.behavior(), connection);
        PlayerRecord previousPlayer = players.get(playerId);
        Optional<Object> profile = previousPlayer != null && previousPlayer.mudlibProfileProjection().isPresent()
                ? previousPlayer.mudlibProfileProjection() : persona.mudlibBehaviorProjection();
        updatePlayer(connection, Optional.of(persona.id()), profile);
        StringBuilder pending = pendingOutputByBehavior.remove(selected.behavior());
        if (pending != null && !pending.isEmpty()) {
            write(connection, pending.toString());
        }
        // Notify only after all indexes agree, including a displaced connection's closure.
        displaced.ifPresent(attachmentListener);
        if (previous == null || previous.behavior() != selected.behavior()) {
            attachmentListener.accept(new AttachmentChange(connection.handle, Optional.ofNullable(previous),
                    Optional.of(connection.attachment), AttachmentChange.Cause.PERSONA_SELECTED));
        }
        return connection.handle;
    }

    /**
     * Opens and attaches a standalone compatibility session when no transport opening preceded it.
     * Hosted transports normally call {@link #open(String, String, Consumer)} first.
     */
    public InstanceSession bind(String id, String remoteAddress, Consumer<String> output, PersonaAttachment selected) {
        Connection connection = connection(id);
        if (connection == null) {
            SessionID sessionId = new SessionID(id);
            connection = open(sessionId, new PlayerID("projection-player/" + objectReference.apply(selected.behavior())),
                    remoteAddress, output);
        } else {
            connection.output = output != null ? output : ignored -> {};
        }
        return attach(id, selected);
    }

    /** Returns the stable Persona id associated with an authored behavior object. */
    public PersonaID personaId(Object behavior) {
        return personaIdsByBehavior.computeIfAbsent(Objects.requireNonNull(behavior, "behavior"),
                object -> new PersonaID("projection-persona/" + objectReference.apply(object)));
    }

    /** Closes a connection and clears every live attachment, input and protocol route it owns. */
    public void close(String id) {
        Connection connection = connection(id);
        if (connection != null) closeConnection(connection, AttachmentChange.Cause.CONNECTION_CLOSED)
                .ifPresent(attachmentListener);
    }

    private Optional<AttachmentChange> closeConnection(Connection connection, AttachmentChange.Cause cause) {
        connections.remove(connection.record.id());
        PersonaAttachment previous = connection.attachment;
        detachPersona(connection);
        PlayerRecord player = players.get(connection.record.playerId());
        if (player != null) {
            Set<SessionID> remaining = new HashSet<>(player.activeSessionIds());
            remaining.remove(connection.record.id());
            players.put(player.id(), new PlayerRecord(player.id(), remaining, Optional.empty(), player.mudlibProfileProjection()));
        }
        connection.login = null;
        connection.pendingInput = null;
        connection.enabledProtocols.clear();
        connection.protocolOutput = (ignoredProtocol, ignoredMessage) -> {};
        return previous == null ? Optional.empty()
                : Optional.of(new AttachmentChange(connection.handle, Optional.of(previous), Optional.empty(), cause));
    }

    private void detachPersona(Connection connection) {
        PersonaAttachment attachment = connection.attachment;
        if (attachment == null) return;
        connectionsByBehavior.remove(attachment.behavior());
        PersonaRecord persona = attachment.personaRecord();
        personas.put(persona.id(), new PersonaRecord(persona.id(), persona.entity(), Optional.empty(),
                persona.mudlibBehaviorProjection()));
        connection.attachment = null;
    }

    /** Drops connection and buffered output state when an authored object is destroyed. */
    public void discardObject(Object behavior) {
        Connection connection = connectionsByBehavior.get(behavior);
        if (connection != null) {
            closeConnection(connection, AttachmentChange.Cause.BEHAVIOR_DESTROYED).ifPresent(attachmentListener);
        }
        pendingOutputByBehavior.remove(behavior);
        for (Connection candidate : connections.values()) {
            if (candidate.pendingInput != null && candidate.pendingInput.handler() == behavior) {
                candidate.pendingInput = null;
            }
        }
    }

    /** Returns attached behaviors that are in ordinary command mode rather than self-captured login. */
    public List<Object> users() {
        return connections.values().stream()
                .filter(connection -> connection.attachment != null)
                .filter(connection -> connection.pendingInput == null
                        || connection.pendingInput.handler() != connection.attachment.behavior())
                .map(connection -> connection.attachment.behavior())
                .toList();
    }

    /** Returns whether an authored object is currently controlled by a connection. */
    public boolean isInteractive(Object behavior) {
        return behavior != null && connectionsByBehavior.containsKey(behavior);
    }

    /** Returns the record of an open connection. */
    public Optional<SessionRecord> sessionRecord(String id) {
        Connection connection = connection(id);
        return connection == null ? Optional.empty() : Optional.of(connection.record);
    }

    /** Returns the Player controlling an open connection. */
    public Optional<PlayerRecord> playerRecordForSession(String id) {
        return sessionRecord(id).map(SessionRecord::playerId).map(players::get);
    }

    /** Returns the last known Persona record, including its detached state after disconnect. */
    public Optional<PersonaRecord> personaRecordForProjection(Object behavior) {
        return Optional.ofNullable(personaIdsByBehavior.get(behavior)).map(personas::get);
    }

    /** Returns the currently attached authored behavior for one open connection. */
    public Optional<Object> objectForSession(String id) {
        return attachment(id).map(PersonaAttachment::behavior);
    }

    /** Writes already formatted text to one open connection. */
    public boolean writeToSession(SessionID id, String text) {
        Connection connection = connections.get(id);
        if (connection == null) return false;
        write(connection, text);
        return true;
    }

    /** Writes already formatted text to every live connection of a Player. */
    public boolean writeToPlayer(PlayerID id, String text) {
        PlayerRecord player = players.get(id);
        if (player == null) return false;
        boolean delivered = false;
        for (SessionID session : player.activeSessionIds()) delivered |= writeToSession(session, text);
        return delivered;
    }

    /** Writes already formatted text to the connection controlling a Persona. */
    public boolean writeToPersona(PersonaID id, String text) {
        PersonaRecord persona = personas.get(id);
        if (persona == null) return false;
        return connections.values().stream()
                .filter(connection -> connection.attachment != null
                        && connection.attachment.personaRecord().id().equals(id))
                .findFirst().map(connection -> { write(connection, text); return true; }).orElse(false);
    }

    /** Writes already formatted text to an authored object's connection, if attached. */
    public boolean writeToObject(Object behavior, String text) {
        Connection connection = connectionsByBehavior.get(behavior);
        if (connection == null) return false;
        write(connection, text);
        return true;
    }

    /** Buffers private authored output until that behavior becomes attached to a connection. */
    public void bufferOutput(Object behavior, String text) {
        if (behavior != null) pendingOutputByBehavior.computeIfAbsent(behavior, ignored -> new StringBuilder()).append(text);
    }

    private void write(Connection connection, String text) {
        deliveredText.accept(text);
        connection.output.accept(text);
    }

    /** Stores one pending authored input callback on the connection, independently of its Persona. */
    public void captureInput(Object persona, Object handler, String method, boolean noEcho, Object... extraArguments) {
        Connection connection = connectionsByBehavior.get(persona);
        if (connection != null) connection.pendingInput = new CapturedInput(handler, method, noEcho, extraArguments);
    }

    /** Returns the callback awaiting the connection's next input line. */
    public Optional<CapturedInput> pendingInput(Object persona) {
        Connection connection = connectionsByBehavior.get(persona);
        return connection == null ? Optional.empty() : Optional.ofNullable(connection.pendingInput);
    }

    /** Clears a delivered callback only when it has not installed a successor while executing. */
    public void completeInput(InstanceSession session, CapturedInput delivered) {
        Connection connection = connection(session.sessionId());
        if (connection != null && connection.handle == session && connection.pendingInput == delivered) {
            connection.pendingInput = null;
        }
    }

    /** Updates connection activity during login as well as ordinary attached play. */
    public void touchSession(String sessionId) {
        Connection connection = connection(sessionId);
        if (connection == null) return;
        SessionRecord old = connection.record;
        connection.record = new SessionRecord(old.id(), old.playerId(), old.remoteAddress(), old.connectedAt(),
                clock.instant(), old.attachedPersonaId());
    }

    /** Updates activity time through an attached authored behavior object. */
    public void touch(Object persona) {
        Connection connection = connectionsByBehavior.get(persona);
        if (connection != null) touchSession(connection.record.id().value());
    }

    /** Returns whole idle seconds for an attached authored object. */
    public int idleSeconds(Object persona) {
        Connection connection = connectionsByBehavior.get(persona);
        return connection == null ? 0 : (int) Math.max(0L,
                (clock.millis() - connection.record.lastActivityAt().toEpochMilli()) / 1000L);
    }

    /** Returns the remote address supplied by the connection's transport. */
    public Optional<String> remoteAddress(Object persona) {
        Connection connection = connectionsByBehavior.get(persona);
        return connection == null ? Optional.empty() : connection.record.remoteAddress();
    }

    /** Installs out-of-band output before login has attached any Persona. */
    public void bindProtocolSink(String sessionId, BiConsumer<String, String> sink) {
        Connection connection = connection(sessionId);
        if (connection != null) connection.protocolOutput = sink != null ? sink : (ignoredProtocol, ignoredMessage) -> {};
    }

    /** Records negotiated protocol state on the connection, including during login. */
    public void setProtocolEnabled(String sessionId, String protocol, boolean enabled) {
        Connection connection = connection(sessionId);
        if (connection == null) return;
        String normalized = normalizeProtocol(protocol);
        if (enabled) connection.enabledProtocols.add(normalized);
        else connection.enabledProtocols.remove(normalized);
    }

    /** Returns protocol state without requiring a selected Persona. */
    public boolean isProtocolEnabled(String sessionId, String protocol) {
        Connection connection = connection(sessionId);
        return connection != null && connection.enabledProtocols.contains(normalizeProtocol(protocol));
    }

    /** Returns an immutable snapshot of negotiated protocols for attachment-time callbacks. */
    public Set<String> enabledProtocols(String sessionId) {
        Connection connection = connection(sessionId);
        return connection == null ? Set.of() : Set.copyOf(connection.enabledProtocols);
    }

    /** Installs the connection's negotiated out-of-band output route. */
    public void bindProtocolSink(Object persona, BiConsumer<String, String> sink) {
        Connection connection = connectionsByBehavior.get(persona);
        if (connection != null) connection.protocolOutput = sink != null ? sink : (ignoredProtocol, ignoredMessage) -> {};
    }

    /** Enables or disables a client protocol for the attached connection. */
    public void setProtocolEnabled(Object persona, String protocol, boolean enabled) {
        Connection connection = connectionsByBehavior.get(persona);
        if (connection == null) return;
        String normalized = normalizeProtocol(protocol);
        if (enabled) connection.enabledProtocols.add(normalized);
        else connection.enabledProtocols.remove(normalized);
    }

    /** Returns whether the attached connection negotiated a protocol. */
    public boolean isProtocolEnabled(Object persona, String protocol) {
        Connection connection = connectionsByBehavior.get(persona);
        return connection != null && connection.enabledProtocols.contains(normalizeProtocol(protocol));
    }

    /** Sends one already encoded message through a negotiated connection protocol. */
    public boolean sendProtocolMessage(Object persona, String protocol, String message) {
        Connection connection = connectionsByBehavior.get(persona);
        String normalized = normalizeProtocol(protocol);
        if (connection == null || !connection.enabledProtocols.contains(normalized)) return false;
        connection.protocolOutput.accept(normalized, Objects.requireNonNull(message, "message"));
        return true;
    }

    private void updatePlayer(Connection connection, Optional<PersonaID> persona, Optional<Object> profile) {
        PlayerID id = connection.record.playerId();
        PlayerRecord existing = players.get(id);
        Set<SessionID> sessions = new HashSet<>();
        if (existing != null) sessions.addAll(existing.activeSessionIds());
        sessions.add(connection.record.id());
        players.put(id, new PlayerRecord(id, sessions, persona, profile));
    }

    private Connection connection(String id) {
        return id == null ? null : connections.get(new SessionID(id));
    }

    private Connection requireConnection(String id) {
        Connection connection = connection(id);
        if (connection == null) throw new IllegalStateException("Session is not open: " + id);
        return connection;
    }

    private static Optional<String> normalizedText(String value) {
        return Optional.ofNullable(value).map(String::trim).filter(text -> !text.isEmpty());
    }

    private static String normalizeProtocol(String protocol) {
        if (protocol == null || protocol.isBlank()) throw new IllegalArgumentException("Client protocol name must not be blank.");
        return protocol.trim().toUpperCase(Locale.ROOT);
    }

    /** A committed transition observed after connection and attachment indexes agree. */
    public record AttachmentChange(
            InstanceSession session,
            Optional<PersonaAttachment> previous,
            Optional<PersonaAttachment> current,
            Cause cause) {
        /** Requires explicit absent/present states rather than null attachment sentinels. */
        public AttachmentChange {
            Objects.requireNonNull(session, "session");
            Objects.requireNonNull(previous, "previous");
            Objects.requireNonNull(current, "current");
            Objects.requireNonNull(cause, "cause");
        }

        /** Explains which lifecycle owner remains responsible for native world cleanup. */
        public enum Cause {
            /** A selected Persona replaces the previous authored behavior, if any. */
            PERSONA_SELECTED,
            /** The connection closes; the instance removes its former world presence. */
            CONNECTION_CLOSED,
            /** Authored destruction already owns ordered inventory movement and world removal. */
            BEHAVIOR_DESTROYED
        }
    }

    /** Immutable callback declaration; the runtime adapter alone invokes its authored handler. */
    public record CapturedInput(Object handler, String methodName, boolean noEcho, Object[] extraArgs) {
        /** Captures arguments without exposing the mutable caller array. */
        public CapturedInput {
            Objects.requireNonNull(handler, "handler");
            Objects.requireNonNull(methodName, "methodName");
            extraArgs = extraArgs == null ? new Object[0] : extraArgs.clone();
        }
        @Override public Object[] extraArgs() { return extraArgs.clone(); }
    }

    /** Mutable connection state is private so indexes cannot be updated independently by callers. */
    private static final class Connection {
        private final InstanceSession handle;
        private SessionRecord record;
        private Consumer<String> output;
        private PersonaAttachment attachment;
        private ManagedLoginSession login;
        private CapturedInput pendingInput;
        private final Set<String> enabledProtocols = new HashSet<>();
        private BiConsumer<String, String> protocolOutput = (ignoredProtocol, ignoredMessage) -> {};

        private Connection(InstanceSession handle, SessionRecord record, Consumer<String> output) {
            this.handle = handle;
            this.record = record;
            this.output = output;
        }
    }
}
