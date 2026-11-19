package io.github.protasm.jvmud.engine.instance;

import io.github.protasm.jvmud.engine.instance.session.InstanceSession;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.BiConsumer;

/** Interactive operations on one hosted mudlib, independent of its player transport. */
public interface InstanceHost {
    /**
     * Executes trusted administrative work on the selected mudlib's execution queue,
     * serialized with player dispatch and ticks. The callback must not use
     * the runtime outside an administer call, and must not wait for network input or write to a socket.
     */
    default <T> T administer(java.util.function.Function<io.github.protasm.jvmud.engine.instance.lpc.MudlibRuntime, T> action) {
        throw new UnsupportedOperationException("Administration is unavailable for this host.");
    }

    Path mudlibRoot();

    MudlibBootResult bootResult();

    Duration worldTickInterval();

    void advanceWorldTick();

    void shutdown(Object reason);

    /** Opens a connection, which may enter login before attaching a Persona. */
    InstanceSession attachSession(PrintWriter out, String remoteAddress);

    /** Binds the transport callback used for negotiated out-of-band protocol messages. */
    default void bindClientProtocolSink(
            InstanceSession session, BiConsumer<String, String> protocolOutputSink) {}

    /** Notifies the hosted runtime that a client protocol has been enabled or disabled. */
    default void setClientProtocolEnabled(InstanceSession session, String protocol, boolean enabled) {}

    /** Delivers one decoded-text message received over a negotiated client protocol. */
    default void receiveClientProtocolMessage(InstanceSession session, String protocol, String message) {}

    /** Closes the connection and its current Persona attachment. */
    void detachSession(InstanceSession session);

    /** Delivers input; player-facing command meaning and responses belong to the mudlib. */
    void dispatch(InstanceSession session, PrintWriter out, String commandLine);

    void printPromptIfReady(InstanceSession session, PrintWriter out);

    /** Returns whether managed login or an authored callback is waiting for the next input line. */
    boolean isCapturingInput(InstanceSession session);

    /** Returns whether the current login prompt or authored input callback requests hidden input. */
    boolean isCapturingNoEchoInput(InstanceSession session);

    /** Returns connection liveness; a managed login can be open without a Persona attachment. */
    boolean isOpen(InstanceSession session);

    /** Prefix that escapes a line into transport control rather than mudlib command dispatch. */
    default String transportControlPrefix() {
        return "//";
    }
}
