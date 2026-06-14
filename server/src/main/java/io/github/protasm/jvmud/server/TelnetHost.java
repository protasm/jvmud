package io.github.protasm.jvmud.server;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.Duration;

/** Interactive host behind one telnet listener. */
interface TelnetHost {
    Path mudlibRoot();

    MudlibBootResult bootResult();

    Duration worldTickInterval();

    void advanceWorldTick();

    TelnetPersona attachPersona(PrintWriter out, String remoteAddress);

    void detachPersona(TelnetPersona persona);

    Object dispatch(TelnetPersona persona, PrintWriter out, String commandLine);

    void printPromptIfReady(TelnetPersona persona, PrintWriter out);

    boolean isCapturingInput(TelnetPersona persona);

    boolean isCapturingNoEchoInput(TelnetPersona persona);

    boolean isAttached(TelnetPersona persona);
}
