package io.github.protasm.jvmud.engine.world;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Identity adapter between opaque mudlib objects and JVMud-native world locations.
 *
 * <p>The adapter contains no second containment graph. It only records projection identity;
 * location and contents queries are always answered by the associated {@link WorldRuntime}.</p>
 */
public final class MudlibWorldProjection {
    private final WorldRuntime worldRuntime;
    private final Map<Object, Location> locationsByObject = new IdentityHashMap<>();
    private final Map<Location, Object> objectsByLocation = new IdentityHashMap<>();
    private long nextId = 1;

    /** Creates an identity adapter backed by the supplied authoritative world runtime. */
    public MudlibWorldProjection(WorldRuntime worldRuntime) {
        this.worldRuntime = Objects.requireNonNull(worldRuntime, "worldRuntime");
    }

    /** Returns the authoritative world runtime used by this adapter. */
    public WorldRuntime worldRuntime() {
        return worldRuntime;
    }

    /** Binds a mudlib object to an existing native place. */
    public void bindPlace(Object object, Place place) {
        requireBindingAvailable(object, place);
        Location existing = locationsByObject.get(object);
        if (existing instanceof Entity entity) {
            for (Entity child : List.copyOf(worldRuntime.contentsOf(entity))) {
                worldRuntime.move(child, place);
            }
            objectsByLocation.remove(entity);
            locationsByObject.remove(object);
            worldRuntime.removeEntity(entity.id());
        }
        bind(object, place);
    }

    /** Binds a mudlib object to an existing native entity. */
    public void bindEntity(Object object, Entity entity) {
        requireBindingAvailable(object, entity);
        Location existing = locationsByObject.get(object);
        if (existing instanceof Entity prior && prior != entity) {
            for (Entity child : List.copyOf(worldRuntime.contentsOf(prior))) {
                worldRuntime.move(child, entity);
            }
            objectsByLocation.remove(prior);
            locationsByObject.remove(object);
            worldRuntime.removeEntity(prior.id());
        }
        bind(object, entity);
    }

    /** Moves an opaque mudlib object using the native containment graph. */
    public void move(Object object, Object destination) {
        Entity entity = entityFor(object);
        if (destination == null) {
            worldRuntime.detach(entity);
            return;
        }
        worldRuntime.move(entity, locationFor(destination));
    }

    /** Returns the opaque object that immediately contains the supplied object. */
    public Object environment(Object object) {
        Location location = locationsByObject.get(object);
        if (!(location instanceof Entity entity)) {
            return null;
        }
        return objectsByLocation.get(worldRuntime.locationOf(entity));
    }

    /** Returns the opaque objects immediately contained by the supplied object. */
    public List<Object> inventory(Object object) {
        Location location = locationsByObject.get(object);
        if (location == null) {
            return List.of();
        }
        return worldRuntime.contentsOf(location).stream()
                .map(objectsByLocation::get)
                .filter(Objects::nonNull)
                .toList();
    }

    /** Removes an opaque entity projection from the native world. */
    public void remove(Object object) {
        Location location = locationsByObject.remove(object);
        if (location == null) {
            return;
        }
        objectsByLocation.remove(location);
        if (location instanceof Entity entity) {
            for (Entity child : List.copyOf(worldRuntime.contentsOf(entity))) {
                worldRuntime.detach(child);
            }
            worldRuntime.removeEntity(entity.id());
        }
    }

    private Entity entityFor(Object object) {
        Objects.requireNonNull(object, "object");
        Location existing = locationsByObject.get(object);
        if (existing instanceof Entity entity) {
            return entity;
        }
        if (existing instanceof Place) {
            throw new IllegalArgumentException("A mudlib place projection cannot be moved as an entity.");
        }
        Entity entity = worldRuntime.createDetachedEntity(nextId("entity"), "mudlib entity");
        bind(object, entity);
        return entity;
    }

    private Location locationFor(Object object) {
        Location existing = locationsByObject.get(object);
        if (existing != null) {
            return existing;
        }
        Entity container = worldRuntime.createDetachedEntity(nextId("container"), "mudlib container");
        bind(object, container);
        return container;
    }

    private void bind(Object object, Location location) {
        Objects.requireNonNull(object, "object");
        Objects.requireNonNull(location, "location");
        Location priorLocation = locationsByObject.get(object);
        if (priorLocation != null && priorLocation != location) {
            throw new IllegalArgumentException("Mudlib object is already bound to a different world location.");
        }
        Object priorObject = objectsByLocation.get(location);
        if (priorObject != null && priorObject != object) {
            throw new IllegalArgumentException("World location is already bound to a different mudlib object.");
        }
        locationsByObject.put(object, location);
        objectsByLocation.put(location, object);
    }

    private void requireBindingAvailable(Object object, Location location) {
        Objects.requireNonNull(object, "object");
        Objects.requireNonNull(location, "location");
        Object priorObject = objectsByLocation.get(location);
        if (priorObject != null && priorObject != object) {
            throw new IllegalArgumentException("World location is already bound to a different mudlib object.");
        }
    }

    private String nextId(String kind) {
        String id;
        do {
            id = "jvmud:" + kind + ":" + nextId++;
        } while (worldRuntime.findLocation(id) != null);
        return id;
    }
}
