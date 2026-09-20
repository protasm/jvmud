package io.github.protasm.jvmud.transport.admin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Versioned UTF-8 framing shared by the administration consoles, listeners, and private worker pipes. */
public final class AdminWire {
    public static final String VERSION = "JVMUD-ADMIN-2";
    public static final int MAX_COMMAND_BYTES = 65536;
    public static final int MAX_RESPONSE_BYTES = 16 * 1024 * 1024;

    private AdminWire() {}

    /** Reads one bounded frame; rejects negative, oversized, or truncated payloads. */
    public static String read(DataInputStream in, int limit) throws IOException {
        int length = in.readInt();
        if (length < 0 || length > limit) {
            throw new IOException("Invalid administration frame length.");
        }
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /** Writes one bounded frame and flushes it without waiting for another request. */
    public static void write(DataOutputStream out, String text, int limit) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > limit) {
            throw new IOException("Administration response exceeds the frame limit.");
        }
        out.writeInt(bytes.length);
        out.write(bytes);
        out.flush();
    }
}
