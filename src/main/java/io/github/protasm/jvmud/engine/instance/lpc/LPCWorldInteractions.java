package io.github.protasm.jvmud.engine.instance.lpc;

import io.github.protasm.jvmud.engine.model.mudlib.MudlibLifecycleEvent;
import io.github.protasm.jvmud.engine.model.world.MudlibWorldProjection;
import io.github.protasm.jvmud.engine.model.world.PerceptionEvent;
import io.github.protasm.jvmud.engine.model.world.World;
import io.github.protasm.jvmud.engine.model.world.WorldRuntime;
import io.github.protasm.jvmud.language.runtime.Truth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Coordinates authored LPC behavior around the native world model for one hosted mudlib.
 *
 * <p>{@link WorldRuntime} owns containment and movement validity. {@link MudlibWorldProjection} adapts opaque LPC
 * identities into that graph. This owner uses that projection for movement, nearby-object
 * interactions, presence and perception. It holds aliases, light and translucence as metadata on
 * LPC identities; it maintains no second containment graph.</p>
 *
 * <p>Movement commits containment before departure code runs. Departure may redirect or destroy
 * the actor, so arrival starts only when its original destination is still current. Arrival and
 * explicit command refresh retain distinct traversal and registration scopes. Hook failures unwind
 * execution scopes without rolling back world or authored state that has already changed.</p>
 *
 * <p>Invocation, actor scopes and reload receivers retain their existing owners. The host supplies
 * loading, lifecycle policy, runtime binding and session-facing output. Only movement lifecycle
 * calls establish a runtime binding here; perception inherits the caller's existing binding.
 * Hosted mutations remain confined to the instance execution queue.</p>
 */
final class LPCWorldInteractions {
    private final LPCObjectLifecycle objectLifecycle;
    private final LPCInvocationStack invocationStack;
    private final LPCMethodInvoker methodInvoker;
    private final LPCCommands commands;
    private final Host host;
    private MudlibWorldProjection projection = new MudlibWorldProjection(
            new WorldRuntime(new World("mudlib-runtime", "Mudlib Runtime")));
    // These indexes agree: namespace -> alias -> object, and object -> namespace -> alias.
    // Replacing an alias removes its previous owner's reverse entry before later cleanup can use it.
    private final Map<String, Map<String, Object>> entityAliases = new LinkedHashMap<>();
    private final Map<Object, Map<String, String>> aliasesByEntity = new IdentityHashMap<>();
    private final Map<Object, Integer> lightLevels = new IdentityHashMap<>();
    private final Set<Object> opaqueEntities = Collections.newSetFromMap(new IdentityHashMap<>());
    // Nested speech is legitimate. Bound cycles explicitly instead of suppressing responses.
    private int perceptionDepth;

    /** Shares existing invocation owners and a focused bridge to hosted policy and services. */
    LPCWorldInteractions(LPCObjectLifecycle objectLifecycle, LPCInvocationStack invocationStack,
            LPCMethodInvoker methodInvoker, LPCCommands commands, Host host) {
        this.objectLifecycle = Objects.requireNonNull(objectLifecycle, "objectLifecycle");
        this.invocationStack = Objects.requireNonNull(invocationStack, "invocationStack");
        this.methodInvoker = Objects.requireNonNull(methodInvoker, "methodInvoker");
        this.commands = Objects.requireNonNull(commands, "commands");
        this.host = Objects.requireNonNull(host, "host");
    }

    /** Drops projection and world metadata after the host has detached the object and its contents. */
    void discardObject(Object object) {
        projection.remove(object);
        lightLevels.remove(object);
        opaqueEntities.remove(object);
        removeEntityAliases(object);
    }

    /**
     * Rebuilds actions for an explicitly selected actor in one registration scope. The hosting
     * runtime supplies its configured hook and binding; this pass also applies to detached actors.
     * Unlike arrival, it includes the actor again when traversing the environment's inventory.
     */
    void refreshCommandActions(Object actor, Consumer<Object> interactionHook) {
        commands.withActor(actor, () -> {
            commands.clearTemporaryActions(actor);
            commands.clearPendingActionMethods();
            try {
                return commands.withScopedRegistration(() -> {
                    interactionHook.accept(actor);
                    Object environment = environment(actor);
                    if (environment != null) {
                        interactionHook.accept(environment);
                        forEachInventory(environment, interactionHook);
                    }
                    forEachInventory(actor, interactionHook);
                    return null;
                });
            } finally {
                commands.clearPendingActionMethods();
            }
        });
    }

    /** Prefetches the next sibling before a hook can move it; this is deliberately not a full snapshot. */
    private void forEachInventory(Object container, Consumer<Object> action) {
        Object child = firstInventory(container);
        while (child != null) {
            Object next = nextInventory(child);
            action.accept(child);
            child = next;
        }
    }

    /**
     * Selects the native world projection that owns mudlib containment state.
     *
     * <p>Hosted instances set this during boot before loading world objects. Unstarted mudlib hosts
     * retain an isolated native world by default.</p>
     */
    void setProjection(MudlibWorldProjection projection) {
        this.projection = Objects.requireNonNull(projection, "worldProjection");
    }

    /** Returns the native world projection currently used for LPC containment operations. */
    MudlibWorldProjection projection() {
        return projection;
    }

    /** Emits text to the emitter's location and immediate neighbors, excluding the emitter. */
    void emitPerceivable(Object emitter, Object value) {
        emitEvent(emitter, "text", value, host.messageText(value));
    }

    /** Emits text while excluding the emitter and one supplied object reference. */
    void emitPerceivableExcept(Object emitter, Object value, Object excluded) {
        emitAt(environment(emitter), new PerceptionEvent(commands.currentActor(), emitter,
                environment(emitter), "text", value, host.messageText(value), false), emitter, excluded);
    }

    /** Emits text to the location and its immediate contents without a source exclusion. */
    void emitPerceivableAt(Object location, Object value) {
        emitAt(location, new PerceptionEvent(commands.currentActor(), invocationStack.currentObject(), location,
                "text", value, host.messageText(value), false));
    }

    /** Emits a structured event to the location and its immediate contents, excluding the source. */
    void emitEvent(Object source, String kind, Object content, String text) {
        Object location = environment(source);
        emitAt(location, new PerceptionEvent(commands.currentActor(), source, location,
                kind, content, text, false), source);
    }

    /** Directed world communication, distinct from private interface output. */
    void deliverEvent(Object target, String kind, Object content, String text) {
        if (target != null) {
            perceive(target, new PerceptionEvent(commands.currentActor(), invocationStack.currentObject(), environment(target),
                    kind, content, text, true));
        }
    }

    /** Captures location-first observers once; each delivery keeps its own destruction checks. */
    private void emitAt(Object location, PerceptionEvent event, Object... excluded) {
        if (location == null) return;
        List<Object> observers = new ArrayList<>();
        observers.add(location);
        observers.addAll(List.copyOf(inventory(location)));
        for (Object observer : observers) {
            if (!isExcluded(observer, excluded)) perceive(observer, event);
        }
    }

    /**
     * Delivers session text before authored hooks, inheriting the existing runtime binding.
     * Receiver and boundary hooks read current policy separately and receive fresh event mappings.
     */
    private void perceive(Object observer, PerceptionEvent event) {
        if (host.isDestroyedIdentity(observer)) return;
        if (perceptionDepth >= 64) throw new IllegalStateException("Perception callback nesting exceeds 64");
        perceptionDepth++;
        try {
            if (host.isInteractive(observer)) host.writePerceptionText(observer, event.text());
            Supplier<Object> delivery = () -> {
                String receiver = host.lifecycleMethod(MudlibLifecycleEvent.PERCEPTION_RECEIVED);
                if (receiver != null) methodInvoker.invokeOptional(observer, receiver, event.toMapping());
                String adapter = host.lifecycleMethod(MudlibLifecycleEvent.PERCEPTION_DELIVERY);
                if (adapter != null && !host.isDestroyedIdentity(observer)) {
                    Object boundary = host.perceptionBoundaryObject();
                    if (boundary != null) methodInvoker.invokeOptional(boundary, adapter, observer, event.toMapping());
                }
                return null;
            };
            if (event.actor() == null) delivery.get();
            else commands.withActor(event.actor(), delivery);
        } finally {
            perceptionDepth--;
        }
    }

    /** Exclusions compare the original references without canonicalization or equality matching. */
    private boolean isExcluded(Object target, Object... excluded) {
        for (Object object : excluded) {
            if (target == object) {
                return true;
            }
        }
        return false;
    }

    /**
     * Moves one LPC object and immediately starts the destination interaction scope for a connected
     * command actor.
     *
     * <p>Movement is itself a lifecycle boundary. A room and the nearby objects must observe a
     * connected actor's arrival before that actor can issue another command; deferring their
     * lifecycle methods until the next command can let the actor leave before entrance events and
     * temporary interactions are installed.</p>
     */
    void move(Object object, Object destination) {
        Objects.requireNonNull(object, "object");
        if (destination instanceof String path) {
            destination = host.loadOrGetObject(path);
        }
        if (destination != null && wouldCreateContainmentCycle(object, destination)) {
            throw new IllegalArgumentException(
                    "Cannot move " + objectIdOrDescription(object) + " into "
                            + objectIdOrDescription(destination) + " because it would create a containment cycle.");
        }

        Object previousLocation = projection.environment(object);
        if (previousLocation == destination) {
            return;
        }

        projection.move(object, destination);
        if (previousLocation != null
                && (host.isInteractive(object) || commands.isEnabled(object))) {
            invokeDepartureLifecycle(object, previousLocation);
        }
        // Departure cleanup may destroy or redirect the actor. Do not deliver a stale arrival.
        if (destination != null && !host.isDestroyedIdentity(object)
                && projection.environment(object) == destination) {
            invokeArrivalLifecycle(object, destination);
        }
    }

    /** Selects exact one-argument departure before exact zero-argument departure, after graph mutation. */
    private void invokeDepartureLifecycle(Object actor, Object previousLocation) {
        String methodName = host.lifecycleMethod(MudlibLifecycleEvent.ENTITY_DEPARTED_FROM_PLACE);
        if (methodName == null) return;
        boolean acceptsActor = methodInvoker.hasMethod(objectLifecycle.receiverOf(previousLocation).getClass(), methodName, 1);
        if (!acceptsActor && !methodInvoker.hasMethod(objectLifecycle.receiverOf(previousLocation).getClass(), methodName, 0)) return;

        host.withRuntimeContext(() -> {
            commands.withActor(actor, () -> acceptsActor
                    ? methodInvoker.invokeRequired(previousLocation, methodName, actor)
                    : methodInvoker.invokeRequired(previousLocation, methodName));
        });
    }

    /** Preserves phase order and fresh snapshots; each handler enters its own interaction scope. */
    private void invokeArrivalLifecycle(Object object, Object destination) {
        if (host.isInteractive(destination)) {
            invokeInteractionLifecycle(destination, object);
        }

        for (Object nearby : List.copyOf(inventory(destination))) {
            if (nearby != object && host.isInteractive(nearby)) {
                invokeInteractionLifecycle(nearby, object);
            }
        }

        if (host.isInteractive(object)) {
            commands.clearTemporaryActions(object);
            commands.clearPendingActionMethods();
            try {
                invokeInteractionLifecycle(object, object);
                invokeInteractionLifecycle(object, destination);
                for (Object nearby : List.copyOf(inventory(destination))) {
                    if (nearby != object) {
                        invokeInteractionLifecycle(object, nearby);
                    }
                }
                for (Object carried : List.copyOf(inventory(object))) {
                    invokeInteractionLifecycle(object, carried);
                }
            } finally {
                commands.clearPendingActionMethods();
            }
        }
    }

    /** Runs one configured interaction hook with movement's runtime, actor and registration scopes. */
    private void invokeInteractionLifecycle(Object actor, Object handler) {
        String methodName = host.lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED);
        if (methodName == null) {
            return;
        }

        host.withRuntimeContext(() -> {
            commands.withActor(actor, () -> commands.withScopedRegistration(() -> {
                methodInvoker.invokeLifecycle(handler, methodName);
                return null;
            }));
        });
    }

    /** Returns immediate containment, using the lexical object when no object is supplied. */
    Object environment(Object object) {
        Object target = (object != null) ? object : invocationStack.currentObject();
        return (target != null) ? projection.environment(target) : null;
    }

    /** Searches immediate inventory; an omitted container also permits the lexical object's environment. */
    Object present(Object identifier, Object container) {
        Object targetContainer = (container != null) ? container : invocationStack.currentObject();
        if (targetContainer == null) {
            return null;
        }

        Object found = findPresent(identifier, targetContainer);
        if (found != null) {
            return found;
        }

        if (container == null) {
            Object ambientContainer = environment(targetContainer);
            if (ambientContainer != null && ambientContainer != targetContainer) {
                return findPresent(identifier, ambientContainer);
            }
        }

        return null;
    }

    private Object findPresent(Object identifier, Object container) {
        for (Object object : inventory(container)) {
            if (object == identifier || matchesIdentifier(object, identifier)) {
                return object;
            }
        }
        return null;
    }

    /** Replaces an object's namespaced alias, retaining the existing normalization and ownership rules. */
    void bindAlias(Object object, Object namespace, Object alias) {
        if (object == null) {
            return;
        }

        String normalizedNamespace = normalizeRegistryText(namespace);
        if (normalizedNamespace == null) {
            return;
        }

        Map<String, String> aliases = aliasesByEntity.computeIfAbsent(object, ignored -> new LinkedHashMap<>());
        String existing = aliases.remove(normalizedNamespace);
        if (existing != null) {
            Map<String, Object> namespaceAliases = entityAliases.get(normalizedNamespace);
            if (namespaceAliases != null) {
                namespaceAliases.remove(existing);
                if (namespaceAliases.isEmpty()) {
                    entityAliases.remove(normalizedNamespace);
                }
            }
        }

        String normalizedAlias = normalizeRegistryText(alias);
        if (normalizedAlias == null) {
            if (aliases.isEmpty()) {
                aliasesByEntity.remove(object);
            }
            return;
        }

        aliases.put(normalizedNamespace, normalizedAlias);
        Object previousOwner = entityAliases
                .computeIfAbsent(normalizedNamespace, ignored -> new LinkedHashMap<>())
                .put(normalizedAlias, object);
        // A replaced object must no longer own this alias, including during later cleanup.
        if (previousOwner != null && previousOwner != object) {
            Map<String, String> previousAliases = aliasesByEntity.get(previousOwner);
            if (previousAliases != null) {
                previousAliases.remove(normalizedNamespace);
                if (previousAliases.isEmpty()) {
                    aliasesByEntity.remove(previousOwner);
                }
            }
        }
    }

    /** Looks up a normalized namespaced alias without loading or realizing an object. */
    Object findAlias(Object namespace, Object alias) {
        String normalizedNamespace = normalizeRegistryText(namespace);
        String normalizedAlias = normalizeRegistryText(alias);
        if (normalizedNamespace == null || normalizedAlias == null) {
            return null;
        }
        return entityAliases.getOrDefault(normalizedNamespace, Map.of()).get(normalizedAlias);
    }

    /** Returns LPC status for an object's alias in the normalized namespace. */
    int entityHasAlias(Object object, Object namespace) {
        String normalizedNamespace = normalizeRegistryText(namespace);
        if (object == null || normalizedNamespace == null) {
            return 0;
        }
        return aliasesByEntity.getOrDefault(object, Map.of()).containsKey(normalizedNamespace) ? 1 : 0;
    }

    /** Selects whether the object's contents contribute to LPC ambient light. */
    void setTranslucent(Object object, boolean translucent) {
        if (object == null) {
            return;
        }
        if (translucent) {
            opaqueEntities.remove(object);
        } else {
            opaqueEntities.add(object);
        }
    }

    /** Reports LPC light transparency; null and unconfigured objects are translucent. */
    boolean isTranslucent(Object object) {
        return object == null || !opaqueEntities.contains(object);
    }

    /** Returns the first object in the native projection's immediate inventory, or null. */
    Object firstInventory(Object container) {
        List<Object> inventory = inventory(container);
        return inventory.isEmpty() ? null : inventory.get(0);
    }

    /** Returns the next inventory entry in the containing location, or null. */
    Object nextInventory(Object object) {
        Object container = environment(object);
        if (container == null) {
            return null;
        }

        List<Object> inventory = inventory(container);
        int index = inventory.indexOf(object);
        if (index == -1 || index + 1 >= inventory.size()) {
            return null;
        }
        return inventory.get(index + 1);
    }

    /** Updates the lexical object's light and returns visible light from its outermost container. */
    int setLight(int delta) {
        Object object = invocationStack.currentObject();
        if (object == null) {
            return delta;
        }
        int light = lightLevels.getOrDefault(object, 0);
        if (delta != 0) {
            light += delta;
            lightLevels.put(object, light);
        }
        return visibleLight(object);
    }

    private int visibleLight(Object object) {
        return visibleLight(object, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private int visibleLight(Object object, Set<Object> visited) {
        Object root = outermostEnvironment(object);
        return visibleLightIn(root, visited);
    }

    private Object outermostEnvironment(Object object) {
        Object root = object;
        Object current = projection.environment(object);
        while (current != null) {
            root = current;
            current = projection.environment(current);
        }
        return root;
    }

    private int visibleLightIn(Object object, Set<Object> visited) {
        if (object == null || !visited.add(object)) {
            return 0;
        }

        int light = lightLevels.getOrDefault(object, 0);
        if (!isTranslucent(object)) {
            return light;
        }
        for (Object child : inventory(object)) {
            light += visibleLightIn(child, visited);
        }
        return light;
    }

    /** Returns the native projection's immediate inventory snapshot, also used during destruction. */
    List<Object> inventory(Object object) {
        return projection.inventory(object);
    }

    private boolean wouldCreateContainmentCycle(Object object, Object destination) {
        Object current = destination;
        while (current != null) {
            if (current == object) {
                return true;
            }
            current = projection.environment(current);
        }
        return false;
    }

    private void removeEntityAliases(Object object) {
        Map<String, String> aliases = aliasesByEntity.remove(object);
        if (aliases == null) {
            return;
        }
        for (Map.Entry<String, String> alias : aliases.entrySet()) {
            Map<String, Object> namespaceAliases = entityAliases.get(alias.getKey());
            if (namespaceAliases == null) {
                continue;
            }
            namespaceAliases.remove(alias.getValue());
            if (namespaceAliases.isEmpty()) {
                entityAliases.remove(alias.getKey());
            }
        }
    }

    /** Retains the existing trim and default-locale lowercase behavior for alias compatibility. */
    private String normalizeRegistryText(Object value) {
        if (value == null) {
            return null;
        }
        String normalized = value.toString().trim().toLowerCase();
        return normalized.isEmpty() ? null : normalized;
    }

    /** An existing identifier method decides the match; only absence permits canonical-ID fallback. */
    private boolean matchesIdentifier(Object object, Object identifier) {
        if (identifier == null) {
            return false;
        }
        String id = identifier.toString();
        if (id.isBlank()) {
            return false;
        }

        try {
            Object receiver = objectLifecycle.receiverOf(object);
            LPCMethodInvoker.InvocationPlan invocation = methodInvoker.findInvocation(
                    receiver.getClass(), "id", new Object[] {id});
            return invocationStack.withCurrentObject(object, () -> {
                try {
                    return Truth.isTruthy(invocation.invoke(receiver));
                } catch (ReflectiveOperationException e) {
                    return false;
                }
            });
        } catch (NoSuchMethodException e) {
            // Fall back to exact object id matching below.
        }

        return id.equals(host.objectId(object));
    }

    private String objectIdOrDescription(Object object) {
        String id = host.objectId(object);
        return id != null ? id : String.valueOf(object);
    }

    /** Host policy and services, leaving object registration, sessions and private output outside this owner. */
    interface Host {
        /** Resolves a destination or configured service object through the existing loader. */
        Object loadOrGetObject(String path);

        /** Reads the current lifecycle method for each hook separately. */
        String lifecycleMethod(MudlibLifecycleEvent event);

        /** Resolves the currently configured perception adapter object only when delivery needs it. */
        Object perceptionBoundaryObject();

        /** Returns the registered object ID used by optional identifier fallback and diagnostics. */
        String objectId(Object object);

        /** Queries the session owner's current interactive attachment without changing identity rules. */
        boolean isInteractive(Object object);

        /** Tests this raw reference against tombstones without canonicalizing a historical receiver. */
        boolean isDestroyedIdentity(Object object);

        /** Retains the host's existing compatibility conversion of message values to text. */
        String messageText(Object value);

        /** Delivers player-bound world text through existing private-output policy before authored hooks. */
        void writePerceptionText(Object observer, Object value);

        /** Binds movement lifecycle execution and restores the previous runtime even on failure. */
        void withRuntimeContext(Runnable action);
    }
}
