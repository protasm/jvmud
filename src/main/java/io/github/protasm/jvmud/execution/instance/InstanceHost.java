package io.github.protasm.jvmud.execution.instance;

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
    default <T> T administer(java.util.function.Function<io.github.protasm.jvmud.language.exec.LPCRuntime, T> action) {
        throw new UnsupportedOperationException("Administration is unavailable for this host.");
    }

    Path mudlibRoot();

    MudlibBootResult bootResult();

    Duration worldTickInterval();

    void advanceWorldTick();

    void shutdown(Object reason);

    InstancePersona attachPersona(PrintWriter out, String remoteAddress);

    /** Binds the transport callback used for negotiated out-of-band protocol messages. */
    default void bindClientProtocolSink(
            InstancePersona persona, BiConsumer<String, String> protocolOutputSink) {}

    /** Notifies the hosted runtime that a client protocol has been enabled or disabled. */
    default void setClientProtocolEnabled(InstancePersona persona, String protocol, boolean enabled) {}

    /** Delivers one decoded-text message received over a negotiated client protocol. */
    default void receiveClientProtocolMessage(InstancePersona persona, String protocol, String message) {}

    void detachPersona(InstancePersona persona);

    /** Delivers input; player-facing command meaning and responses belong to the mudlib. */
    void dispatch(InstancePersona persona, PrintWriter out, String commandLine);

    void printPromptIfReady(InstancePersona persona, PrintWriter out);

    boolean isCapturingInput(InstancePersona persona);

    boolean isCapturingNoEchoInput(InstancePersona persona);

    boolean isAttached(InstancePersona persona);

    /** Prefix that escapes a line into transport control rather than mudlib command dispatch. */
    default String transportControlPrefix() {
        return "//";
    }
}
