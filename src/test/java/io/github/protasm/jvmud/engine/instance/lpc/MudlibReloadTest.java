package io.github.protasm.jvmud.engine.instance.lpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.protasm.jvmud.engine.configuration.MudlibConfiguration;
import io.github.protasm.jvmud.engine.instance.efun.EngineEfuns;
import io.github.protasm.jvmud.engine.model.mudlib.MudlibLifecycleEvent;
import io.github.protasm.jvmud.engine.model.time.WorldScheduler;
import io.github.protasm.jvmud.language.execution.LPCExecutionOptions;
import io.github.protasm.jvmud.language.execution.LPCObjectHandle;
import io.github.protasm.jvmud.language.execution.LPCRuntimeException;
import io.github.protasm.jvmud.language.runtime.RuntimeCallable;
import io.github.protasm.jvmud.language.runtime.RuntimeContextHolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Documents which parts of a live LPC object survive a compiled-behavior replacement. */
final class MudlibReloadTest {
    @TempDir
    Path directory;

    @Test
    void reloadPreservesIdentityReferencesAndStateWhileNewClonesUseTheNewRevision() throws Exception {
        MudlibRuntime runtime = runtime();
        write("counter", counterSource(3, 10, "first"));
        LPCObjectHandle shared = runtime.load("counter");
        Object oldClone = runtime.cloneObject("counter");
        LPCObjectHandle holder = runtime.loadSource("holder.c", """
                object target;
                void remember(object value) { target = value; }
                object remembered() { return target; }
                mixed read() { return jvmud_invoke_lpc_object(target, "version"); }
                """);
        holder.invoke("remember", shared.instance());
        shared.invoke("set_value", 42);
        assertEquals(13, runtime.invokeObject(oldClone, "current"));

        write("counter", counterSource(100, 1000, "second"));
        LPCObjectHandle reloaded = runtime.reload("counter");

        assertSame(shared.instance(), reloaded.instance());
        assertSame(shared.instance(), runtime.loadOrGetObject("counter"));
        assertSame(shared.instance(), holder.invoke("remembered"));
        assertEquals("second", holder.invoke("read"));
        assertEquals("second", shared.invoke("version"));
        assertEquals(42, shared.invoke("current"), "Neither generated initialization nor create reruns");
        assertEquals("first", runtime.invokeObject(oldClone, "version"));
        assertEquals(13, runtime.invokeObject(oldClone, "current"));

        Object newClone = runtime.cloneObject("counter");
        assertEquals("second", runtime.invokeObject(newClone, "version"));
        assertEquals(1100, runtime.invokeObject(newClone, "current"));
    }

    @Test
    void syntaxAndFieldLayoutFailuresLeaveTheWorkingRevisionIntact() throws Exception {
        MudlibRuntime runtime = runtime();
        String original = counterSource(3, 10, "working");
        write("counter", original);
        LPCObjectHandle shared = runtime.load("counter");
        shared.invoke("set_value", 42);
        List<String> rejectedSources = List.of(
                "void broken( {",
                "int extra;\n" + original,
                "string version() { return \"removed field\"; }",
                "string value; string version() { return \"changed type\"; }");

        for (String source : rejectedSources) {
            write("counter", source);

            assertThrows(LPCRuntimeException.class, () -> runtime.reload("counter"));

            assertSame(shared.instance(), runtime.loadOrGetObject("counter"));
            assertEquals("working", shared.invoke("version"));
            assertEquals(42, shared.invoke("current"));
            assertEquals("working", runtime.invokeObject(runtime.cloneObject("counter"), "version"));
        }
    }

    @Test
    void childLoadingAndReloadingLinkTheParentRevisionUsedDuringCompilation() throws Exception {
        MudlibRuntime runtime = runtime();
        write("parent", "int inherited_value() { return 1; }");
        write("old_child", "inherit \"parent\"; int read() { return inherited_value(); }");
        LPCObjectHandle parent = runtime.load("parent");
        LPCObjectHandle child = runtime.load("old_child");
        assertEquals(1, child.invoke("read"));

        write("parent", "int inherited_value() { return 2; } int added_method() { return 7; }");
        String changedChild = "inherit \"parent\"; int read() { return added_method(); }";
        write("new_child", changedChild);
        write("old_child", changedChild);

        LPCObjectHandle loaded = runtime.load("new_child");
        LPCObjectHandle reloaded = runtime.reload("old_child");

        assertEquals(7, loaded.invoke("read"));
        assertEquals(7, reloaded.invoke("read"));
        assertSame(child.instance(), reloaded.instance());
        assertEquals(1, parent.invoke("inherited_value"), "Loading a dependency does not reload its live object");
    }

    @Test
    void pendingNamedCallbackKeepsItsTargetAndRunsTheReplacementMethod() throws Exception {
        MudlibRuntime runtime = runtime();
        WorldScheduler scheduler = new WorldScheduler();
        runtime.setScheduler(scheduler);
        String source = """
                int value;
                void start() { jvmud_schedule_deferred_callback("finish", 2); }
                void finish() { value += 1; }
                int current() { return value; }
                """;
        write("timer", source);
        LPCObjectHandle timer = runtime.load("timer");
        timer.invoke("start");
        write("timer", source.replace("value += 1", "value += 7"));

        LPCObjectHandle reloaded = runtime.reload("timer");
        scheduler.advanceTo(2);

        assertSame(timer.instance(), reloaded.instance());
        assertEquals(7, timer.invoke("current"));
    }

    @Test
    void existingInlineCallableKeepsItsCodeAndSharesTheObjectsLiveFields() throws Exception {
        MudlibRuntime runtime = runtime();
        String source = """
                int value = 10;
                function incrementer() { return (: value += 1 :); }
                int current() { return value; }
                """;
        write("closures", source);
        LPCObjectHandle object = runtime.load("closures");
        RuntimeCallable oldCallback = (RuntimeCallable) object.invoke("incrementer");
        write("closures", source.replace("value += 1", "value += 100"));
        runtime.reload("closures");
        RuntimeCallable newCallback = (RuntimeCallable) object.invoke("incrementer");

        assertEquals(11, invoke(runtime, oldCallback));
        assertEquals(111, invoke(runtime, newCallback));
        assertEquals(112, invoke(runtime, oldCallback));
        assertEquals(112, object.invoke("current"));
    }

    private MudlibRuntime runtime() {
        MudlibRuntime runtime = new MudlibRuntime(LPCExecutionOptions.builder().baseIncludePath(directory).build());
        EngineEfuns.registerCore(runtime);
        runtime.configureMudlib(MudlibConfiguration.builder()
                .lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED, "create")
                .build());
        return runtime;
    }

    private void write(String name, String source) throws IOException {
        Files.writeString(directory.resolve(name + ".c"), source);
    }

    private static Object invoke(MudlibRuntime runtime, RuntimeCallable callable) {
        return runtime.withRuntimeContext(() -> callable.call(RuntimeContextHolder.requireCurrent()));
    }

    private static String counterSource(int initialValue, int createdValue, String version) {
        return """
                int value = %d;
                void create() { value += %d; }
                void set_value(int next) { value = next; }
                int current() { return value; }
                string version() { return "%s"; }
                """.formatted(initialValue, createdValue, version);
    }
}
