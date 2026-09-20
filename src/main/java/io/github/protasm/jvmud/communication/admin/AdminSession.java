package io.github.protasm.jvmud.communication.admin;

import java.io.IOException;

/** One authenticated administration conversation, independent of its connection protocol. */
public interface AdminSession extends AutoCloseable {
    /** Identifies the command scope shown in the console prompt. */
    String scope();
    /** Executes one command and returns its text and whether the conversation remains open. */
    Reply execute(String command) throws IOException;
    /** Releases session state without stopping the engine or mudlib. */
    @Override default void close() throws IOException {}
    /** Bounded protocol response; false ends this administration conversation. */
    record Reply(String text, boolean running) {}
}
