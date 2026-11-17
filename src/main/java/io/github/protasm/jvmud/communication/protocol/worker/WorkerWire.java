package io.github.protasm.jvmud.communication.protocol.worker;

import io.github.protasm.jvmud.communication.protocol.UTF8Frames;

import java.io.*;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Bounded JSON messages carried only over inherited parent/worker pipes, never a public admin socket. */
public final class WorkerWire {
    private static final int MAX_MESSAGE_BYTES = 16 * 1024 * 1024;
    private static final ObjectMapper JSON = new ObjectMapper();
    private WorkerWire() {}
    /** Sends one framed message, preserving pipes when game code writes to System.out. */
    public static void write(DataOutputStream out, Object message) throws IOException {
        UTF8Frames.write(out, JSON.writeValueAsString(message), MAX_MESSAGE_BYTES);
    }
    /** Reads one complete message with an allocation limit. */
    public static <T> T read(DataInputStream in, Class<T> type) throws IOException {
        return JSON.readValue(UTF8Frames.read(in, MAX_MESSAGE_BYTES), type);
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
