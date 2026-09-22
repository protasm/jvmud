package io.github.protasm.jvmud.communication.console;

import io.github.protasm.jvmud.communication.transport.admin.AdminTLS;
import io.github.protasm.jvmud.communication.transport.admin.AdminWire;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLException;
import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.util.*;

/** Shared interactive administration console; the authenticated endpoint determines its command scope. */
public final class AdminConsole {
    private AdminConsole() {}

    /** Reports expected operator and connection errors without a Java stack trace; failures exit with status 1. */
    public static void main(String[] args) {
        try { run(args); }
        catch (IOException | GeneralSecurityException | IllegalArgumentException e) {
            System.err.println("Error: " + errorMessage(e));
            System.exit(1);
        }
    }

    /** Connects locally for bootstrap or through pinned TLS for engine or mudlib administration. */
    private static void run(String[] args) throws IOException, GeneralSecurityException {
        if (args.length == 1 && args[0].equals("--help")) { System.out.println(usage()); return; }
        Map<String, String> options = parseOptions(args);
        if (options.containsKey("--socket")) {
            try (SocketChannel socket = SocketChannel.open(StandardProtocolFamily.UNIX)) {
                try { socket.connect(UnixDomainSocketAddress.of(options.get("--socket"))); }
                catch (IOException e) {
                    throw new IOException("Cannot connect to the local engine at " + options.get("--socket")
                            + ". Check that the engine is running and the socket path is correct.", e);
                }
                converse(new DataInputStream(Channels.newInputStream(socket)), new DataOutputStream(Channels.newOutputStream(socket)), null, null);
            }
        } else {
            requireNoninteractiveCredentials(options);
            System.out.println("Connecting to " + options.get("--host") + ":" + options.get("--port") + " over TLS.");
            String fingerprint = promptOption(options, "--fingerprint", "Administration TLS fingerprint: ");
            // Collect terminal input before connecting: the server bounds authentication to ten seconds.
            String name = promptOption(options, "--user", "Administrator name: ");
            String token;
            if (options.containsKey("--token-file")) token = Files.readString(Path.of(options.get("--token-file"))).trim();
            else {
                char[] secret = System.console().readPassword("Access token: ");
                if (secret == null) return;
                try { token = new String(secret); }
                finally { Arrays.fill(secret, '\0'); }
            }
            try (SSLSocket socket = (SSLSocket) AdminTLS.client(fingerprint).getSocketFactory().createSocket()) {
                socket.connect(new InetSocketAddress(options.get("--host"), Integer.parseInt(options.get("--port"))), 5000);
                socket.setSoTimeout(10000); socket.startHandshake(); socket.setSoTimeout(0);
                converse(new DataInputStream(socket.getInputStream()), new DataOutputStream(socket.getOutputStream()), name, token);
            }
        }
    }

    /** Extracts certificate diagnostics from TLS wrappers and gives common transport failures actionable wording. */
    private static String errorMessage(Exception failure) {
        if (failure instanceof SSLException) {
            String certificateMessage = null;
            for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
                if (cause instanceof CertificateException && cause.getMessage() != null)
                    certificateMessage = cause.getMessage();
            }
            return certificateMessage != null ? certificateMessage
                    : "TLS connection failed. Check the server's administration port and TLS configuration.";
        }
        if (failure instanceof UnknownHostException) return "Cannot resolve the server hostname: " + failure.getMessage();
        if (failure instanceof ConnectException) return "Connection refused. Check that the engine is running at the specified server and port.";
        if (failure instanceof SocketTimeoutException) return "Connection timed out. Check the server address, port, and network access.";
        if (failure instanceof EOFException && failure.getMessage() == null) return "The server closed the administration connection.";
        return failure.getMessage() == null ? "Administration connection failed." : failure.getMessage();
    }

    /** Requires explicit credentials for scripts; interactive prompts never consume command input. */
    private static void requireNoninteractiveCredentials(Map<String, String> options) {
        if (System.console() == null && !options.keySet().containsAll(Set.of("--user", "--fingerprint", "--token-file")))
            throw new IllegalArgumentException("Without an interactive terminal, supply --user, --fingerprint, and --token-file.");
    }

    /** Reads an omitted public connection value from the terminal, rejecting empty or cancelled input. */
    private static String promptOption(Map<String, String> options, String key, String prompt) throws IOException {
        if (options.containsKey(key)) return options.get(key);
        String value = System.console().readLine("%s", prompt);
        if (value == null) throw new EOFException("Administration connection cancelled.");
        if (value.isBlank()) throw new IllegalArgumentException(key + " must not be blank.");
        return value.trim();
    }

    private static void converse(DataInputStream in, DataOutputStream out, String name, String token) throws IOException {
        AdminWire.write(out, AdminWire.VERSION, 128);
        if (name != null) { AdminWire.write(out, name, 256); AdminWire.write(out, token, 256); }
        String scope = AdminWire.read(in, 1024);
        if (scope.startsWith("ERROR:")) throw new IOException(scope.substring("ERROR:".length()).strip());
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

    /**
     * Accepts an optional server and port, defaulting to localhost:4001.
     * The standalone --owner flag selects the standard engine's recovery socket
     * under the current user's home; --socket selects a custom local endpoint.
     * Named options remain available for scripting.
     */
    static Map<String, String> parseOptions(String[] args) {
        if (args.length == 1 && args[0].equals("--owner"))
            return Map.of("--socket", Path.of(System.getProperty("user.home"), ".jvmud", "engine-4000", "engine.sock").toString());
        Map<String, String> result = new HashMap<>();
        int firstOption = 0;
        while (firstOption < args.length && !args[firstOption].startsWith("--")) {
            if (firstOption >= 2 || args[firstOption].isBlank() || args[firstOption].startsWith("-"))
                throw new IllegalArgumentException(usage());
            result.put(firstOption == 0 ? "--host" : "--port", args[firstOption]);
            firstOption++;
        }
        Set<String> accepted = Set.of("--socket", "--host", "--port", "--user", "--token-file", "--fingerprint");
        for (int i = firstOption; i < args.length; i++) {
            String key = args[i];
            if (!accepted.contains(key) || ++i == args.length || args[i].isBlank() || args[i].startsWith("--")
                    || result.putIfAbsent(key, args[i]) != null)
                throw new IllegalArgumentException(usage());
        }
        if (result.containsKey("--socket")) {
            if (result.size() != 1) throw new IllegalArgumentException(usage());
        } else {
            result.putIfAbsent("--host", "localhost");
            result.putIfAbsent("--port", "4001");
            try { int port = Integer.parseInt(result.get("--port")); if (port < 1 || port > 65535) throw new NumberFormatException(); }
            catch (NumberFormatException e) { throw new IllegalArgumentException(usage()); }
            if (result.containsKey("--fingerprint") && !result.get("--fingerprint").replace(":", "").matches("(?i)[0-9a-f]{64}"))
                throw new IllegalArgumentException("Invalid certificate fingerprint.");
        }
        return Map.copyOf(result);
    }

    private static String usage() {
        return "Usage: jvmud-console [<server> [<port>]] [--user <name>] [--fingerprint <SHA-256>] [--token-file <path>]\n"
                + "   or: jvmud-console [--host <host>] [--port <admin-port>] [--user <name>] [--fingerprint <SHA-256>] [--token-file <path>]\n"
                + "   or: jvmud-console --owner\n"
                + "   or: jvmud-console --socket <engine.sock>\n"
                + "--owner: connect to ~/.jvmud/engine-4000/engine.sock as the engine state directory's OS owner; no token or fingerprint needed.\n"
                + "Use --socket for a custom state directory or an engine using a different player port.\n"
                + "Defaults: localhost:4001 over TLS. Prompts for the trusted certificate fingerprint, administrator name, and token.\n"
                + "Obtain the fingerprint from the engine operator through a trusted channel. Tokens are entered without echo.\n"
                + "Without a terminal, supply --user, --fingerprint, and --token-file.";
    }
}
