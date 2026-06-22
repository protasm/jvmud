package io.github.protasm.jvmud.instance;

import io.github.protasm.jvmud.compiler.exec.LPCObjectLoadObserver;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Routes one hosted entrypoint across mounted mudlib worlds. */
public final class MudlibRouter implements InstanceHost {
    private final Map<String, MudInstance> mudsByGameId = new LinkedHashMap<>();
    private final Map<String, InstancePersona> suspendedDefaultPersonasBySession = new HashMap<>();
    private final MudInstance defaultMud;
    private int nextSessionId = 1;

    private MudlibRouter(MudInstance defaultMud) {
        this.defaultMud = Objects.requireNonNull(defaultMud, "defaultMud");
        register(defaultMud);
    }

    public static MudlibRouter boot(Path defaultMudlibRoot, String defaultConfigPath) {
        return boot(defaultMudlibRoot, defaultConfigPath, MudlibBootProgress.none(), LPCObjectLoadObserver.NONE);
    }

    /**
     * Boots the default mudlib router and reports host-visible progress while startup objects load.
     *
     * @param defaultMudlibRoot filesystem root of the default mudlib
     * @param defaultConfigPath mudlib-relative JVMud configuration path for the default mudlib
     * @param progress callback for local startup progress events
     * @return router with the default mudlib, and any automatic sibling mounts, registered
     */
    public static MudlibRouter boot(Path defaultMudlibRoot, String defaultConfigPath, MudlibBootProgress progress) {
        return boot(defaultMudlibRoot, defaultConfigPath, progress, LPCObjectLoadObserver.NONE);
    }

    /**
     * Boots the default mudlib router with host-visible preload and object-load diagnostics.
     *
     * @param defaultMudlibRoot filesystem root of the default mudlib
     * @param defaultConfigPath mudlib-relative JVMud configuration path for the default mudlib
     * @param progress callback for local startup progress events
     * @param objectLoadObserver observer for each shared LPC object load attempt
     * @return router with the default mudlib, and any automatic sibling mounts, registered
     */
    public static MudlibRouter boot(
            Path defaultMudlibRoot,
            String defaultConfigPath,
            MudlibBootProgress progress,
            LPCObjectLoadObserver objectLoadObserver) {
        MudInstance defaultMud = MudInstance.boot(defaultMudlibRoot, defaultConfigPath, progress, objectLoadObserver);
        MudlibRouter host = new MudlibRouter(defaultMud);
        if ("lpmuseum".equals(defaultMud.gameId())) {
            Path siblingLp245 = defaultMud.mudlibRoot().getParent().resolve("lp245");
            if (Files.isDirectory(siblingLp245)) {
                host.register(MudInstance.boot(
                        siblingLp245, MudlibBoot.LP245_CONFIG_PATH, progress, objectLoadObserver));
            }
        }
        return host;
    }

    private void register(MudInstance mud) {
        mud.setTransferHandler(this::requestTransfer);
        mudsByGameId.put(mud.gameId(), mud);
    }

    private int requestTransfer(MudInstance sourceMud, Object actor, String gameId) {
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
        for (MudInstance mud : mudsByGameId.values()) {
            mud.advanceWorldTick();
        }
    }

    @Override
    public synchronized void shutdown(Object reason) {
        for (MudInstance mud : mudsByGameId.values()) {
            mud.shutdown(reason);
        }
    }

    @Override
    public synchronized InstancePersona attachPersona(PrintWriter out, String remoteAddress) {
        return defaultMud.attachPersona("telnet/" + nextSessionId++, out, remoteAddress, true);
    }

    @Override
    public synchronized void detachPersona(InstancePersona persona) {
        if (persona != null) {
            persona.mud().detachPersona(persona);
        }
    }

    @Override
    public synchronized Object dispatch(InstancePersona persona, PrintWriter out, String commandLine) {
        MudInstance sourceMud = persona.mud();
        Object result = sourceMud.dispatch(persona, out, commandLine);
        String destinationGameId = sourceMud.consumeRequestedTransfer(persona);
        if (destinationGameId != null) {
            transfer(persona, out, destinationGameId);
        }
        return result;
    }

    private void transfer(InstancePersona persona, PrintWriter out, String destinationGameId) {
        MudInstance destinationMud = mudsByGameId.get(destinationGameId);
        if (destinationMud == null) {
            out.println("The exhibit portal flickers, but no destination answers.");
            return;
        }

        MudInstance sourceMud = persona.mud();
        if (destinationMud == defaultMud && sourceMud != defaultMud) {
            InstancePersona suspended = suspendedDefaultPersonasBySession.remove(persona.sessionId());
            if (suspended == null) {
                out.println("The return portal flickers, but your museum Persona does not answer.");
                return;
            }
            sourceMud.detachPersona(persona, true);
            out.println("The return portal opens.");
            persona.replaceWith(defaultMud.resumePersona(suspended, out, persona.remoteAddress()));
            return;
        }

        if (sourceMud == defaultMud) {
            suspendedDefaultPersonasBySession.put(persona.sessionId(), snapshot(persona));
            sourceMud.suspendPersonaForTransfer(persona);
        } else {
            sourceMud.detachPersona(persona, false);
        }
        out.println("The exhibit portal opens.");
        InstancePersona replacement = destinationMud.attachVisitingPersona(
                persona.sessionId(),
                out,
                persona.remoteAddress(),
                persona.userId(),
                persona.gender());
        persona.replaceWith(replacement);
    }

    private InstancePersona snapshot(InstancePersona persona) {
        return new InstancePersona(
                persona.mud(),
                persona.sessionId(),
                persona.objectId(),
                persona.name(),
                persona.userId(),
                persona.gender(),
                persona.actor(),
                persona.remoteAddress());
    }

    @Override
    public synchronized void printPromptIfReady(InstancePersona persona, PrintWriter out) {
        if (persona != null) {
            persona.mud().printPromptIfReady(persona, out);
        }
    }

    @Override
    public synchronized boolean isCapturingInput(InstancePersona persona) {
        return persona != null && persona.mud().isCapturingInput(persona);
    }

    @Override
    public synchronized boolean isCapturingNoEchoInput(InstancePersona persona) {
        return persona != null && persona.mud().isCapturingNoEchoInput(persona);
    }

    @Override
    public synchronized boolean isAttached(InstancePersona persona) {
        return persona != null && persona.mud().isAttached(persona);
    }
}
