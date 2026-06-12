package io.github.protasm.jvmud.server;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Telnet host that can route one listener across multiple booted mudlib instances. */
final class MultiMudTelnetHost implements TelnetHost {
    private final Map<String, TelnetMud> mudsByGameId = new LinkedHashMap<>();
    private final TelnetMud defaultMud;
    private int nextSessionId = 1;

    private MultiMudTelnetHost(TelnetMud defaultMud) {
        this.defaultMud = Objects.requireNonNull(defaultMud, "defaultMud");
        register(defaultMud);
    }

    static MultiMudTelnetHost boot(Path defaultMudlibRoot, String defaultConfigPath) {
        TelnetMud defaultMud = TelnetMud.boot(defaultMudlibRoot, defaultConfigPath);
        MultiMudTelnetHost host = new MultiMudTelnetHost(defaultMud);
        if ("lpmuseum".equals(defaultMud.gameId())) {
            Path siblingLp245 = defaultMud.mudlibRoot().getParent().resolve("lp245");
            if (Files.isDirectory(siblingLp245)) {
                host.register(TelnetMud.boot(siblingLp245, MudlibBoot.DEFAULT_CONFIG_PATH));
            }
        }
        return host;
    }

    private void register(TelnetMud mud) {
        mud.setTransferHandler(this::requestTransfer);
        mudsByGameId.put(mud.gameId(), mud);
    }

    private int requestTransfer(TelnetMud sourceMud, Object actor, String gameId) {
        if (!mudsByGameId.containsKey(gameId)) {
            return 0;
        }
        sourceMud.requestTransfer(actor, gameId);
        return 1;
    }

    @Override
    public Path mudlibRoot() {
        return defaultMud.mudlibRoot();
    }

    @Override
    public MudlibBootResult bootResult() {
        return defaultMud.bootResult();
    }

    @Override
    public Duration worldTickInterval() {
        return defaultMud.worldTickInterval();
    }

    @Override
    public synchronized void advanceWorldTick() {
        for (TelnetMud mud : mudsByGameId.values()) {
            mud.advanceWorldTick();
        }
    }

    @Override
    public synchronized TelnetPersona attachPersona(PrintWriter out, String remoteAddress) {
        return defaultMud.attachPersona("telnet/" + nextSessionId++, out, remoteAddress, true);
    }

    @Override
    public synchronized void detachPersona(TelnetPersona persona) {
        if (persona != null) {
            persona.mud().detachPersona(persona);
        }
    }

    @Override
    public synchronized Object dispatch(TelnetPersona persona, PrintWriter out, String commandLine) {
        TelnetMud sourceMud = persona.mud();
        Object result = sourceMud.dispatch(persona, out, commandLine);
        String destinationGameId = sourceMud.consumeRequestedTransfer(persona);
        if (destinationGameId != null) {
            transfer(persona, out, destinationGameId);
        }
        return result;
    }

    private void transfer(TelnetPersona persona, PrintWriter out, String destinationGameId) {
        TelnetMud destinationMud = mudsByGameId.get(destinationGameId);
        if (destinationMud == null) {
            out.println("The exhibit portal flickers, but no destination answers.");
            return;
        }
        persona.mud().detachPersona(persona, false);
        out.println("The exhibit portal opens.");
        TelnetPersona replacement = destinationMud.attachPersona(
                persona.sessionId(),
                out,
                persona.remoteAddress(),
                false);
        persona.replaceWith(replacement);
    }

    @Override
    public synchronized void printPromptIfReady(TelnetPersona persona, PrintWriter out) {
        if (persona != null) {
            persona.mud().printPromptIfReady(persona, out);
        }
    }

    @Override
    public synchronized boolean isAttached(TelnetPersona persona) {
        return persona != null && persona.mud().isAttached(persona);
    }
}
