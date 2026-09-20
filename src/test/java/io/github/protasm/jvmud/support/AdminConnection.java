package io.github.protasm.jvmud.support;

import io.github.protasm.jvmud.transport.admin.*;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.file.Path;

/** Actual protocol client used by transport and process-boundary integration tests. */
public final class AdminConnection implements AutoCloseable {
    private final Closeable connection;
    public final DataInputStream input;
    private final DataOutputStream output;
    public final String greeting;
    public boolean running = true;

    public AdminConnection(int port, String fingerprint, String user, String token) throws Exception {
        SSLSocket socket = (SSLSocket) AdminTLS.client(fingerprint).getSocketFactory().createSocket("127.0.0.1", port);
        socket.setSoTimeout(5000); connection = socket;
        try {
            socket.startHandshake(); input = new DataInputStream(socket.getInputStream()); output = new DataOutputStream(socket.getOutputStream());
            AdminWire.write(output, AdminWire.VERSION, 128); AdminWire.write(output, user, 256); AdminWire.write(output, token, 256);
            greeting = AdminWire.read(input, 1024);
        } catch (Exception e) { socket.close(); throw e; }
    }
    public AdminConnection(Path path) throws IOException {
        SocketChannel socket = SocketChannel.open(StandardProtocolFamily.UNIX); connection = socket;
        try {
            socket.connect(UnixDomainSocketAddress.of(path));
            input = new DataInputStream(Channels.newInputStream(socket)); output = new DataOutputStream(Channels.newOutputStream(socket));
            AdminWire.write(output, AdminWire.VERSION, 128); greeting = AdminWire.read(input, 1024);
        } catch (IOException e) { socket.close(); throw e; }
    }
    public String command(String command) throws IOException {
        AdminWire.write(output, command, AdminWire.MAX_COMMAND_BYTES);
        String text = AdminWire.read(input, AdminWire.MAX_RESPONSE_BYTES); running = input.readBoolean(); return text;
    }
    public void close() throws IOException { connection.close(); }
}
