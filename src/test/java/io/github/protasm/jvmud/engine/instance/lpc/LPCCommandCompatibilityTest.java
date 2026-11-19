package io.github.protasm.jvmud.engine.instance.lpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.protasm.jvmud.engine.configuration.MudlibConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

/** Preserves authored command ordering, failure messages and registration lifetimes. */
final class LPCCommandCompatibilityTest {
    @Test
    void nestedDeclinedCommandsForTheSameActorKeepSeparateFailureMessages() {
        MudlibRuntimeContext context = new MudlibRuntimeContext(null);
        List<String> output = new ArrayList<>();
        Object actor = connectedActor(context, "actor", output);
        register(context, actor, action(ignored -> {
            context.notifyCommandFailure("inner first\n");
            return false;
        }), "inner");
        register(context, actor, action(ignored -> {
            context.notifyCommandFailure("inner last\n");
            return 0;
        }), "inner");
        register(context, actor, action(ignored -> {
            context.notifyCommandFailure("outer failure\n");
            assertEquals(0, dispatch(context, actor, "inner"));
            assertEquals("outer", context.currentCommandVerb());
            assertSame(actor, context.currentCommandActor());
            return 0;
        }), "outer");

        assertEquals(0, dispatch(context, actor, "outer"));

        assertEquals(List.of("inner last\n", "outer failure\n"), output,
                "Each declined dispatch emits its own latest failure exactly once");
        assertNull(context.currentCommandVerb());
        assertNull(context.currentCommandActor());
    }

    @Test
    void nestedExceptionForAnotherActorDoesNotEmitItsFailureOrEraseTheOuterFailure() {
        MudlibRuntimeContext context = new MudlibRuntimeContext(null);
        List<String> outerOutput = new ArrayList<>();
        List<String> innerOutput = new ArrayList<>();
        Object outerActor = connectedActor(context, "outer_actor", outerOutput);
        Object innerActor = connectedActor(context, "inner_actor", innerOutput);
        register(context, innerActor, action(ignored -> {
            context.notifyCommandFailure("discarded inner failure\n");
            throw new IllegalStateException("nested action failed");
        }), "inner");
        register(context, outerActor, action(ignored -> {
            context.notifyCommandFailure("outer failure\n");
            IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                    () -> dispatch(context, innerActor, "inner"));
            assertEquals("Failed to call generated LPC method: nested action failed", failure.getMessage());
            assertSame(outerActor, context.currentCommandActor());
            assertEquals("outer", context.currentCommandVerb());
            return 0;
        }), "outer");

        assertEquals(0, dispatch(context, outerActor, "outer"));
        context.notifyCommandFailure("outside any dispatch\n");
        assertEquals(0, dispatch(context, innerActor, "unregistered"));
        assertEquals(0, dispatch(context, outerActor, "unregistered"));

        assertEquals(List.of("outer failure\n"), outerOutput);
        assertEquals(List.of(), innerOutput);
        assertNull(context.currentCommandVerb());
        assertNull(context.currentCommandActor());
    }

    @Test
    void falseResultsContinueAndEmitOneFailureWhileSuccessStopsAndSuppressesFailures() {
        MudlibRuntimeContext context = new MudlibRuntimeContext(null);
        List<String> output = new ArrayList<>();
        List<Object> declinedResults = new ArrayList<>();
        Object actor = connectedActor(context, "actor", output);
        for (Object result : Arrays.asList(null, false, 0, 0L, 0.0d)) {
            register(context, actor, action(ignored -> {
                declinedResults.add(result);
                context.notifyCommandFailure("last declined action\n");
                return result;
            }), "decline");
        }
        register(context, actor, action(ignored -> {
            context.notifyCommandFailure("suppressed failure\n");
            return false;
        }), "accept");
        register(context, actor, action(ignored -> {
            context.write("accepted\n");
            return 7;
        }), "accept");
        register(context, actor, action(ignored -> {
            throw new AssertionError("A successful action must stop dispatch");
        }), "accept");

        assertEquals(0, dispatch(context, actor, "decline"));
        assertEquals(Arrays.asList(null, false, 0, 0L, 0.0d), declinedResults);
        assertEquals(List.of("last declined action\n"), output);
        assertEquals(7, dispatch(context, actor, "accept"));
        assertEquals(List.of("last declined action\n", "accepted\n"), output);
    }

    @Test
    void orderingAndCatchAllArgumentsPreserveEachConfiguredConvention() {
        for (boolean newestFirst : List.of(false, true)) {
            for (boolean argumentsOnly : List.of(false, true)) {
                MudlibRuntimeContext context = new MudlibRuntimeContext(null);
                context.configureMudlib(MudlibConfiguration.builder()
                        .commandActionsNewestFirst(newestFirst)
                        .commandActionsArgumentsOnly(argumentsOnly)
                        .build());
                Object actor = new Object();
                List<String> calls = new ArrayList<>();
                register(context, actor, recording("catch old", context, calls), "", true);
                register(context, actor, recording("prefix old", context, calls), "lo", true);
                register(context, actor, recording("exact old", context, calls), "look");
                register(context, actor, recording("exact new", context, calls), "look");
                register(context, actor, recording("prefix new", context, calls), "lo", true);
                register(context, actor, recording("catch new", context, calls), "", true);
                register(context, actor, recording("not a prefix", context, calls), "l");
                String catchArgument = argumentsOnly ? "shelf" : "look shelf";

                assertEquals(0, dispatch(context, actor, "look shelf"));

                List<String> expected = newestFirst
                        ? List.of("catch new:" + catchArgument, "prefix new:shelf", "exact new:shelf",
                                "exact old:shelf", "prefix old:shelf", "catch old:" + catchArgument)
                        : List.of("exact old:shelf", "exact new:shelf", "catch new:" + catchArgument,
                                "catch old:" + catchArgument, "prefix new:shelf", "prefix old:shelf");
                assertEquals(expected, calls, "newestFirst=" + newestFirst + ", argumentsOnly=" + argumentsOnly);
            }
        }
    }

    @Test
    void aliasesExpandTheVerbOnceBeforeExactAndCatchAllSelection() {
        MudlibRuntimeContext context = new MudlibRuntimeContext(null);
        Object actor = new Object();
        List<String> calls = new ArrayList<>();
        context.configureCommandAliases(Map.of(" n ", " north ", "north", "south"));
        register(context, actor, action(argument -> {
            calls.add(context.currentCommandVerb() + ":" + argument);
            return 0;
        }), "north");
        register(context, actor, action(argument -> {
            calls.add(context.currentCommandVerb() + ":" + argument);
            return 0;
        }), "", true);

        assertEquals(0, dispatch(context, actor, "  n old oak  "));

        assertEquals(List.of("north:old oak", "north:north old oak"), calls);
        context.configureCommandAliases(0);
        calls.clear();
        assertEquals(0, dispatch(context, actor, "n old oak"));
        assertEquals(List.of("n:n old oak"), calls, "Replacing aliases with a non-mapping clears the old aliases");
    }

    @Test
    void registrationRefreshKeepsPersistentActionsAndRestoresScopeAfterExceptions() {
        MudlibRuntimeContext context = new MudlibRuntimeContext(null);
        List<String> calls = new ArrayList<>();
        ActionReceiver actor = action(argument -> {
            calls.add("personal");
            return true;
        });
        ActionReceiver room = action(argument -> {
            calls.add("room");
            return true;
        });
        // A persistent action declared before an actor is installed belongs to its handler itself.
        context.withCurrentObject(actor, () -> {
            context.rememberActionMethod("perform");
            context.registerVerb("personal");
            return null;
        });
        context.withScopedCommandRegistration(() -> {
            register(context, actor, room, "local");
            return null;
        });
        assertEquals(true, dispatch(context, actor, "local"));
        assertThrows(IllegalStateException.class, () -> context.withScopedCommandRegistration(() -> {
            throw new IllegalStateException("registration interrupted");
        }));
        register(context, actor, actor, "after_exception");
        context.clearCommandActions(actor);

        assertEquals(0, dispatch(context, actor, "local"));
        assertEquals(true, dispatch(context, actor, "personal"));
        assertEquals(true, dispatch(context, actor, "after_exception"));
        assertEquals(List.of("room", "personal", "personal"), calls);
        context.withCurrentObject(actor, () -> {
            context.rememberActionMethod("perform");
            context.clearPendingActionMethods();
            context.registerVerb("discarded");
            return null;
        });
        assertEquals(0, dispatch(context, actor, "discarded"));
    }

    @Test
    void changesToRegisteredActionsAffectTheNextDispatchRatherThanTheCurrentSnapshot() {
        MudlibRuntimeContext context = new MudlibRuntimeContext(null);
        Object actor = new Object();
        List<String> calls = new ArrayList<>();
        ActionReceiver removed = action(argument -> {
            calls.add("removed");
            return 0;
        });
        ActionReceiver added = action(argument -> {
            calls.add("added");
            return 0;
        });
        register(context, actor, action(argument -> {
            calls.add("mutating");
            if (context.removeCommandActions(actor, removed) == 1) {
                register(context, actor, added, "change");
            }
            return 0;
        }), "change");
        register(context, actor, removed, "change");

        assertEquals(0, dispatch(context, actor, "change"));
        assertEquals(List.of("mutating", "removed"), calls);
        calls.clear();
        assertEquals(0, dispatch(context, actor, "change"));
        assertEquals(List.of("mutating", "added"), calls);
    }

    @Test
    void destroyingAHandlerRemovesItsActionsFromEveryActorWithoutRemovingOtherHandlers() {
        MudlibRuntimeContext context = new MudlibRuntimeContext(null);
        Object firstActor = new Object();
        Object secondActor = new Object();
        List<String> calls = new ArrayList<>();
        ActionReceiver destroyed = action(argument -> {
            throw new AssertionError("A destroyed handler must not be dispatched");
        });
        ActionReceiver surviving = action(argument -> {
            calls.add("survivor");
            return true;
        });
        context.registerObject("destroyed", destroyed);
        register(context, firstActor, destroyed, "shared");
        register(context, secondActor, destroyed, "shared");
        register(context, firstActor, surviving, "shared");
        register(context, destroyed, surviving, "owned");

        context.destructObject(destroyed);

        assertEquals(true, dispatch(context, firstActor, "shared"));
        assertEquals(0, dispatch(context, secondActor, "shared"));
        assertEquals(0, dispatch(context, destroyed, "owned"), "Destruction also discards the object's actor table");
        assertEquals(List.of("survivor"), calls);
    }

    private static Object connectedActor(MudlibRuntimeContext context, String name, List<String> output) {
        Object actor = new Object();
        context.registerObject(name, actor);
        context.bindSession(name, actor, "127.0.0.1", output::add);
        return actor;
    }

    private static Object dispatch(MudlibRuntimeContext context, Object actor, String command) {
        return context.withCommandActor(actor, () -> context.dispatchCommand(actor, command));
    }

    private static void register(MudlibRuntimeContext context, Object actor, ActionReceiver handler, String verb) {
        register(context, actor, handler, verb, false);
    }

    private static void register(MudlibRuntimeContext context, Object actor, ActionReceiver handler, String verb, boolean prefixMatch) {
        context.withCommandActor(actor, () -> context.withCurrentObject(handler, () -> {
            context.rememberActionMethod("perform");
            context.registerVerb(verb, prefixMatch);
            return null;
        }));
    }

    private static ActionReceiver recording(String name, MudlibRuntimeContext context, List<String> calls) {
        return action(argument -> {
            assertEquals("look", context.currentCommandVerb());
            calls.add(name + ":" + argument);
            return 0;
        });
    }

    private static ActionReceiver action(Function<String, Object> body) {
        return new ActionReceiver(body);
    }

    /** Exposes the same public-method shape as an authored command handler. */
    public static final class ActionReceiver {
        private final Function<String, Object> body;

        ActionReceiver(Function<String, Object> body) {
            this.body = body;
        }

        public Object perform(String argument) {
            return body.apply(argument);
        }
    }
}
