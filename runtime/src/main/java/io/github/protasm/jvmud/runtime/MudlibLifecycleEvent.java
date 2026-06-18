package io.github.protasm.jvmud.runtime;

/** Native JVMud lifecycle moments that a mudlib may map to its own methods. */
public enum MudlibLifecycleEvent {
    OBJECT_LOADED,
    OBJECT_ACTIVATED,
    /**
     * A shared object path was requested, no object was already registered for that path,
     * and no source file could be found for it.
     *
     * <p>Mudlibs can map this event to a method that receives the normalized requested
     * object path and may return an object to satisfy that path. JVMud then binds the
     * returned object to the requested path so later shared-object loads return the same
     * object without asking the mudlib again. Returning LPC false lets normal source-missing
     * failure handling continue.</p>
     */
    OBJECT_SOURCE_MISSING,
    OBJECT_DESTRUCTION_REQUESTED,
    OBJECT_DESTROYED,
    ENTITY_ARRIVED_AT_PLACE,
    ENTITY_DEPARTED_FROM_PLACE,
    ENTITY_ADDED_TO_ENTITY,
    ENTITY_REMOVED_FROM_ENTITY,
    PLAYER_SESSION_CONNECTED,
    PLAYER_PERSONA_RESOLVED,
    PLAYER_OBJECT_BOUND,
    PLAYER_ENTERED_WORLD,
    PLAYER_SESSION_DISCONNECTED,
    INTERACTION_SCOPE_STARTED,
    COMMAND_DISPATCH_STARTED,
    COMMAND_DISPATCH_FINISHED,
    LOG_ERROR,
    RUNTIME_ERROR,
    SCHEDULED_TICK_ERROR,
    SERVER_SHUTDOWN,
    SCHEDULED_TICK,
    DEFERRED_CALLBACK
}
