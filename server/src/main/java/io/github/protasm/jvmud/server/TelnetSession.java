package io.github.protasm.jvmud.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/** One line-oriented telnet connection backed by an interactive JVMud session. */
final class TelnetSession implements Runnable {
    private static final int IAC = 255;
    private static final int WILL = 251;
    private static final int WONT = 252;
    private static final int DO = 253;
    private static final int DONT = 254;

    private final Socket socket;
    private final TelnetMud mud;

    TelnetSession(Socket socket, TelnetMud mud) {
        this.socket = socket;
        this.mud = Objects.requireNonNull(mud, "mud");
    }

    @Override
    public void run() {
        SessionState session = null;
        try (socket;
                BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
                OutputStream rawOut = socket.getOutputStream();
                PrintWriter out = new PrintWriter(new OutputStreamWriter(rawOut, StandardCharsets.UTF_8), true)) {
            out.println("JVMud telnet. Type /help for commands or /quit to disconnect.");
            session = new SessionState(mud.attachPersona(out, socket.getInetAddress().getHostAddress()));
            prompt(out);

            StringBuilder line = new StringBuilder();
            int value;
            while (session.running && (value = in.read()) != -1) {
                if (handleTelnetCommand(value, in, rawOut, out)) {
                    continue;
                }
                if (value == '\r') {
                    continue;
                }
                if (value == '\n') {
                    executeLine(session, out, line);
                    continue;
                }
                if (value == 8 || value == 127) {
                    if (!line.isEmpty()) {
                        line.setLength(line.length() - 1);
                    }
                    continue;
                }
                if (value >= 32 || value == '\t') {
                    line.append((char) value);
                }
            }
        } catch (IOException ignored) {
            // A telnet client dropping the socket is a normal session ending.
        } finally {
            if (session != null) {
                session.detach(mud);
            }
        }
    }

    private void executeLine(SessionState session, PrintWriter out, StringBuilder line) {
        String commandLine = line.toString().trim();
        line.setLength(0);
        if (commandLine.isBlank()) {
            prompt(out);
            return;
        }

        if (commandLine.startsWith("/")) {
            executeSlashCommand(session, out, commandLine.substring(1).trim());
        } else {
            executePlayerCommand(session, out, commandLine);
        }
        if (session.running) {
            prompt(out);
        }
    }

    private void executeSlashCommand(SessionState session, PrintWriter out, String commandLine) {
        switch (commandLine) {
        case "help", "h" -> {
            out.println("Telnet commands:");
            out.println("  /help  Show this command reference.");
            out.println("  /quit  Disconnect this session.");
        }
        case "quit", "exit", "q" -> {
            session.detach(mud);
            session.running = false;
        }
        default -> out.println("Unknown telnet command: /" + commandLine);
        }
    }

    private void executePlayerCommand(SessionState session, PrintWriter out, String commandLine) {
        try {
            Object result = mud.dispatch(session.persona, out, normalizePlayerCommand(commandLine));
            if (Integer.valueOf(0).equals(result)) {
                out.println("You can't do that.");
            }
        } catch (RuntimeException e) {
            out.println("Error: " + e.getMessage());
        }
    }

    private String normalizePlayerCommand(String commandLine) {
        String trimmed = commandLine.trim();
        if (trimmed.startsWith("go ")) {
            trimmed = trimmed.substring(3).trim();
        }
        return switch (trimmed) {
        case "n" -> "north";
        case "s" -> "south";
        case "e" -> "east";
        case "w" -> "west";
        case "u" -> "up";
        case "d" -> "down";
        case "l" -> "look";
        default -> trimmed;
        };
    }

    private boolean handleTelnetCommand(
            int value, BufferedInputStream in, OutputStream rawOut, PrintWriter out) throws IOException {
        if (value != IAC) {
            return false;
        }

        int command = in.read();
        if (command == -1) {
            return true;
        }
        if (command == IAC) {
            return false;
        }
        if (command == DO || command == DONT || command == WILL || command == WONT) {
            int option = in.read();
            if (option != -1) {
                refuseOption(command, option, rawOut, out);
            }
        }
        return true;
    }

    private void refuseOption(int command, int option, OutputStream rawOut, PrintWriter out) throws IOException {
        int response = (command == DO || command == DONT) ? WONT : DONT;
        out.flush();
        rawOut.write(IAC);
        rawOut.write(response);
        rawOut.write(option);
        rawOut.flush();
    }

    private void prompt(PrintWriter out) {
        out.print("> ");
        out.flush();
    }

    private static final class SessionState {
        private final TelnetMud.Persona persona;
        private boolean running = true;
        private boolean detached;

        private SessionState(TelnetMud.Persona persona) {
            this.persona = persona;
        }

        private void detach(TelnetMud mud) {
            if (detached) {
                return;
            }
            mud.detachPersona(persona);
            detached = true;
        }
    }
}
