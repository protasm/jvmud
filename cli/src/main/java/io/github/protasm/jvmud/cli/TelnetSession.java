package io.github.protasm.jvmud.cli;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/** One line-oriented telnet connection backed by an AdminCli session. */
final class TelnetSession implements Runnable {
    private static final int IAC = 255;
    private static final int WILL = 251;
    private static final int WONT = 252;
    private static final int DO = 253;
    private static final int DONT = 254;

    private final Socket socket;
    private final Path mudlibRoot;
    private final String configObjectPath;

    TelnetSession(Socket socket, Path mudlibRoot, String configObjectPath) {
        this.socket = socket;
        this.mudlibRoot = mudlibRoot;
        this.configObjectPath = configObjectPath;
    }

    @Override
    public void run() {
        try (socket;
                BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
                OutputStream rawOut = socket.getOutputStream();
                PrintWriter out = new PrintWriter(new OutputStreamWriter(rawOut, StandardCharsets.UTF_8), true)) {
            AdminCli cli = new AdminCli(out);
            cli.execute("/boot " + mudlibRoot + " " + configObjectPath);
            out.println("JVMud telnet. Type /help for slash commands or /quit to disconnect.");
            prompt(out);

            StringBuilder line = new StringBuilder();
            int value;
            while (cli.isRunning() && (value = in.read()) != -1) {
                if (handleTelnetCommand(value, in, rawOut, out)) {
                    continue;
                }
                if (value == '\r') {
                    continue;
                }
                if (value == '\n') {
                    executeLine(cli, out, line);
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
        }
    }

    private void executeLine(AdminCli cli, PrintWriter out, StringBuilder line) {
        String commandLine = line.toString();
        line.setLength(0);
        cli.execute(commandLine);
        if (cli.isRunning()) {
            prompt(out);
        }
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
}
