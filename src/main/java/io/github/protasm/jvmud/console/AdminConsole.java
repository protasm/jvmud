package io.github.protasm.jvmud.console;

import io.github.protasm.jvmud.transport.admin.AdminTLS;
import io.github.protasm.jvmud.transport.admin.AdminWire;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Shared interactive administration console; the authenticated endpoint determines its command scope. */
public final class AdminConsole {
    private AdminConsole() {}

    /** Connects locally for bootstrap or through pinned TLS for engine or mudlib administration. */
    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equals("--help")) { System.out.println(usage()); return; }
        Map<String, String> options = parseOptions(args);
        if (options.containsKey("--socket")) {
            try (SocketChannel socket = SocketChannel.open(StandardProtocolFamily.UNIX)) {
                socket.connect(UnixDomainSocketAddress.of(options.get("--socket")));
                converse(new DataInputStream(Channels.newInputStream(socket)), new DataOutputStream(Channels.newOutputStream(socket)), null, null);
            }
        } else {
            String token;
            if (options.containsKey("--token-file")) token = Files.readString(Path.of(options.get("--token-file"))).trim();
            else {
                if (System.console() == null) throw new IllegalArgumentException("Use --token-file without an interactive terminal.");
                char[] secret = System.console().readPassword("Access token: ");
                if (secret == null) return;
                token = new String(secret); Arrays.fill(secret, '\0');
            }
            try (SSLSocket socket = (SSLSocket) AdminTLS.client(options.get("--fingerprint")).getSocketFactory().createSocket()) {
                socket.connect(new InetSocketAddress(options.getOrDefault("--host", "localhost"), Integer.parseInt(options.get("--port"))), 5000);
                socket.setSoTimeout(10000); socket.startHandshake(); socket.setSoTimeout(0);
                converse(new DataInputStream(socket.getInputStream()), new DataOutputStream(socket.getOutputStream()), options.get("--user"), token);
            }
        }
    }

    private static void converse(DataInputStream in, DataOutputStream out, String name, String token) throws IOException {
        AdminWire.write(out, AdminWire.VERSION, 128);
        if (name != null) { AdminWire.write(out, name, 256); AdminWire.write(out, token, 256); }
        String scope = AdminWire.read(in, 1024);
        if (scope.startsWith("ERROR:")) throw new IOException(scope);
        System.out.println("Connected to " + scope + ". Type help for commands; quit disconnects.");
        BufferedReader terminal = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        while (true) {
            System.out.print(scope + "> "); System.out.flush();
            String line = terminal.readLine(); if (line == null) return;
            AdminWire.write(out, line, AdminWire.MAX_COMMAND_BYTES);
            System.out.print(AdminWire.read(in, AdminWire.MAX_RESPONSE_BYTES));
            if (!in.readBoolean()) return;
        }
    }

    /** Validates one explicit endpoint and requires an out-of-band trust pin for remote connections. */
    static Map<String, String> parseOptions(String[] args) {
        Map<String, String> result = new HashMap<>();
        Set<String> accepted = Set.of("--socket", "--host", "--port", "--user", "--token-file", "--fingerprint");
        for (int i = 0; i < args.length; i++) {
            String key = args[i];
            if (!accepted.contains(key) || ++i == args.length || args[i].isBlank() || result.putIfAbsent(key, args[i]) != null)
                throw new IllegalArgumentException(usage());
        }
        if (result.containsKey("--socket")) {
            if (result.size() != 1) throw new IllegalArgumentException(usage());
        } else {
            if (!result.keySet().containsAll(Set.of("--port", "--user", "--fingerprint"))) throw new IllegalArgumentException(usage());
            try { int port = Integer.parseInt(result.get("--port")); if (port < 1 || port > 65535) throw new NumberFormatException(); }
            catch (NumberFormatException e) { throw new IllegalArgumentException(usage()); }
            if (!result.get("--fingerprint").replace(":", "").matches("(?i)[0-9a-f]{64}")) throw new IllegalArgumentException("Invalid certificate fingerprint.");
        }
        return Map.copyOf(result);
    }

    private static String usage() {
        return "Usage: jvmud-console --socket <engine.sock>\n"
                + "   or: jvmud-console [--host <host>] --port <admin-port> --user <name> --fingerprint <SHA-256> [--token-file <path>]";
    }
}
