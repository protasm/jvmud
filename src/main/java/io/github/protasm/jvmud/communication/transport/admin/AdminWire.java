package io.github.protasm.jvmud.communication.transport.admin;

import io.github.protasm.jvmud.communication.protocol.UTF8Frames;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Administration protocol version and message limits, carried in shared UTF-8 frames. */
public final class AdminWire {
    public static final String VERSION = "JVMUD-ADMIN-2";
    public static final int MAX_COMMAND_BYTES = 65536;
    public static final int MAX_RESPONSE_BYTES = 16 * 1024 * 1024;

    private AdminWire() {}

    /** Reads one bounded frame; rejects negative, oversized, or truncated payloads. */
    public static String read(DataInputStream in, int limit) throws IOException {
        return UTF8Frames.read(in, limit);
    }

    /** Writes one bounded frame and flushes it without waiting for another request. */
    public static void write(DataOutputStream out, String text, int limit) throws IOException {
        UTF8Frames.write(out, text, limit);
    }
}
