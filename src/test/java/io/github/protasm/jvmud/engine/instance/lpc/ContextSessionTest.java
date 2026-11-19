package io.github.protasm.jvmud.engine.instance.lpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.language.execution.LPCProgramRevision;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/** Guards the authored-object adapter before it changes authoritative session or world state. */
final class ContextSessionTest {
    @Test
    void falseAndDestroyedTargetsCannotReplaceThePersonaOrCreateGhostEntities() {
        MudlibRuntimeContext context = new MudlibRuntimeContext(null);
        HostActor actor = new HostActor();
        HostActor destroyed = new HostActor();
        context.registerObject("actor", actor);
        context.registerObject("destroyed", destroyed);
        context.destructObject(destroyed);
        context.bindSession("one", actor, "127.0.0.1", ignored -> {});
        context.setSessionProtocolEnabled(actor, "GMCP", true);
        context.withCurrentObject(actor, () -> {
            context.captureSessionInput("reply", true);
            return null;
        });

        for (Object invalid : List.of(0, 7, false, "not an object", List.of(), Map.of(), new Object[0], destroyed)) {
            assertFalse(context.rebindSessionLpcObject(invalid, actor));
            assertFalse(context.rebindSessionLpcObject(actor, invalid));
            assertSame(actor, context.lpcObjectForSession("one").orElseThrow());
            assertTrue(context.hasCapturedSessionInput(actor));
            assertTrue(context.isSessionProtocolEnabled(actor, "GMCP"));
            assertTrue(context.worldProjection().projectedEntity(invalid).isEmpty());
            assertThrows(IllegalArgumentException.class,
                    () -> context.bindSession("invalid", invalid, "127.0.0.1", ignored -> {}));
            assertTrue(context.sessions().session("invalid").isEmpty());
        }
    }

    @Test
    void historicalJavaReceiversResolveToTheSameLivePersonaIdentity() {
        MudlibRuntimeContext context = new MudlibRuntimeContext(null);
        HostActor identity = new HostActor();
        context.registerObject("actor", identity);
        context.objectLifecycle().register(identity, new LPCProgramRevision("actor", HostActor.class));
        StringBuilder output = new StringBuilder();
        context.bindSession("one", identity, "127.0.0.1", output::append);
        var entity = context.worldProjection().projectedEntity(identity).orElseThrow();
        var prepared = context.objectLifecycle().prepareReload(identity, new LPCProgramRevision("actor", HostActor.class));
        context.objectLifecycle().install(prepared);
        Object receiver = context.objectReceiver(identity);

        assertTrue(context.rebindSessionLpcObject(receiver, receiver));
        assertTrue(context.isInteractive(receiver));
        context.setSessionProtocolEnabled(receiver, "GMCP", true);
        List<String> packets = new java.util.ArrayList<>();
        context.bindSessionProtocolSink(receiver, (protocol, text) -> packets.add(protocol + ":" + text));
        assertTrue(context.isSessionProtocolEnabled(receiver, "GMCP"));
        assertTrue(context.sendSessionProtocolMessage(receiver, "GMCP", "shared route"));
        assertEquals(List.of("GMCP:shared route"), packets);
        assertEquals("127.0.0.1", context.queryIpNumber(receiver));
        assertEquals(context.personaRecordForProjection(identity), context.personaRecordForProjection(receiver));
        context.writeToLpcObject(receiver, "shared output");
        assertEquals("shared output", output.toString());
        context.withCurrentObject(receiver, () -> {
            context.captureSessionInput("reply", true);
            return null;
        });
        assertTrue(context.hasCapturedSessionInput(receiver));
        assertTrue(context.capturedSessionInputNoEcho(receiver));

        assertSame(identity, context.lpcObjectForSession("one").orElseThrow());
        assertSame(entity, context.sessions().attachment("one").orElseThrow().entity());
        assertTrue(context.worldProjection().projectedEntity(receiver).isEmpty());
    }

    /** A valid opaque host receiver; the adapter must not require authored generated classes. */
    public static final class HostActor {}
}
