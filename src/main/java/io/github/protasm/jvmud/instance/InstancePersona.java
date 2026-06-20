package io.github.protasm.jvmud.instance;

import java.util.Objects;

/** Active server-side binding for one telnet player's current mudlib Persona. */
public final class InstancePersona {
    private MudInstance mud;
    private String objectId;
    private String name;
    private String userId;
    private String gender;
    private Object actor;
    private final String sessionId;
    private final String remoteAddress;

    InstancePersona(MudInstance mud, String sessionId, String objectId, String name, Object actor, String remoteAddress) {
        this(mud, sessionId, objectId, name, name, "", actor, remoteAddress);
    }

    InstancePersona(
            MudInstance mud,
            String sessionId,
            String objectId,
            String name,
            String userId,
            String gender,
            Object actor,
            String remoteAddress) {
        this.mud = Objects.requireNonNull(mud, "mud");
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        this.objectId = Objects.requireNonNull(objectId, "objectId");
        this.name = Objects.requireNonNull(name, "name");
        this.userId = Objects.requireNonNull(userId, "userId");
        this.gender = Objects.requireNonNullElse(gender, "");
        this.actor = Objects.requireNonNull(actor, "actor");
        this.remoteAddress = remoteAddress;
    }

    MudInstance mud() {
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

    String userId() {
        return userId;
    }

    String gender() {
        return gender;
    }

    Object actor() {
        return actor;
    }

    String remoteAddress() {
        return remoteAddress;
    }

    void replaceWith(InstancePersona replacement) {
        Objects.requireNonNull(replacement, "replacement");
        this.mud = replacement.mud;
        this.objectId = replacement.objectId;
        this.name = replacement.name;
        this.userId = replacement.userId;
        this.gender = replacement.gender;
        this.actor = replacement.actor;
    }
}
