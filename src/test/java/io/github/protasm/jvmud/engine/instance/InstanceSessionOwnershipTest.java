package io.github.protasm.jvmud.engine.instance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.engine.configuration.MudlibConfiguration;
import io.github.protasm.jvmud.engine.model.mudlib.MudlibLifecycleEvent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/** A connection handle authorizes operations only in the instance that owns it. */
final class InstanceSessionOwnershipTest {
    @Test
    void destroyingAnAttachedContainerMovesContentsBeforeRemovingItsWorldBinding() {
        MudInstance instance = MudInstance.boot(Path.of("mudlibs/smallmercies"), "jvmud/smallmercies.config");
        try {
            var session = instance.attachSession(new PrintWriter(new StringWriter(), true), "127.0.0.1");
            DepartureCounter container = new DepartureCounter();
            instance.administer(runtime -> {
                runtime.configureMudlib(MudlibConfiguration.builder()
                        .lifecycleMethod(MudlibLifecycleEvent.ENTITY_DEPARTED_FROM_PLACE, "depart")
                        .build());
                runtime.registerHostObject("container", container);
                assertTrue(runtime.rebindSessionLpcObject(container, session.personaObject()));
                var child = runtime.loadSource("departure_child.c", """
                        void activate() { jvmud_enable_commands(); }
                        """);
                child.invoke("activate");
                runtime.moveObject(child.instance(), container);

                runtime.destructObject(container);

                assertEquals(1, container.departures);
                assertNull(runtime.environment(child.instance()));
                assertTrue(runtime.worldProjection().projectedEntity(container).isEmpty());
                return null;
            });
            assertFalse(instance.isOpen(session));
        } finally {
            instance.shutdown(0);
        }
    }

    @Test
    void transferringAnExistingPersonaPreservesItsNativeEntityAndLocation() {
        MudInstance instance = MudInstance.boot(Path.of("mudlibs/smallmercies"), "jvmud/smallmercies.config");
        try {
            var first = instance.attachSession(new PrintWriter(new StringWriter(), true), "127.0.0.1");
            var second = instance.attachSession(new PrintWriter(new StringWriter(), true), "127.0.0.2");
            Object selected = second.personaObject();
            var entity = second.attachment().orElseThrow().entity();
            Object location = instance.administer(runtime -> runtime.environment(selected));

            boolean transferred = instance.administer(runtime -> runtime.rebindSessionLpcObject(selected, first.personaObject()));
            assertTrue(transferred);

            assertTrue(instance.isOpen(first));
            assertFalse(instance.isOpen(second));
            assertEquals(selected, first.personaObject());
            assertEquals(entity, first.attachment().orElseThrow().entity());
            assertEquals(entity, instance.administer(runtime -> runtime.worldProjection()
                    .projectedEntity(selected).orElseThrow()));
            assertEquals(location, instance.administer(runtime -> runtime.environment(selected)));
        } finally {
            instance.shutdown(0);
        }
    }

    @Test
    void foreignSessionCannotMutateAnotherWorldWithTheSameSessionId() {
        MudInstance first = MudInstance.boot(Path.of("mudlibs/smallmercies"), "jvmud/smallmercies.config");
        MudInstance second = MudInstance.boot(Path.of("mudlibs/smallmercies"), "jvmud/smallmercies.config");
        try {
            var local = first.attachSession(new PrintWriter(new StringWriter(), true), "127.0.0.1");
            var foreign = second.attachSession(new PrintWriter(new StringWriter(), true), "127.0.0.2");
            assertEquals(local.id(), foreign.id(), "Session ids are scoped to their instance");
            List<String> localOutput = new ArrayList<>();
            List<String> foreignOutput = new ArrayList<>();
            first.bindClientProtocolSink(local, (protocol, message) -> localOutput.add(message));
            first.setClientProtocolEnabled(local, "GMCP", true);
            localOutput.clear();

            first.bindClientProtocolSink(foreign, (protocol, message) -> foreignOutput.add(message));
            first.setClientProtocolEnabled(foreign, "GMCP", false);
            first.receiveClientProtocolMessage(foreign, "GMCP", "Core.Hello {}");
            first.detachSession(foreign);

            assertTrue(first.isOpen(local));
            assertTrue(second.isOpen(foreign));
            assertFalse(first.isOpen(foreign));
            assertFalse(first.isCapturingInput(foreign));
            assertFalse(first.isCapturingNoEchoInput(foreign));
            boolean delivered = first.administer(runtime -> runtime.sessions()
                    .sendProtocolMessage(local.personaObject(), "GMCP", "local-only"));
            assertTrue(delivered);
            assertEquals(List.of("local-only"), localOutput);
            assertTrue(foreignOutput.isEmpty());
        } finally {
            first.shutdown(0);
            second.shutdown(0);
        }
    }

    /** An authored-style departure hook that records ordered inventory cleanup. */
    public static final class DepartureCounter {
        int departures;

        public void depart(Object child) {
            departures++;
        }
    }
}
