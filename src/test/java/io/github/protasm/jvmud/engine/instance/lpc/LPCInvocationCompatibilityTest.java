package io.github.protasm.jvmud.engine.instance.lpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.protasm.jvmud.engine.configuration.MudlibConfiguration;
import io.github.protasm.jvmud.engine.instance.efun.EngineEfuns;
import io.github.protasm.jvmud.language.execution.LPCExecutionOptions;
import io.github.protasm.jvmud.language.execution.LPCObjectHandle;
import io.github.protasm.jvmud.language.execution.LPCRuntimeException;
import io.github.protasm.jvmud.language.runtime.RuntimeCallable;
import io.github.protasm.jvmud.language.runtime.RuntimeContext;
import io.github.protasm.jvmud.language.runtime.RuntimeContextHolder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Preserves the invocation policies shared by authored objects, commands, globals and reloads. */
final class LPCInvocationCompatibilityTest {
    @TempDir
    Path directory;

    @Test
    void nestedCallsAndCommandsRestoreObjectActorAndVerbAfterSuccessAndFailure() {
        MudlibRuntimeContext context = new MudlibRuntimeContext(null);
        Object surroundingObject = new Object();
        Object outerActor = new Object();
        Object innerActor = new Object();
        CommandReceiver outer = new CommandReceiver();
        CommandReceiver inner = new CommandReceiver();
        FailingReceiver failure = new FailingReceiver();
        context.registerObject("outer", outer);
        context.registerObject("inner", inner);
        context.registerObject("failure", failure);
        registerAction(context, outerActor, outer, "outer");
        registerAction(context, innerActor, inner, "inner");
        inner.action = argument -> {
            assertScope(context, inner, outer, innerActor, "inner");
            assertEquals("nested", argument);
            return 1;
        };
        outer.action = argument -> {
            assertScope(context, outer, surroundingObject, outerActor, "outer");
            assertEquals("initial", argument);
            assertEquals(1, context.withCommandActor(innerActor,
                    () -> context.dispatchCommand(innerActor, "inner nested")));
            assertScope(context, outer, surroundingObject, outerActor, "outer");
            assertThrows(IllegalArgumentException.class, () -> context.invokeObject(failure, "explode", 7));
            assertScope(context, outer, surroundingObject, outerActor, "outer");
            inner.action = ignored -> {
                assertScope(context, inner, outer, innerActor, "inner");
                throw new IllegalStateException("nested command failure");
            };
            IllegalArgumentException nestedFailure = assertThrows(IllegalArgumentException.class, () -> context.withCommandActor(innerActor,
                    () -> context.dispatchCommand(innerActor, "inner failing")));
            assertEquals("Failed to call generated LPC method: nested command failure", nestedFailure.getMessage());
            assertScope(context, outer, surroundingObject, outerActor, "outer");
            return 1;
        };

        assertEquals(1, context.withCurrentObject(surroundingObject, () -> {
            // At the outermost scope, previous_object retains the current object fallback.
            assertSame(surroundingObject, context.previousObject());
            return context.withCommandActor(outerActor, () -> context.dispatchCommand(outerActor, "outer initial"));
        }));

        assertScope(context, null, null, null, null);
    }

    @Test
    void requiredCallsPadMissingArgumentsAndOptionalCallsAlsoTrimSurplusArguments() {
        MudlibRuntimeContext context = new MudlibRuntimeContext(null);
        ArgumentReceiver receiver = new ArgumentReceiver();
        Object identity = new Object();

        assertEquals(Arrays.asList(7, null, false, null, null), context.invokeObject(receiver, "capture", 7L));
        assertEquals(Arrays.asList(7, null, false, null, null), context.invokeOptionalObject(receiver, "capture", 7L));
        List<Object> expected = Arrays.asList(7, null, true, identity, List.of(2));
        assertEquals(expected, context.invokeObject(receiver, "capture", 7L, 0, 1, identity, List.of(2)));
        assertEquals(expected, context.invokeOptionalObject(receiver, "capture", 7L, 0, 1, identity, List.of(2), "surplus"));
        assertThrows(IllegalArgumentException.class,
                () -> context.invokeObject(receiver, "capture", 7L, 0, 1, identity, List.of(2), "surplus"));
        assertEquals(0, context.invokeOptionalObject(receiver, "absent"));
        assertThrows(IllegalArgumentException.class, () -> context.invokeObject(receiver, "absent"));
        assertScope(context, null, null, null, null);
    }

    @Test
    void lifecycleHooksPreferTheMostDerivedDeclarationOverAnInheritedExactArity() {
        MudlibRuntimeContext context = new MudlibRuntimeContext(null);
        ChildLifecycleReceiver receiver = new ChildLifecycleReceiver();

        assertEquals("parent:7", context.invokeObject(receiver, "onEvent", 7));
        assertEquals("parent:7", context.invokeOptionalObject(receiver, "onEvent", 7));
        assertEquals("child:7:null", context.invokeLifecycleObject(receiver, "onEvent", 7));
        assertEquals("child:7:arrival", context.invokeLifecycleObject(receiver, "onEvent", 7, "arrival", "surplus"));
        assertEquals(0, context.invokeLifecycleObject(receiver, "absent"));
        assertScope(context, null, null, null, null);
    }

    @Test
    void invocationModesRetainTheirFailureDiagnosticsAndOriginalCause() {
        MudlibRuntimeContext context = new MudlibRuntimeContext(null);
        FailingReceiver receiver = new FailingReceiver();
        context.registerObject("failure", receiver);

        IllegalArgumentException required = assertThrows(IllegalArgumentException.class,
                () -> context.invokeObject(receiver, "explode", 7));
        IllegalArgumentException optional = assertThrows(IllegalArgumentException.class,
                () -> context.invokeOptionalObject(receiver, "explode", 7));
        IllegalArgumentException lifecycle = assertThrows(IllegalArgumentException.class,
                () -> context.invokeLifecycleObject(receiver, "explode", 7));

        assertEquals("Failed to call generated LPC method: authored failure", required.getMessage());
        String detailed = "Failed to call explode/1 on failure with parameter types [int]"
                + " and argument types [Integer]: authored failure";
        assertEquals(detailed, optional.getMessage());
        assertEquals(detailed, lifecycle.getMessage());
        for (IllegalArgumentException thrown : List.of(required, optional, lifecycle)) {
            InvocationTargetException reflected = assertInstanceOf(InvocationTargetException.class, thrown.getCause());
            assertInstanceOf(IllegalStateException.class, reflected.getTargetException());
            assertEquals("authored failure", reflected.getTargetException().getMessage());
        }
        assertScope(context, null, null, null, null);
    }

    @Test
    void globalCompatibilityFunctionsKeepTheirLexicalCallerAndRestoreItAfterFailure() throws Exception {
        write("globals", """
                mixed lexical_scope() {
                    return ({ jvmud_current_lpc_object(), jvmud_previous_lpc_object(),
                              jvmud_current_agent(), jvmud_current_verb() });
                }
                mixed failing_global() {
                    return jvmud_invoke_lpc_object("failure", "explode", 7);
                }
                """);
        MudlibRuntime runtime = runtime();
        runtime.configureMudlib(MudlibConfiguration.builder().mfunObjectPath("globals").build());
        runtime.registerHostObject("failure", new FailingReceiver());
        LPCObjectHandle caller = runtime.loadSource("caller.c", "mixed scope() { return lexical_scope(); }");
        MudlibRuntimeContext context = context(runtime);
        Object surroundingObject = new Object();
        Object actor = new Object();

        runtime.withCurrentObject(surroundingObject, () -> runtime.withCommandActor(actor, () -> {
            assertEquals(Arrays.asList(caller.instance(), surroundingObject, actor, null), caller.invoke("scope"));
            IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                    () -> context.invokeEfun("failing_global", 0, new Object[0]));
            assertEquals("Failed to call generated LPC method: authored failure", failure.getMessage());
            assertScope(context, surroundingObject, surroundingObject, actor, null);
            return null;
        }));

        assertScope(context, null, null, null, null);
    }

    @Test
    void namedCallsFollowReloadWhileHistoricalInlineCallsShareStateAndRestoreReceiversAfterFailure() throws Exception {
        String source = """
                int value = 10;
                int step() { value += 1; return value; }
                mixed fail_after_step() {
                    value += 1;
                    return jvmud_invoke_lpc_object("failure", "explode", 7);
                }
                function named_step() { return #'step; }
                function inline_step() { return (: step() :); }
                function inline_failure() { return (: fail_after_step() :); }
                int current() { return value; }
                """;
        write("counter", source);
        MudlibRuntime runtime = runtime();
        runtime.registerHostObject("failure", new FailingReceiver());
        LPCObjectHandle counter = runtime.load("counter");
        RuntimeCallable named = (RuntimeCallable) counter.invoke("named_step");
        RuntimeCallable historical = (RuntimeCallable) counter.invoke("inline_step");
        RuntimeCallable historicalFailure = (RuntimeCallable) counter.invoke("inline_failure");
        write("counter", source.replace("value += 1;", "value += 100;"));
        runtime.reload("counter");
        MudlibRuntimeContext context = context(runtime);
        Object surroundingObject = new Object();
        Object actor = new Object();
        RuntimeContext previousRuntime = RuntimeContextHolder.current();

        runtime.withCurrentObject(surroundingObject, () -> runtime.withCommandActor(actor, () -> {
            // The replacement Java receiver must expose the original LPC identity in the stack.
            context.withCurrentObject(context.objectReceiver(counter.instance()), () -> {
                assertSame(counter.instance(), context.currentObject());
                assertSame(surroundingObject, context.previousObject());
                return null;
            });
            assertEquals(110, invoke(runtime, named));
            assertEquals(111, invoke(runtime, historical));
            assertThrows(IllegalArgumentException.class, () -> invoke(runtime, historicalFailure));
            assertScope(context, surroundingObject, surroundingObject, actor, null);
            assertSame(context, RuntimeContextHolder.current());
            assertEquals(112, counter.invoke("current"), "Historical state changes survive a thrown callback");
            assertEquals(212, invoke(runtime, named), "Failure must restore the installed behavior receiver");
            return null;
        }));

        assertSame(previousRuntime, RuntimeContextHolder.current());
        assertScope(context, null, null, null, null);
    }

    @Test
    void reloadRejectsAnActiveAncestorAndSucceedsAfterItsFailingCallUnwinds() throws Exception {
        String source = """
                mixed attempt_reload() { return jvmud_invoke_lpc_object("reload_gate", "perform"); }
                int version() { return 1; }
                """;
        write("active", source);
        MudlibRuntime runtime = runtime();
        runtime.registerHostObject("reload_gate", new CallbackReceiver(() -> runtime.reload("active")));
        LPCObjectHandle active = runtime.load("active");
        write("active", source.replace("return 1;", "return 2;"));

        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class, () -> active.invoke("attempt_reload"));
        Throwable cause = failure;
        while (cause.getCause() != null) cause = cause.getCause();
        assertInstanceOf(LPCRuntimeException.class, cause);
        assertEquals("Cannot reload an LPC object while it is executing: active", cause.getMessage());
        assertEquals(1, active.invoke("version"));
        assertScope(context(runtime), null, null, null, null);

        assertSame(active.instance(), runtime.reload("active").instance());
        assertEquals(2, active.invoke("version"));
    }

    private MudlibRuntime runtime() {
        MudlibRuntime runtime = new MudlibRuntime(LPCExecutionOptions.builder().baseIncludePath(directory).build());
        EngineEfuns.registerCore(runtime);
        return runtime;
    }

    private void write(String name, String source) throws IOException {
        Files.writeString(directory.resolve(name + ".c"), source);
    }

    private static MudlibRuntimeContext context(MudlibRuntime runtime) {
        return runtime.withRuntimeContext(() -> (MudlibRuntimeContext) RuntimeContextHolder.requireCurrent());
    }

    private static Object invoke(MudlibRuntime runtime, RuntimeCallable callable) {
        return runtime.withRuntimeContext(() -> callable.call(RuntimeContextHolder.requireCurrent()));
    }

    private static void registerAction(MudlibRuntimeContext context, Object actor, Object handler, String verb) {
        context.withCommandActor(actor, () -> context.withCurrentObject(handler, () -> {
            context.rememberActionMethod("perform");
            context.registerVerb(verb);
            return null;
        }));
    }

    private static void assertScope(MudlibRuntimeContext context, Object current, Object previous, Object actor, String verb) {
        assertSame(current, context.currentObject());
        assertSame(previous, context.previousObject());
        assertSame(actor, context.currentCommandActor());
        assertEquals(verb, context.currentCommandVerb());
    }

    /** Public methods stand in for generated methods while assertions inspect their active scope. */
    public static final class CommandReceiver {
        private Function<String, Object> action;

        public Object perform(String argument) {
            return action.apply(argument);
        }
    }

    /** Makes argument conversion and omitted LPC zero values directly observable. */
    public static final class ArgumentReceiver {
        public List<Object> capture(int count, String label, boolean enabled, Object target, List<?> values) {
            return Arrays.asList(count, label, enabled, target, values);
        }
    }

    /** An inherited exact-arity method remains eligible for ordinary calls. */
    public static class ParentLifecycleReceiver {
        public String onEvent(int flags) {
            return "parent:" + flags;
        }
    }

    /** The descendant hook wins for lifecycle dispatch even when its arity is less convenient. */
    public static final class ChildLifecycleReceiver extends ParentLifecycleReceiver {
        public String onEvent(int flags, String message) {
            return "child:" + flags + ":" + message;
        }
    }

    /** Supplies a deterministic authored failure for every reflective invocation mode. */
    public static final class FailingReceiver {
        public Object explode(int ignored) {
            throw new IllegalStateException("authored failure");
        }
    }

    /** Re-enters the public runtime API while its authored caller remains on the invocation stack. */
    public static final class CallbackReceiver {
        private final Supplier<Object> action;

        CallbackReceiver(Supplier<Object> action) {
            this.action = action;
        }

        public Object perform() {
            return action.get();
        }
    }
}
