package io.github.protasm.jvmud.cli;

import io.github.protasm.jvmud.compiler.exec.LPCRuntime;
import io.github.protasm.jvmud.runtime.Entity;
import io.github.protasm.jvmud.runtime.Location;
import io.github.protasm.jvmud.runtime.Place;
import io.github.protasm.jvmud.runtime.WorldRuntime;
import java.util.Objects;

/** Host-owned actor for the local CLI session. */
public final class LocalSessionActor {
    private final LPCRuntime runtime;
    private final WorldRuntime worldRuntime;
    private final Entity entity;
    private final String name;

    LocalSessionActor(LPCRuntime runtime, WorldRuntime worldRuntime, Entity entity, String name) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.worldRuntime = Objects.requireNonNull(worldRuntime, "worldRuntime");
        this.entity = Objects.requireNonNull(entity, "entity");
        this.name = Objects.requireNonNull(name, "name");
    }

    Entity entity() {
        return entity;
    }

    public String shortName() {
        return name;
    }

    public String query_name() {
        return name;
    }

    public String query_real_name() {
        return name;
    }

    public int query_level() {
        return 0;
    }

    public int query_invis() {
        return 0;
    }

    public int remove_ghost() {
        return 1;
    }

    public int id(Object value) {
        if (value == null) {
            return 0;
        }
        String text = value.toString();
        return name.equals(text) || "me".equals(text) || "player".equals(text) ? 1 : 0;
    }

    public int move_player(Object directionAndDestination) {
        if (directionAndDestination == null) {
            return 0;
        }

        String movement = directionAndDestination.toString();
        int separator = movement.indexOf('#');
        if (separator == -1 || separator == movement.length() - 1) {
            return 0;
        }

        String action = movement.substring(0, separator).trim();
        String destinationPath = movement.substring(separator + 1);
        Object destination;
        try {
            destination = runtime.loadOrGetObject(destinationPath);
        } catch (RuntimeException e) {
            return 0;
        }
        Place destinationPlace = placeFor(destinationPath);
        Place origin = worldRuntime.placeOf(entity);
        if (origin != null && !action.isEmpty()) {
            worldRuntime.connect(origin, action, destinationPlace);
        }
        worldRuntime.move(entity, destinationPlace);
        runtime.moveObject(this, destination);
        runtime.invokeObject(destination, "long", new Object[] {null});
        return 1;
    }

    private Place placeFor(String path) {
        Location existing = worldRuntime.findLocation(path);
        if (existing instanceof Place place) {
            return place;
        }
        if (existing != null) {
            throw new IllegalArgumentException("Destination is not a place: " + path);
        }
        return worldRuntime.createPlace(path, path);
    }
}
