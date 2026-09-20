package io.github.protasm.jvmud.engine.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.protasm.jvmud.transport.admin.AdminWire;
import java.io.*;

/** Bounded JSON messages carried only over inherited parent/worker pipes, never a public admin socket. */
public final class WorkerWire {
    private static final ObjectMapper JSON = new ObjectMapper();
    private WorkerWire() {}
    /** Sends one framed message, preserving pipes when game code writes to System.out. */
    public static void write(DataOutputStream out, Object message) throws IOException {
        AdminWire.write(out, JSON.writeValueAsString(message), AdminWire.MAX_RESPONSE_BYTES);
    }
    /** Reads one complete message with an allocation limit. */
    public static <T> T read(DataInputStream in, Class<T> type) throws IOException {
        return JSON.readValue(AdminWire.read(in, AdminWire.MAX_RESPONSE_BYTES), type);
    }
    /** Worker boot request; no engine credentials are inherited. */
    public record Boot(String root, String config, boolean trace) {}
    /** Readiness metadata returned only after boot and private transport startup succeed. */
    public record Ready(String id, String name, int playerPort, String secret, String summary) {}
    /** A command session request; OPEN and CLOSE isolate each administrator's state. */
    public record Request(String operation, String session, String command) {}
    /** Command result from a worker's serialized execution queue. */
    public record Response(String text, boolean running) {}
}
