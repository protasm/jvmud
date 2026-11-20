package io.github.protasm.jvmud.engine.instance.lpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.engine.configuration.MudlibConfiguration;
import io.github.protasm.jvmud.engine.instance.efun.EngineEfuns;
import io.github.protasm.jvmud.engine.model.mudlib.MudlibLifecycleEvent;
import io.github.protasm.jvmud.language.execution.LPCExecutionOptions;
import io.github.protasm.jvmud.language.execution.LPCObjectHandle;
import io.github.protasm.jvmud.language.execution.LPCRuntimeException;
import io.github.protasm.jvmud.language.runtime.RuntimeContext;
import io.github.protasm.jvmud.language.runtime.RuntimeContextHolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Preserves the lifecycle effects layered on the authoritative world containment graph. */
final class LPCWorldInteractionCompatibilityTest {
    @TempDir
    Path directory;

    @Test
    void attachedArrivalRunsOrderedInteractionScopesAndImmediatelyRefreshesCommands() {
        MudlibRuntimeContext context = context();
        WorldObject origin = register(context, "origin");
        WorldObject destination = register(context, "destination");
        WorldObject arriving = register(context, "arriving");
        WorldObject resident = register(context, "resident");
        WorldObject nearby = register(context, "nearby");
        WorldObject carried = register(context, "carried");
        context.moveObject(arriving, origin);
        context.moveObject(resident, destination);
        context.moveObject(nearby, destination);
        context.moveObject(carried, arriving);
        context.bindSession("arriving", arriving, "local", ignored -> {});
        context.bindSession("resident", resident, "local", ignored -> {});
        var personaEntity = context.sessions().attachment("arriving").orElseThrow().entity();
        List<String> events = new ArrayList<>();
        for (WorldObject receiver : List.of(arriving, destination, resident, nearby, carried)) {
            receiver.onInteraction = () -> {
                assertSame(context, RuntimeContextHolder.current());
                events.add(context.objectId(context.currentCommandActor()) + "<-" + context.objectId(context.currentObject()));
            };
        }
        origin.onDeparture = actor -> {
            assertSame(arriving, actor);
            assertSame(arriving, context.currentCommandActor());
            assertSame(destination, context.environment(actor), "Containment changes before departure is delivered");
            events.add("depart origin");
        };
        Runnable destinationScope = destination.onInteraction;
        destination.onInteraction = () -> {
            destinationScope.run();
            registerCurrentCommand(context, "new");
        };
        Runnable carriedScope = carried.onInteraction;
        carried.onInteraction = () -> {
            carriedScope.run();
            registerCurrentCommand(context, "carried");
        };
        context.withScopedCommandRegistration(() -> {
            registerCommand(context, arriving, origin, "old");
            return null;
        });

        context.moveObject(arriving, destination);

        assertEquals(List.of("depart origin", "resident<-arriving", "arriving<-arriving",
                "arriving<-destination", "arriving<-resident", "arriving<-nearby", "arriving<-carried"), events);
        assertEquals(0, dispatch(context, arriving, "old"));
        assertEquals(true, dispatch(context, arriving, "new"));
        assertEquals(true, dispatch(context, arriving, "carried"));
        assertSame(personaEntity, context.sessions().attachment("arriving").orElseThrow().entity());
        assertSame(personaEntity, context.worldProjection().projectedEntity(arriving).orElseThrow());
        assertSame(context.worldProjection().projectedEntity(destination).orElseThrow(),
                context.worldProjection().worldRuntime().locationOf(personaEntity));
        assertEquals(List.of(resident, nearby, arriving), context.worldProjection().inventory(destination));
        assertSame(resident, context.firstInventory(destination));
        assertSame(nearby, context.nextInventory(resident));
        assertSame(arriving, context.nextInventory(nearby));
        assertNull(context.nextInventory(arriving));
        assertSame(arriving, context.environment(carried));
        assertThrows(IllegalArgumentException.class, () -> context.moveObject(destination, carried));
        assertEquals(List.of(resident, nearby, arriving), context.worldProjection().inventory(destination));
        context.moveObject(arriving, destination);
        assertEquals(7, events.size(), "A no-op move must not repeat lifecycle delivery");
    }

    @Test
    void departureRedirectRunsTheIntermediateDepartureAndSkipsTheStaleArrival() {
        MudlibRuntimeContext context = context();
        WorldObject origin = register(context, "origin");
        WorldObject requested = register(context, "requested");
        WorldObject redirected = register(context, "redirected");
        WorldObject actor = register(context, "actor");
        context.moveObject(actor, origin);
        context.bindSession("actor", actor, "local", ignored -> {});
        var entity = context.sessions().attachment("actor").orElseThrow().entity();
        List<String> events = new ArrayList<>();
        origin.onDeparture = departing -> {
            events.add("origin departure");
            assertSame(requested, context.environment(departing));
            context.moveObject(departing, redirected);
        };
        requested.onDeparture = departing -> {
            events.add("requested departure");
            assertSame(redirected, context.environment(departing));
        };
        requested.onInteraction = () -> {
            throw new AssertionError("The original destination must not receive a stale arrival");
        };
        redirected.onInteraction = () -> events.add("redirected arrival");

        context.moveObject(actor, requested);

        assertEquals(List.of("origin departure", "requested departure", "redirected arrival"), events);
        assertSame(redirected, context.environment(actor));
        assertEquals(List.of(), context.worldProjection().inventory(origin));
        assertEquals(List.of(), context.worldProjection().inventory(requested));
        assertEquals(List.of(actor), context.worldProjection().inventory(redirected));
        assertSame(entity, context.sessions().attachment("actor").orElseThrow().entity());
    }

    @Test
    void departureDestructionSkipsArrivalAndClearsProjectionAliasesAndContainedObjects() {
        MudlibRuntimeContext context = context();
        WorldObject origin = register(context, "origin");
        WorldObject destination = register(context, "destination");
        WorldObject actor = register(context, "actor");
        WorldObject carried = register(context, "carried");
        context.moveObject(actor, origin);
        context.moveObject(carried, actor);
        context.bindSession("actor", actor, "local", ignored -> {});
        context.bindEntityAlias(actor, "living", "traveler");
        context.setEntityTranslucent(actor, false);
        context.runWithCurrentObject(actor, () -> context.setLight(3));
        var carriedEntity = context.worldProjection().projectedEntity(carried).orElseThrow();
        origin.onDeparture = context::destructObject;
        destination.onInteraction = () -> {
            throw new AssertionError("A destroyed actor must not receive an arrival scope");
        };

        context.moveObject(actor, destination);

        assertTrue(context.isDestroyedObject(actor));
        assertFalse(context.isInteractive(actor));
        assertTrue(context.lpcObjectForSession("actor").isEmpty());
        assertTrue(context.worldProjection().projectedEntity(actor).isEmpty());
        assertNull(context.environment(carried));
        assertSame(carriedEntity, context.worldProjection().projectedEntity(carried).orElseThrow());
        assertEquals(List.of(), context.worldProjection().inventory(destination));
        assertNull(context.findEntityAlias("living", "traveler"));
        assertEquals(0, context.entityHasAlias(actor, "living"));
        assertTrue(context.entityTranslucent(actor), "Destruction clears opacity metadata");
        assertEquals(0, context.withCurrentObject(destination, () -> context.setLight(0)));
    }

    @Test
    void failedArrivalKeepsCommittedMovementButRestoresCallerAndRegistrationScopes() {
        MudlibRuntimeContext context = context();
        WorldObject origin = register(context, "origin");
        WorldObject destination = register(context, "destination");
        WorldObject actor = register(context, "actor");
        WorldObject laterHandler = register(context, "later_handler");
        Object ambientObject = new Object();
        Object ambientActor = new Object();
        RuntimeContext ambientRuntime = new MudlibRuntimeContext(null);
        context.moveObject(actor, origin);
        context.bindSession("actor", actor, "local", ignored -> {});
        destination.onInteraction = () -> {
            registerCurrentCommand(context, "partial");
            context.rememberActionMethod("perform");
            throw new IllegalStateException("arrival interrupted");
        };
        RuntimeContext previous = RuntimeContextHolder.current();
        RuntimeContextHolder.setCurrent(ambientRuntime);
        try {
            context.withCurrentObject(ambientObject, () -> context.withCommandActor(ambientActor, () -> {
                IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                        () -> context.moveObject(actor, destination));
                assertTrue(failure.getMessage().contains("arrival interrupted"), failure.getMessage());
                assertSame(ambientRuntime, RuntimeContextHolder.current());
                assertSame(ambientObject, context.currentObject());
                assertSame(ambientActor, context.currentCommandActor());
                return null;
            }));
        } finally {
            RuntimeContextHolder.setCurrent(previous);
        }

        assertSame(destination, context.environment(actor), "Authored lifecycle failure does not roll movement back");
        assertEquals(List.of(actor), context.worldProjection().inventory(destination));
        assertEquals(true, dispatch(context, actor, "partial"), "Actions registered before failure remain until refresh");
        context.withCommandActor(actor, () -> context.withCurrentObject(laterHandler, () -> {
            context.registerVerb("leaked_pending_method");
            return null;
        }));
        assertEquals(0, dispatch(context, actor, "leaked_pending_method"));
        registerCommand(context, actor, laterHandler, "persistent_after_failure");
        context.clearCommandActions(actor);
        assertEquals(0, dispatch(context, actor, "partial"));
        assertEquals(true, dispatch(context, actor, "persistent_after_failure"), "Scoped registration depth must unwind on failure");
        assertNull(context.currentObject());
        assertNull(context.currentCommandActor());
    }

    @Test
    void explicitRefreshUsesOneScopedPassAndPrefetchesEachNextInventoryObject() {
        MudlibRuntime runtime = new MudlibRuntime(LPCExecutionOptions.builder().baseIncludePath(directory).build());
        runtime.configureMudlib(configuration());
        MudlibRuntimeContext context = runtime.withRuntimeContext(
                () -> (MudlibRuntimeContext) RuntimeContextHolder.requireCurrent());
        WorldObject actor = register(context, "actor");
        WorldObject room = register(context, "room");
        WorldObject first = register(context, "first");
        WorldObject prefetched = register(context, "prefetched");
        WorldObject later = register(context, "later");
        WorldObject carried = register(context, "carried");
        context.moveObject(actor, room);
        context.moveObject(first, room);
        context.moveObject(prefetched, room);
        context.moveObject(later, room);
        context.moveObject(carried, actor);
        List<String> events = new ArrayList<>();
        actor.onInteraction = () -> {
            events.add("actor");
            if (events.size() == 1) context.rememberActionMethod("perform");
        };
        room.onInteraction = () -> {
            events.add("room");
            assertSame(context, RuntimeContextHolder.current());
            assertSame(actor, context.currentCommandActor());
            // A pending method survives from the preceding hook within the same registration pass.
            context.registerVerb("spanning_hooks");
        };
        first.onInteraction = () -> {
            events.add("first");
            context.rememberActionMethod("perform");
            context.moveObject(prefetched, null);
        };
        prefetched.onInteraction = () -> {
            events.add("prefetched");
            context.registerVerb("detached_handler");
        };
        later.onInteraction = () -> {
            throw new AssertionError("Traversal stops after the prefetched sibling has become detached");
        };
        carried.onInteraction = () -> {
            events.add("carried");
            registerCurrentCommand(context, "carried");
            context.rememberActionMethod("perform");
        };

        runtime.refreshCommandActions(actor);

        assertEquals(List.of("actor", "room", "actor", "first", "prefetched", "carried"), events);
        assertNull(context.environment(prefetched));
        assertEquals(true, runtime.dispatchCommand(actor, "spanning_hooks"));
        assertEquals(true, runtime.dispatchCommand(actor, "detached_handler"));
        assertEquals(true, runtime.dispatchCommand(actor, "carried"));
        context.withCommandActor(actor, () -> context.withCurrentObject(room, () -> {
            context.registerVerb("leaked_pending_method");
            return null;
        }));
        assertEquals(0, runtime.dispatchCommand(actor, "leaked_pending_method"));
        registerCommand(context, actor, actor, "personal");

        runtime.configureMudlib(MudlibConfiguration.empty());
        runtime.refreshCommandActions(actor);

        assertEquals(0, runtime.dispatchCommand(actor, "spanning_hooks"));
        assertEquals(0, runtime.dispatchCommand(actor, "detached_handler"));
        assertEquals(0, runtime.dispatchCommand(actor, "carried"));
        assertEquals(true, runtime.dispatchCommand(actor, "personal"), "A hookless refresh still clears scoped actions");
        assertEquals(6, events.size());
        assertNull(context.currentObject());
        assertNull(context.currentCommandActor());
    }

    @Test
    void occupiedRoomReloadKeepsAttachmentsAndActionsWhileMovementRefreshesTheNewBehavior() throws Exception {
        String roomSource = """
                int arrivals;
                void init() { arrivals += 1; jvmud_add_action("inspect", "inspect"); }
                int inspect(string argument) { jvmud_write("first revision\\n"); return 1; }
                int arrival_count() { return arrivals; }
                """;
        Path source = directory.resolve("room.c");
        Files.writeString(source, roomSource);
        MudlibRuntime runtime = new MudlibRuntime(LPCExecutionOptions.builder().baseIncludePath(directory).build());
        EngineEfuns.registerCore(runtime);
        runtime.configureMudlib(configuration());
        LPCObjectHandle room = runtime.load("room");
        LPCObjectHandle origin = runtime.loadSource("origin.c", """
                void init() { jvmud_add_action("where", "where"); }
                int where(string argument) { jvmud_write("origin\\n"); return 1; }
                """);
        String personaSource = """
                void setup() { jvmud_add_action("personal", "personal"); }
                int personal(string argument) { jvmud_write("personal\\n"); return 1; }
                """;
        LPCObjectHandle first = runtime.loadSource("first.c", personaSource);
        LPCObjectHandle second = runtime.loadSource("second.c", personaSource);
        first.invoke("setup");
        second.invoke("setup");
        StringBuilder firstOutput = new StringBuilder();
        StringBuilder secondOutput = new StringBuilder();
        runtime.bindSession("first", first.instance(), "local", firstOutput::append);
        runtime.bindSession("second", second.instance(), "local", secondOutput::append);
        var firstEntity = runtime.sessions().attachment("first").orElseThrow().entity();
        var secondEntity = runtime.sessions().attachment("second").orElseThrow().entity();
        runtime.moveObject(first.instance(), origin.instance());
        runtime.moveObject(first.instance(), room.instance());
        runtime.moveObject(second.instance(), room.instance());
        assertEquals(0, runtime.dispatchCommand(first.instance(), "where"));
        assertEquals(1, runtime.dispatchCommand(first.instance(), "inspect"));
        assertEquals("first revision\n", firstOutput.toString());
        String replacement = roomSource.replace("first revision", "replacement revision");
        Files.writeString(source, replacement);

        assertSame(room.instance(), runtime.reload("room").instance());
        assertEquals(2, room.invoke("arrival_count"), "Reload does not repeat arrival hooks");
        Files.writeString(source, "int incompatible_extra_field;\n" + replacement);
        assertThrows(LPCRuntimeException.class, () -> runtime.reload("room"));
        firstOutput.setLength(0);
        assertEquals(1, runtime.dispatchCommand(first.instance(), "inspect"));
        assertEquals(1, runtime.dispatchCommand(second.instance(), "inspect"));
        assertEquals("replacement revision\n", firstOutput.toString());
        assertEquals("replacement revision\n", secondOutput.toString());
        assertSame(firstEntity, runtime.sessions().attachment("first").orElseThrow().entity());
        assertSame(secondEntity, runtime.sessions().attachment("second").orElseThrow().entity());
        assertEquals(List.of(first.instance(), second.instance()), runtime.worldProjection().inventory(room.instance()));
        assertSame(first.instance(), runtime.lpcObjectForSession("first").orElseThrow());
        assertSame(second.instance(), runtime.lpcObjectForSession("second").orElseThrow());

        runtime.moveObject(first.instance(), origin.instance());
        firstOutput.setLength(0);
        assertEquals(0, runtime.dispatchCommand(first.instance(), "inspect"));
        assertEquals(1, runtime.dispatchCommand(first.instance(), "where"));
        assertEquals(1, runtime.dispatchCommand(first.instance(), "personal"));
        assertEquals("origin\npersonal\n", firstOutput.toString());
        runtime.moveObject(first.instance(), room.instance());
        firstOutput.setLength(0);
        assertEquals(1, runtime.dispatchCommand(first.instance(), "inspect"));
        assertEquals("replacement revision\n", firstOutput.toString());
        assertEquals(3, room.invoke("arrival_count"));
        assertSame(firstEntity, runtime.sessions().attachment("first").orElseThrow().entity());
    }

    private static MudlibConfiguration configuration() {
        return MudlibConfiguration.builder()
                .lifecycleMethod(MudlibLifecycleEvent.ENTITY_DEPARTED_FROM_PLACE, "depart")
                .lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED, "init")
                .build();
    }

    private static MudlibRuntimeContext context() {
        MudlibRuntimeContext context = new MudlibRuntimeContext(null);
        context.configureMudlib(configuration());
        return context;
    }

    private static WorldObject register(MudlibRuntimeContext context, String name) {
        WorldObject object = new WorldObject();
        context.registerObject(name, object);
        return object;
    }

    private static void registerCurrentCommand(MudlibRuntimeContext context, String verb) {
        context.rememberActionMethod("perform");
        context.registerVerb(verb);
    }

    private static void registerCommand(MudlibRuntimeContext context, Object actor, Object handler, String verb) {
        context.withCommandActor(actor, () -> context.withCurrentObject(handler, () -> {
            registerCurrentCommand(context, verb);
            return null;
        }));
    }

    private static Object dispatch(MudlibRuntimeContext context, Object actor, String command) {
        return context.withCommandActor(actor, () -> context.dispatchCommand(actor, command));
    }

    /** Public methods stand in for authored hooks so each scenario can observe or re-enter the runtime. */
    public static final class WorldObject {
        private Consumer<Object> onDeparture = actor -> {};
        private Runnable onInteraction = () -> {};

        public void depart(Object actor) {
            onDeparture.accept(actor);
        }

        public void init() {
            onInteraction.run();
        }

        public Object perform(String argument) {
            return true;
        }
    }
}
