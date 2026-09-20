package io.github.protasm.jvmud.engine;

/** Read-only engine view of a mudlib's lifecycle, endpoints, process and latest failure. */
public record MudlibStatus(String id, String name, State state, int playerPort, int adminPort, long pid, String detail) {
    /** A failed or stopped mudlib remains registered so administrators can inspect and restart it. */
    public enum State { STARTING, RUNNING, STOPPING, STOPPED, FAILED }
}
