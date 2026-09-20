package io.github.protasm.jvmud.instance;

import io.github.protasm.jvmud.admin.MudlibAdminCommandSession;
import io.github.protasm.jvmud.engine.worker.WorkerWire;
import io.github.protasm.jvmud.transport.telnet.TelnetServer;
import java.io.*;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;

/** Private worker JVM entry point. Owns exactly one mudlib and accepts administration only through parent pipes. */
public final class MudlibWorker {
    private MudlibWorker() {}

    /** Boots a single mudlib, reports readiness, and exits when its parent closes the control pipe. */
    public static void main(String[] args) throws Exception {
        var input = new DataInputStream(System.in);
        var output = new DataOutputStream(System.out);
        System.setOut(System.err);
        WorkerWire.Boot request = WorkerWire.read(input, WorkerWire.Boot.class);
        ProcessHandle parent = ProcessHandle.current().parent().orElseThrow();
        Thread.ofVirtual().name("jvmud-parent-watch").start(() -> {
            while (parent.isAlive()) {
                try { Thread.sleep(1000); } catch (InterruptedException e) { return; }
            }
            Runtime.getRuntime().halt(1);
        });
        StartupObjectLoadTrace trace = new StartupObjectLoadTrace(request.trace());
        MudInstance mud = MudInstance.boot(Path.of(request.root()), request.config(), MudlibBootProgress.none(), trace);
        trace.finishStartup(); trace.printSummaryIfEnabled();
        mud.startExecution();
        byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes);
        String secret = HexFormat.of().formatHex(bytes);
        try (TelnetServer player = new TelnetServer(mud, secret)) {
            player.start();
            WorkerWire.write(output, new WorkerWire.Ready(mud.gameId(), mud.gameName(), player.port(), secret, preloadSummary(mud.bootResult())));
            Map<String, Session> sessions = new HashMap<>();
            while (true) {
                WorkerWire.Request command;
                try { command = WorkerWire.read(input, WorkerWire.Request.class); }
                catch (EOFException e) { break; }
                WorkerWire.Response response;
                if (command.operation().equals("STOP")) break;
                if (command.operation().equals("OPEN")) {
                    if (sessions.size() >= 64) throw new IOException("Worker administration session limit reached.");
                    StringWriter text = new StringWriter();
                    var interpreter = mud.administer(runtime -> MudlibAdminCommandSession.attach(new PrintWriter(text, true), runtime, mud.mudlibRoot()));
                    sessions.put(command.session(), new Session(text, interpreter));
                    response = new WorkerWire.Response("", true);
                } else if (command.operation().equals("CLOSE")) {
                    sessions.remove(command.session()); response = new WorkerWire.Response("", false);
                } else if (command.operation().equals("COMMAND")) {
                    Session session = sessions.get(command.session());
                    if (session == null) throw new IOException("Unknown worker command session.");
                    response = mud.administer(runtime -> {
                        session.output().getBuffer().setLength(0);
                        session.interpreter().execute(command.command());
                        return new WorkerWire.Response(session.output().toString(), session.interpreter().isRunning());
                    });
                } else throw new IOException("Unknown worker operation.");
                WorkerWire.write(output, response);
            }
        } finally { mud.shutdown(0); }
    }

    private static String preloadSummary(MudlibBootResult result) {
        if (result.mudlibBoundary().preloadFilePath().isEmpty()) return "preload manifest: none declared.";
        String summary = "preload manifest " + result.mudlibBoundary().preloadFilePath().orElseThrow()
                + ": compiled " + result.preloadManifestPreloadedObjects().size() + " object(s), skipped "
                + result.preloadManifestSkippedPreloads().size() + " object(s).";
        if (!result.preloadManifestSkippedPreloads().isEmpty()) summary += " Skipped: " + String.join(", ", result.preloadManifestSkippedPreloads());
        return summary;
    }

    private record Session(StringWriter output, MudlibAdminCommandSession interpreter) {}
}
