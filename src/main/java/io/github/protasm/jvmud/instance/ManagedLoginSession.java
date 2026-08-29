package io.github.protasm.jvmud.instance;

import java.io.PrintWriter;

/** Host-managed login flow selected explicitly by a mudlib manifest session policy. */
interface ManagedLoginSession {
    /** Emits the first prompt for this login flow. */
    void start();

    /** Returns whether transport echo should be disabled for the next input line. */
    boolean noEcho();

    /** Consumes one input line and returns the resulting session action. */
    ManagedLoginResult handle(String line, PrintWriter out);
}
