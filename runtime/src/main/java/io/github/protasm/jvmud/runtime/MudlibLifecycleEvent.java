package io.github.protasm.jvmud.runtime;

/** Native JVMud lifecycle moments that a mudlib compatibility boundary may handle. */
public enum MudlibLifecycleEvent {
    OBJECT_INITIALIZED,
    OBJECT_REACTIVATED,
    INTERACTION_SCOPE_ENTERED,
    SCHEDULED_TICK,
    DEFERRED_CALLBACK,
    IDLE_OBJECT_REVIEW,
    SESSION_CONNECTED,
    POLICY_CHECK,
    ERROR_REPORTED
}
