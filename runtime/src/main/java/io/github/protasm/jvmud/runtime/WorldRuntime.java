package io.github.protasm.jvmud.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Owns the core world ontology, place links, and containment rules for one JVMud world. */
public final class WorldRuntime {
    private final World world;
    private final Map<String, Place> places = new LinkedHashMap<>();
    private final Map<String, Entity> entities = new LinkedHashMap<>();
    private final Map<Entity, Location> locations = new IdentityHashMap<>();
    private final Map<Location, List<Entity>> contents = new IdentityHashMap<>();
    private final Map<Place, Map<String, Link>> links = new IdentityHashMap<>();
    private final WorldScheduler scheduler = new WorldScheduler();

    public WorldRuntime(World world) {
        this.world = Objects.requireNonNull(world, "world");
    }

    public World world() {
        return world;
    }

    public WorldScheduler scheduler() {
        return scheduler;
    }

    public Place createPlace(String id, String displayName) {
        if (places.containsKey(id) || entities.containsKey(id)) {
            throw new IllegalArgumentException("A location already exists with id: " + id);
        }
        Place place = new Place(id, displayName);
        places.put(place.id(), place);
        contents.put(place, new ArrayList<>());
        links.put(place, new LinkedHashMap<>());
        return place;
    }

    public Entity createEntity(String id, String displayName, Location initialLocation, Capability... capabilities) {
        Objects.requireNonNull(initialLocation, "initialLocation");
        if (places.containsKey(id) || entities.containsKey(id)) {
            throw new IllegalArgumentException("A location already exists with id: " + id);
        }
        ensureKnownLocation(initialLocation);
        Entity entity = new Entity(id, displayName, capabilitySet(capabilities));
        entities.put(entity.id(), entity);
        contents.put(entity, new ArrayList<>());
        move(entity, initialLocation);
        return entity;
    }

    public Location findLocation(String id) {
        Location place = places.get(id);
        return place != null ? place : entities.get(id);
    }

    public Place place(String id) {
        return places.get(id);
    }

    public Entity entity(String id) {
        return entities.get(id);
    }

    public Location locationOf(Entity entity) {
        return locations.get(Objects.requireNonNull(entity, "entity"));
    }

    public List<Entity> contentsOf(Location location) {
        ensureKnownLocation(location);
        return Collections.unmodifiableList(contents.getOrDefault(location, List.of()));
    }

    public Link connect(Place origin, String action, Place destination) {
        return connect(origin, action, destination, true);
    }

    public Link connect(Place origin, String action, Place destination, boolean visible) {
        ensureKnownPlace(origin);
        ensureKnownPlace(destination);
        Link link = new Link(action, origin, destination, visible);
        links.get(origin).put(link.action(), link);
        return link;
    }

    public void connectBothWays(Place first, String firstAction, Place second, String secondAction) {
        connect(first, firstAction, second);
        connect(second, secondAction, first);
    }

    public Link linkFrom(Place origin, String action) {
        ensureKnownPlace(origin);
        return links.get(origin).get(normalizeAction(action));
    }

    public List<Link> linksFrom(Place origin) {
        ensureKnownPlace(origin);
        return List.copyOf(links.get(origin).values());
    }

    public Place placeOf(Entity entity) {
        Location location = locationOf(entity);
        while (location instanceof Entity container) {
            location = locationOf(container);
        }
        return location instanceof Place place ? place : null;
    }

    public boolean traverse(Entity entity, String action) {
        Objects.requireNonNull(entity, "entity");
        Place origin = placeOf(entity);
        if (origin == null) {
            return false;
        }
        Link link = linkFrom(origin, action);
        if (link == null) {
            return false;
        }
        move(entity, link.destination());
        return true;
    }

    public void move(Entity entity, Location destination) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(destination, "destination");
        ensureKnownEntity(entity);
        ensureKnownLocation(destination);
        if (wouldCreateContainmentCycle(entity, destination)) {
            throw new IllegalArgumentException(
                    "Cannot move " + entity.id() + " into " + destination.id()
                            + " because it would create a containment cycle.");
        }

        Location oldLocation = locations.remove(entity);
        if (oldLocation != null) {
            contents.get(oldLocation).remove(entity);
        }

        locations.put(entity, destination);
        contents.get(destination).add(entity);
    }

    private boolean wouldCreateContainmentCycle(Entity entity, Location destination) {
        Location current = destination;
        while (current instanceof Entity currentEntity) {
            if (currentEntity == entity) {
                return true;
            }
            current = locations.get(currentEntity);
        }
        return false;
    }

    private void ensureKnownLocation(Location location) {
        Objects.requireNonNull(location, "location");
        if (location instanceof Place place && places.get(place.id()) == place) {
            return;
        }
        if (location instanceof Entity entity && entities.get(entity.id()) == entity) {
            return;
        }
        throw new IllegalArgumentException("Unknown location: " + location.id());
    }

    private void ensureKnownPlace(Place place) {
        Objects.requireNonNull(place, "place");
        if (places.get(place.id()) != place) {
            throw new IllegalArgumentException("Unknown place: " + place.id());
        }
    }

    private void ensureKnownEntity(Entity entity) {
        if (entities.get(entity.id()) != entity) {
            throw new IllegalArgumentException("Unknown entity: " + entity.id());
        }
    }

    private Set<Capability> capabilitySet(Capability[] capabilities) {
        if (capabilities == null || capabilities.length == 0) {
            return Set.of();
        }
        EnumSet<Capability> values = EnumSet.noneOf(Capability.class);
        Collections.addAll(values, capabilities);
        return values;
    }

    private String normalizeAction(String action) {
        Objects.requireNonNull(action, "action");
        return action.trim();
    }
}
