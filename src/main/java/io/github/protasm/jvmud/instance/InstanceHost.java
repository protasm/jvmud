package io.github.protasm.jvmud.instance;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.BiConsumer;

/** Interactive host behind one telnet listener. */
public interface InstanceHost {
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

    Object dispatch(InstancePersona persona, PrintWriter out, String commandLine);

    void printPromptIfReady(InstancePersona persona, PrintWriter out);

    boolean isCapturingInput(InstancePersona persona);

    boolean isCapturingNoEchoInput(InstancePersona persona);

    boolean isAttached(InstancePersona persona);

    /** Prefix that escapes a line into transport control rather than mudlib command dispatch. */
    default String transportControlPrefix() {
        return "//";
    }
}
