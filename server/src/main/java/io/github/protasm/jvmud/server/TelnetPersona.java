package io.github.protasm.jvmud.server;

import java.util.Objects;

/** Active server-side binding for one telnet player's current mudlib Persona. */
final class TelnetPersona {
    private TelnetMud mud;
    private String objectId;
    private String name;
    private Object actor;
    private final String sessionId;
    private final String remoteAddress;

    TelnetPersona(TelnetMud mud, String sessionId, String objectId, String name, Object actor, String remoteAddress) {
        this.mud = Objects.requireNonNull(mud, "mud");
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        this.objectId = Objects.requireNonNull(objectId, "objectId");
        this.name = Objects.requireNonNull(name, "name");
        this.actor = Objects.requireNonNull(actor, "actor");
        this.remoteAddress = remoteAddress;
    }

    TelnetMud mud() {
        return mud;
    }

    String sessionId() {
        return sessionId;
    }

    String objectId() {
        return objectId;
    }

    String name() {
        return name;
    }

    Object actor() {
        return actor;
    }

    String remoteAddress() {
        return remoteAddress;
    }

    void replaceWith(TelnetPersona replacement) {
        Objects.requireNonNull(replacement, "replacement");
        this.mud = replacement.mud;
        this.objectId = replacement.objectId;
        this.name = replacement.name;
        this.actor = replacement.actor;
    }
}
