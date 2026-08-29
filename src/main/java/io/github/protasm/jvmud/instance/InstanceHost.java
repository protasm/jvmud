package io.github.protasm.jvmud.instance;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.Duration;

/** Interactive host behind one telnet listener. */
public interface InstanceHost {
    Path mudlibRoot();

    MudlibBootResult bootResult();

    Duration worldTickInterval();

    void advanceWorldTick();

    void shutdown(Object reason);

    InstancePersona attachPersona(PrintWriter out, String remoteAddress);

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
