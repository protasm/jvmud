package io.github.protasm.jvmud.transport.telnet;

import io.github.protasm.jvmud.instance.InstanceHost;
import io.github.protasm.jvmud.instance.InstancePersona;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
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
    private static final int ECHO = 1;

    private final Socket socket;
    private final InstanceHost mud;

    TelnetSession(Socket socket, InstanceHost mud) {
        this.socket = socket;
        this.mud = Objects.requireNonNull(mud, "mud");
    }

    @Override
    public void run() {
        SessionState session = null;
        try (socket;
                BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
                OutputStream rawOut = socket.getOutputStream();
                PrintWriter out = new PrintWriter(
                        new TelnetLineEndingWriter(new OutputStreamWriter(rawOut, StandardCharsets.UTF_8)), true)) {
            try {
                session = new SessionState(mud.attachPersona(out, socket.getInetAddress().getHostAddress()));
            } catch (RuntimeException e) {
                out.println("Could not attach player: " + e.getMessage());
                return;
            }
            mud.printPromptIfReady(session.persona, out);
            updateEchoMode(session, rawOut, out);

            StringBuilder line = new StringBuilder();
            int value;
            while (session.running && (value = in.read()) != -1) {
                if (handleTelnetCommand(session, value, in, rawOut, out)) {
                    continue;
                }
                if (value == '\r') {
                    continue;
                }
                if (value == '\n') {
                    executeLine(session, rawOut, out, line);
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

    private void executeLine(SessionState session, OutputStream rawOut, PrintWriter out, StringBuilder line)
            throws IOException {
        String commandLine = line.toString().trim();
        line.setLength(0);
        if (commandLine.isBlank() && !mud.isCapturingInput(session.persona)) {
            mud.printPromptIfReady(session.persona, out);
            updateEchoMode(session, rawOut, out);
            return;
        }

        // A Telnet client does not echo the Return key while server-side echo suppression is
        // active. Supply the terminal line ending before the mudlib writes its next hidden-input
        // prompt so that it begins in column one on every client.
        if (mud.isCapturingNoEchoInput(session.persona)) {
            out.print("\r\n");
            out.flush();
        }

        String controlPrefix = mud.transportControlPrefix();
        if (!controlPrefix.isEmpty() && commandLine.startsWith(controlPrefix)) {
            executeTransportCommand(session, out, commandLine.substring(controlPrefix.length()).trim());
        } else {
            executePlayerCommand(session, out, commandLine);
        }
        out.flush();
        if (!mud.isAttached(session.persona)) {
            session.running = false;
        }
        if (session.running) {
            mud.printPromptIfReady(session.persona, out);
        }
        updateEchoMode(session, rawOut, out);
    }

    private void executeTransportCommand(SessionState session, PrintWriter out, String commandLine) {
        switch (commandLine) {
        case "help", "h" -> {
            out.println("Telnet commands:");
            out.println("  " + mud.transportControlPrefix() + "help  Show this command reference.");
            out.println("  " + mud.transportControlPrefix() + "quit  Disconnect this session.");
        }
        case "quit", "exit", "q" -> {
            session.detach(mud);
            session.running = false;
        }
        default -> out.println("Unknown telnet command: " + mud.transportControlPrefix() + commandLine);
        }
    }

    private void executePlayerCommand(SessionState session, PrintWriter out, String commandLine) {
        try {
            Object result = mud.dispatch(session.persona, out, commandLine);
            if (Integer.valueOf(0).equals(result)) {
                out.println("You can't do that.");
            }
        } catch (RuntimeException e) {
            out.println("Something goes wrong.");
            System.err.println("Unhandled telnet command error: " + e.getMessage());
        }
    }

    private boolean handleTelnetCommand(
            SessionState session, int value, BufferedInputStream in, OutputStream rawOut, PrintWriter out)
            throws IOException {
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
                // DO ECHO acknowledges the WILL ECHO sent while JVMud is consuming hidden input.
                // Rejecting that acknowledgement with WONT immediately re-enables client echo.
                if (command == DO && option == ECHO && session.noEchoNegotiated) {
                    return true;
                }
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

    private void updateEchoMode(SessionState session, OutputStream rawOut, PrintWriter out) throws IOException {
        if (session == null || session.detached || !session.running || !mud.isAttached(session.persona)) {
            if (session != null && session.noEchoNegotiated) {
                writeTelnetCommand(rawOut, out, WONT, ECHO);
                session.noEchoNegotiated = false;
            }
            return;
        }

        boolean shouldSuppressEcho = mud.isCapturingNoEchoInput(session.persona);
        if (shouldSuppressEcho == session.noEchoNegotiated) {
            return;
        }
        writeTelnetCommand(rawOut, out, shouldSuppressEcho ? WILL : WONT, ECHO);
        session.noEchoNegotiated = shouldSuppressEcho;
    }

    private void writeTelnetCommand(OutputStream rawOut, PrintWriter out, int command, int option) throws IOException {
        out.flush();
        rawOut.write(IAC);
        rawOut.write(command);
        rawOut.write(option);
        rawOut.flush();
    }

    private static final class SessionState {
        private final InstancePersona persona;
        private boolean running = true;
        private boolean detached;
        private boolean noEchoNegotiated;

        private SessionState(InstancePersona persona) {
            this.persona = persona;
        }

        private void detach(InstanceHost mud) {
            if (detached) {
                return;
            }
            mud.detachPersona(persona);
            detached = true;
        }
    }

    private static final class TelnetLineEndingWriter extends Writer {
        private final Writer delegate;
        private boolean previousWasCarriageReturn;

        private TelnetLineEndingWriter(Writer delegate) {
            this.delegate = Objects.requireNonNull(delegate, "delegate");
        }

        @Override
        public void write(char[] characters, int offset, int length) throws IOException {
            for (int index = offset; index < offset + length; index++) {
                char character = characters[index];
                if (character == '\n' && !previousWasCarriageReturn) {
                    delegate.write('\r');
                }
                delegate.write(character);
                previousWasCarriageReturn = character == '\r';
            }
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }
}
