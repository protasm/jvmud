package io.github.protasm.jvmud.engine.mudlib;

/**
 * Native JVMud lifecycle moments that a mudlib may map to LPC methods.
 *
 * <p>Lifecycle events are engine-owned names for moments when JVMud can call into mudlib policy or
 * behavior. A mudlib registers interest through {@link MudlibBoundary}: either by declaring that it
 * handles an event, or by mapping the event to a concrete mudlib method name. The method name is
 * mudlib vocabulary; the event name is the engine contract.</p>
 *
 * <p>No lifecycle event is globally required. If a mapping is absent, JVMud proceeds with the
 * engine-owned operation. If a mapping is present and the target object does not define the mapped
 * method, JVMud skips the call unless a future contract marks that event required.</p>
 *
 * <p>Several events are defined before their full runtime delivery path exists. Those reserved
 * events make boundary configuration explicit without forcing the engine to adopt legacy driver
 * concepts such as applies, rooms, heartbeats, or master objects.</p>
 */
public enum MudlibLifecycleEvent {
    /**
     * A mudlib object has been loaded or cloned and may initialize its own state.
     *
     * <p>Current delivery: implemented. JVMud invokes the mapped method on the loaded or cloned
     * object, passing one LPC argument: {@code mixed first_load}. Current compatibility passes
     * {@code 0} for that argument.</p>
     *
     * <p>Common LP245 compatibility mapping: {@code lifecycle.object_loaded = reset}.</p>
     */
    OBJECT_LOADED,

    /**
     * A previously existing object has been reactivated by reload, reset, or world maintenance.
     *
     * <p>Current delivery: reserved. This event is named so mudlibs can describe the boundary they
     * want, but JVMud does not currently invoke it.</p>
     */
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
     *
     * <p>Current delivery: implemented. JVMud invokes the mapped method on the configured boundary
     * object, passing the normalized requested object path.</p>
     *
     * <p>Common LP245 compatibility mapping: {@code lifecycle.object_source_missing =
     * compile_object}.</p>
     */
    OBJECT_SOURCE_MISSING,

    /**
     * An object is about to be destroyed and the mudlib may clean up its own references.
     *
     * <p>Current delivery: implemented. JVMud invokes the mapped method on the configured boundary
     * object, passing the target object being destroyed. The hook's return value is advisory to the
     * runtime destruction path.</p>
     *
     * <p>Common LP245 compatibility mapping: {@code lifecycle.object_destruction_requested =
     * prepare_destruct}.</p>
     */
    OBJECT_DESTRUCTION_REQUESTED,

    /**
     * An object has been removed from the live runtime.
     *
     * <p>Current delivery: reserved. JVMud currently exposes the pre-destruction hook through
     * {@link #OBJECT_DESTRUCTION_REQUESTED}; this post-destruction event is named for future
     * cleanup policies that need a separate after-the-fact notification.</p>
     */
    OBJECT_DESTROYED,

    /**
     * An entity has completed movement into a Place.
     *
     * <p>Current delivery: reserved. Movement currently happens through engine containment and
     * location APIs; this event is reserved for mudlib policy that needs to observe arrivals without
     * defining Places as legacy rooms.</p>
     */
    ENTITY_ARRIVED_AT_PLACE,

    /**
     * An entity is leaving a Place.
     *
     * <p>Current delivery: reserved. This event is paired with {@link #ENTITY_ARRIVED_AT_PLACE} for
     * future movement policy and notification hooks.</p>
     */
    ENTITY_DEPARTED_FROM_PLACE,

    /**
     * An entity has entered another entity's containment.
     *
     * <p>Current delivery: reserved. This is the entity-container counterpart to place movement
     * events.</p>
     */
    ENTITY_ADDED_TO_ENTITY,

    /**
     * An entity has left another entity's containment.
     *
     * <p>Current delivery: reserved. This is the removal counterpart to
     * {@link #ENTITY_ADDED_TO_ENTITY}.</p>
     */
    ENTITY_REMOVED_FROM_ENTITY,

    /**
     * A Session has connected and JVMud has created a Player endpoint.
     *
     * <p>Current delivery: implemented by the server layer for hosted telnet play. The configured
     * player object method is invoked without LPC arguments and may start login or character
     * selection input.</p>
     *
     * <p>Common LP245 compatibility mapping: {@code lifecycle.player_session_connected = logon}.</p>
     */
    PLAYER_SESSION_CONNECTED,

    /**
     * A Session has finished rebinding from one live mudlib projection to another.
     *
     * <p>Current delivery: implemented by the hosted server path after compatibility session
     * handoff operations such as LPC {@code exec} change the object receiving player input. JVMud
     * invokes the configured method without LPC arguments on the newly bound object.</p>
     */
    PLAYER_SESSION_POST_REBIND,

    /**
     * Mudlib policy or JVMud fallback has resolved the Persona a Player will use.
     *
     * <p>Current delivery: reserved. This is a Player/Session/Persona lifecycle step, not a legacy
     * login-driver apply.</p>
     */
    PLAYER_PERSONA_RESOLVED,

    /**
     * JVMud has associated a live mudlib projection with a Player, Session, and Persona.
     *
     * <p>Current delivery: reserved. Compatibility mudlibs that collapse account, connection, and
     * Persona behavior into one LPC object can eventually map this event to their glue code.</p>
     */
    PLAYER_OBJECT_BOUND,

    /**
     * A Player's Persona has entered the world and can begin ordinary interaction.
     *
     * <p>Current delivery: reserved. The engine already owns the attach sequence; this event is
     * named for mudlib policy that wants an explicit post-entry notification.</p>
     */
    PLAYER_ENTERED_WORLD,

    /**
     * A Session has disconnected and JVMud is unbinding session-only routing.
     *
     * <p>Current delivery: implemented by the server layer for hosted telnet play. The configured
     * player object method is invoked without LPC arguments so the mudlib can save or clean up
     * session-owned state.</p>
     *
     * <p>Common LP245 compatibility mapping: {@code lifecycle.player_session_disconnected =
     * quit}.</p>
     */
    PLAYER_SESSION_DISCONNECTED,

    /**
     * An interactive Persona's local command/perception scope is being refreshed.
     *
     * <p>Current delivery: implemented. JVMud invokes the mapped method with no LPC arguments on
     * the active Persona, its location, objects in that location, and objects carried by the
     * Persona. This is where compatibility mudlibs commonly register local text commands.</p>
     *
     * <p>Common LP245 compatibility mapping: {@code lifecycle.interaction_scope_started = init}.</p>
     */
    INTERACTION_SCOPE_STARTED,

    /**
     * JVMud is about to dispatch a Player command line to mudlib behavior.
     *
     * <p>Current delivery: reserved. The command actor and verb are already tracked by the runtime;
     * this event is for future mudlib policy that needs explicit before-dispatch notification.</p>
     */
    COMMAND_DISPATCH_STARTED,

    /**
     * JVMud has finished dispatching a Player command line.
     *
     * <p>Current delivery: reserved. This event is paired with {@link #COMMAND_DISPATCH_STARTED}
     * for future command auditing or cleanup policy.</p>
     */
    COMMAND_DISPATCH_FINISHED,

    /**
     * Compilation or load-time diagnostics should be reported to mudlib policy.
     *
     * <p>Current delivery: implemented. JVMud invokes the mapped method on the configured boundary
     * object with the object path and diagnostic message, then clears generated output so reporting
     * does not leak into gameplay transcripts.</p>
     *
     * <p>Common LP245 compatibility mapping: {@code lifecycle.log_error = log_error}.</p>
     */
    LOG_ERROR,

    /**
     * A runtime error should be reported to mudlib policy.
     *
     * <p>Current delivery: implemented for timed runtime errors and server-layer error handling.
     * JVMud invokes the mapped method on the configured boundary/error handler object with details
     * about the target, context, operation, and message.</p>
     *
     * <p>Common LP245 compatibility mapping: {@code lifecycle.runtime_error = runtime_error}.</p>
     */
    RUNTIME_ERROR,

    /**
     * A scheduled recurring tick failed while invoking mudlib code.
     *
     * <p>Current delivery: implemented. When a scheduled tick throws, JVMud can invoke this
     * specialized error hook in addition to {@link #RUNTIME_ERROR}.</p>
     *
     * <p>Common LP245 compatibility mapping: {@code lifecycle.scheduled_tick_error =
     * heart_beat_error}.</p>
     */
    SCHEDULED_TICK_ERROR,

    /**
     * The host has completed mudlib boot and configured preloading.
     *
     * <p>Current delivery: implemented by the instance boot coordinator. JVMud invokes the mapped
     * no-argument method on the configured boundary object. Mudlibs may use this neutral hook to
     * perform flavor-specific driver initialization without teaching the host about driver master
     * objects or applies.</p>
     */
    SERVER_STARTED,

    /**
     * The host server is shutting down a mudlib.
     *
     * <p>Current delivery: implemented by the server layer. JVMud invokes the mapped method on the
     * configured boundary/error handler object, passing the shutdown reason.</p>
     *
     * <p>Common LP245 compatibility mapping: {@code lifecycle.server_shutdown = notify_shutdown}.</p>
     */
    SERVER_SHUTDOWN,

    /**
     * The engine scheduler is delivering deterministic recurring time to an object.
     *
     * <p>Current delivery: implemented through the temporal tick configuration rather than through
     * a per-event method mapping. The mudlib boundary chooses the recurring tick method name and
     * default interval; compatibility shims may expose legacy names such as {@code set_heart_beat}
     * on top of that scheduler.</p>
     */
    SCHEDULED_TICK,

    /**
     * A previously requested one-shot deferred callback is due.
     *
     * <p>Current delivery: reserved as a named lifecycle event, while concrete delayed callbacks are
     * currently scheduled with explicit callback method names through runtime efuns.</p>
     */
    DEFERRED_CALLBACK
}
