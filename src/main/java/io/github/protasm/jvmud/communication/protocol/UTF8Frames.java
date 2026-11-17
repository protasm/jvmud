package io.github.protasm.jvmud.communication.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Length-prefixed UTF-8 messages for JVMud's private and administration protocols.
 *
 * <p>Each protocol selects its own byte limit. Framing carries no authentication, command, or
 * worker lifecycle policy; those meanings belong to the protocol using the frame.</p>
 */
public final class UTF8Frames {
    private UTF8Frames() {}

    /** Reads a four-byte length followed by exactly that many bytes, within the caller's limit. */
    public static String read(DataInputStream input, int limit) throws IOException {
        int length = input.readInt();
        if (length < 0 || length > limit) {
            throw new IOException("Invalid UTF-8 frame length.");
        }
        byte[] bytes = new byte[length];
        input.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /** Writes one complete frame and flushes it so request/response protocols cannot deadlock. */
    public static void write(DataOutputStream output, String text, int limit) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > limit) {
            throw new IOException("UTF-8 message exceeds the frame limit.");
        }
        output.writeInt(bytes.length);
        output.write(bytes);
        output.flush();
    }
}
