package io.github.protasm.jvmud.cli;

import io.github.protasm.jvmud.transport.admin.AdminWire;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Terminal client for an existing server's authenticated local administration endpoint. */
public final class AdminClient {
    private AdminClient() {}

    /** Connects to the specified admin port; never creates or owns a world runtime. */
    public static void main(String[] args) throws IOException {
        if (args.length == 1 && "--help".equals(args[0])) {
            System.out.println(usage());
            return;
        }
        Options options = parseOptions(args);
        String token = Files.readString(options.tokenFile()).trim();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", options.port()), 5000);
            socket.setSoTimeout(10000);
            var in = new DataInputStream(socket.getInputStream());
            var out = new DataOutputStream(socket.getOutputStream());
            AdminWire.write(out, AdminWire.VERSION, 128);
            AdminWire.write(out, token, 256);
            String greeting = AdminWire.read(in, AdminWire.MAX_RESPONSE_BYTES);
            if (greeting.startsWith("ERROR:")) throw new IOException(greeting);
            System.out.println(greeting);
            socket.setSoTimeout(0);
            var terminal = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            while (true) {
                System.out.print("jvmud> ");
                System.out.flush();
                String line = terminal.readLine();
                if (line == null) return;
                AdminWire.write(out, line, AdminWire.MAX_COMMAND_BYTES);
                System.out.print(AdminWire.read(in, AdminWire.MAX_RESPONSE_BYTES));
                System.out.flush();
                if (!in.readBoolean()) return;
            }
        }
    }

    /** Validates an explicit admin TCP port and optional credential path. */
    static Options parseOptions(String[] args) {
        int port = 0;
        Path tokenFile = null;
        for (int i = 0; i < args.length; i++) {
            String option = args[i];
            if ((!"--port".equals(option) && !"--token-file".equals(option)) || ++i >= args.length) {
                throw new IllegalArgumentException(usage());
            }
            if ("--port".equals(option)) {
                try { port = Integer.parseInt(args[i]); }
                catch (NumberFormatException e) { throw new IllegalArgumentException(usage()); }
                if (port < 1 || port > 65535) throw new IllegalArgumentException(usage());
            } else {
                if (args[i].isBlank()) throw new IllegalArgumentException(usage());
                tokenFile = Path.of(args[i]);
            }
        }
        if (port == 0) throw new IllegalArgumentException(usage());
        return new Options(port, tokenFile == null ? AdminWire.tokenFile(port) : tokenFile);
    }

    static String usage() {
        return "Usage: scripts/jvmud-cli --port <admin-port> [--token-file <path>]";
    }

    record Options(int port, Path tokenFile) {}
}
