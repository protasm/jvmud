package io.github.protasm.jvmud.communication.admin;

import io.github.protasm.jvmud.execution.application.JVMud;
import io.github.protasm.jvmud.execution.instance.MudlibSpec;
import io.github.protasm.jvmud.storage.admin.AdminRegistry;
import java.io.IOException;
import java.nio.file.Path;

/** Engine command language. No mudlib runtime objects or player permissions cross this boundary. */
public final class EngineAdminCommandSession implements AdminSession {
    private final JVMud engine;
    private final AdminRegistry registry;
    private boolean shutdownRequested;
    /** Creates one engine administrator's command context after endpoint authentication. */
    public EngineAdminCommandSession(JVMud engine, AdminRegistry registry) { this.engine = engine; this.registry = registry; }
    @Override public String scope() { return "engine"; }
    @Override public Reply execute(String line) {
        CommandLine command = CommandLine.parse(line);
        if (command.isBlank()) return new Reply("", true);
        try {
            String text = switch (command.name()) {
                case "help" -> """
                        status | mudlibs                         Show engine and mudlib state
                        start <config> [player-port admin-port] Boot a mudlib (omitted ports are allocated)
                        stop <id> | restart <id>                 Manage one mudlib
                        admins                                  List administrators and grants
                        admin-create <name>                     Issue an access token (shown once)
                        admin-rotate <name>                     Replace a token and invalidate sessions
                        admin-delete <name>                     Delete an administrator
                        grant <name> <engine|mudlib:id>          Grant administration authority
                        revoke <name> <engine|mudlib:id>         Revoke authority and disconnect sessions
                        shutdown                                Stop the engine and all mudlibs
                        quit                                    Disconnect this console
                        """;
                case "status", "mudlibs" -> engine.describe();
                case "start" -> {
                    boolean player = !command.optional(1, "").isEmpty(), admin = !command.optional(2, "").isEmpty();
                    if (player != admin) throw new IllegalArgumentException("Supply both player and admin ports, or neither.");
                    var status = engine.startMudlib(MudlibSpec.fromConfig(Path.of(command.required(0))),
                            port(command.optional(1, "0")), port(command.optional(2, "0")));
                    yield status + "\n";
                }
                case "stop" -> { engine.stopMudlib(command.required(0)); yield "Mudlib stopped.\n"; }
                case "restart" -> engine.restartMudlib(command.required(0)) + "\n";
                case "admins" -> registry.describe();
                case "admin-create" -> "Access token (store securely; shown once): " + registry.create(command.required(0)) + "\n";
                case "admin-rotate" -> "Replacement access token (store securely; shown once): " + registry.rotate(command.required(0)) + "\n";
                case "admin-delete" -> { registry.remove(command.required(0)); yield "Administrator deleted.\n"; }
                case "grant", "revoke" -> { registry.grant(command.required(0), command.required(1), command.name().equals("grant")); yield "Grant updated.\n"; }
                case "quit" -> "Disconnected.\n";
                case "shutdown" -> { shutdownRequested = true; yield "Engine shutdown requested.\n"; }
                default -> "Unknown engine command. Type help.\n";
            };
            return new Reply(text, !command.name().equals("quit") && !command.name().equals("shutdown"));
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            return new Reply("Error: " + e.getMessage() + "\n", true);
        }
    }
    /** Runs shutdown after transport has flushed the final reply and closed the command session. */
    @Override public void close() {
        if (shutdownRequested) Thread.ofVirtual().name("jvmud-requested-shutdown").start(engine::close);
    }

    private static int port(String text) {
        int value = Integer.parseInt(text);
        if (value < 0 || value > 65535) throw new IllegalArgumentException("Port out of range.");
        return value;
    }
}
