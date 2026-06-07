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

/**
 * Owns the core world ontology, place links, and containment rules for one JVMud world.
 *
 * <p>A {@code WorldRuntime} is the engine-level view of one running world. It deliberately models
 * JVMud concepts directly: places are linked locations, entities are contained by exactly one
 * immediate location, and movement is a containment update optionally driven by a place-to-place
 * link. LPC objects can adapt into this model, but legacy LP driver terms are not required here.</p>
 *
 * <p>Instances are identity-oriented. A {@link Place} or {@link Entity} must have been created by
 * this runtime before it can be used as a location, link endpoint, movement target, or inventory
 * container.</p>
 */
public final class WorldRuntime {
    private final World world;
    private final Map<String, Place> places = new LinkedHashMap<>();
    private final Map<String, Entity> entities = new LinkedHashMap<>();
    private final Map<Entity, Location> locations = new IdentityHashMap<>();
    private final Map<Location, List<Entity>> contents = new IdentityHashMap<>();
    private final Map<Place, Map<String, Link>> links = new IdentityHashMap<>();
    private final WorldScheduler scheduler = new WorldScheduler();
    private MudlibBoundary mudlibBoundary = MudlibBoundary.empty();

    /**
     * Creates a new runtime for a single world.
     *
     * @param world the world metadata owned by this runtime
     */
    public WorldRuntime(World world) {
        this.world = Objects.requireNonNull(world, "world");
    }

    /** Returns the world metadata for this runtime. */
    public World world() {
        return world;
    }

    /** Returns the deterministic scheduler associated with this world. */
    public WorldScheduler scheduler() {
        return scheduler;
    }

    /** Returns the currently registered mudlib boundary, or an empty boundary if none is present. */
    public MudlibBoundary mudlibBoundary() {
        return mudlibBoundary;
    }

    /**
     * Registers the mudlib boundary metadata used by higher-level engine and compiler adapters.
     *
     * @param mudlibBoundary boundary declarations read from JVMud-native mudlib configuration
     */
    public void registerMudlibBoundary(MudlibBoundary mudlibBoundary) {
        this.mudlibBoundary = Objects.requireNonNull(mudlibBoundary, "mudlibBoundary");
    }

    /**
     * Creates a place that can contain entities and act as a link endpoint.
     *
     * @param id stable world-local identifier
     * @param displayName player-facing or admin-facing label
     * @return the created place
     * @throws IllegalArgumentException if any known place or entity already has the same id
     */
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

    /**
     * Creates an entity at an initial location.
     *
     * <p>Every entity is immediately located. The initial location may be a place or another entity,
     * but it must already belong to this runtime.</p>
     *
     * @param id stable world-local identifier
     * @param displayName player-facing or admin-facing label
     * @param initialLocation known place or entity that will contain the new entity
     * @param capabilities optional engine-level capabilities
     * @return the created entity
     */
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

    /** Finds a place or entity by id, returning {@code null} when no matching location exists. */
    public Location findLocation(String id) {
        Location place = places.get(id);
        return place != null ? place : entities.get(id);
    }

    /** Returns a known place by id, or {@code null} if the id does not name a place. */
    public Place place(String id) {
        return places.get(id);
    }

    /** Returns a known entity by id, or {@code null} if the id does not name an entity. */
    public Entity entity(String id) {
        return entities.get(id);
    }

    /** Returns the immediate location of an entity, or {@code null} if it has not been located. */
    public Location locationOf(Entity entity) {
        return locations.get(Objects.requireNonNull(entity, "entity"));
    }

    /**
     * Returns the entities immediately contained by a location.
     *
     * @throws IllegalArgumentException if the location was not created by this runtime
     */
    public List<Entity> contentsOf(Location location) {
        ensureKnownLocation(location);
        return Collections.unmodifiableList(contents.getOrDefault(location, List.of()));
    }

    /** Creates a visible link from one place to another. */
    public Link connect(Place origin, String action, Place destination) {
        return connect(origin, action, destination, true);
    }

    /**
     * Creates or replaces a link from one place to another for a normalized action string.
     *
     * @param origin known place where traversal begins
     * @param action command/action that selects the link
     * @param destination known place reached by traversal
     * @param visible whether the link should be visible to ordinary perception
     * @return the created link
     */
    public Link connect(Place origin, String action, Place destination, boolean visible) {
        ensureKnownPlace(origin);
        ensureKnownPlace(destination);
        Link link = new Link(action, origin, destination, visible);
        links.get(origin).put(link.action(), link);
        return link;
    }

    /** Creates two visible links that connect the supplied places in opposite directions. */
    public void connectBothWays(Place first, String firstAction, Place second, String secondAction) {
        connect(first, firstAction, second);
        connect(second, secondAction, first);
    }

    /** Returns the link for an action from a place, or {@code null} when no such link exists. */
    public Link linkFrom(Place origin, String action) {
        ensureKnownPlace(origin);
        return links.get(origin).get(normalizeAction(action));
    }

    /** Returns the currently registered outbound links from a place in insertion order. */
    public List<Link> linksFrom(Place origin) {
        ensureKnownPlace(origin);
        return List.copyOf(links.get(origin).values());
    }

    /**
     * Returns the containing place for an entity, walking through entity containers as needed.
     *
     * @return the nearest containing place, or {@code null} if the entity is not ultimately in one
     */
    public Place placeOf(Entity entity) {
        Location location = locationOf(entity);
        while (location instanceof Entity container) {
            location = locationOf(container);
        }
        return location instanceof Place place ? place : null;
    }

    /**
     * Moves an entity across the link selected by an action from its containing place.
     *
     * @return {@code true} if a link was found and movement occurred
     */
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

    /**
     * Moves an entity into a known destination location.
     *
     * <p>Movement updates both the entity's immediate location and the contents lists of the old and
     * new containers. Moving an entity into itself, directly or through nested containment, is
     * rejected.</p>
     */
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
