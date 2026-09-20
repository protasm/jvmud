package io.github.protasm.jvmud.compiler.transpiler;

import static org.junit.jupiter.api.Assertions.*;

import io.github.protasm.jvmud.compiler.efun.builtin.CoreEfuns;
import io.github.protasm.jvmud.compiler.exec.*;
import io.github.protasm.jvmud.engine.mudlib.*;
import io.github.protasm.jvmud.instance.MudInstance;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Verifies explicit opt-in, subclass dispatch and preservation of ordinary call checking. */
class ImplicitSelfCallTranspilerTest {
    @TempDir Path root;

    private LPCRuntime runtime(boolean enabled) {
        var rt = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(root).build());
        CoreEfuns.registerCore(rt);
        rt.registerMudlibBoundary(MudlibBoundary.builder().mudlibRootPath(root)
                .transpileImplicitSelfCalls(enabled).build());
        return rt;
    }

    @Test void dispatchesInheritedHookWithArgumentsOnceAndRequiresImplementation() throws Exception {
        Files.writeString(root.resolve("base.c"), """
                int count;
                int next() { count += 1; return count; }
                mixed run() { return describe(next()); }
                """);
        Files.writeString(root.resolve("child.c"), """
                inherit "base";
                string describe(int n) { return "child:" + n; }
                """);
        Files.writeString(root.resolve("grandchild.c"), """
                inherit "child";
                string describe(int n) { return "grandchild:" + n; }
                """);
        var rt = runtime(true);
        var base = rt.load("base");
        var child = rt.load("child");
        assertEquals("child:1", child.invoke("run"));
        assertEquals("child:2", child.invoke("run"));
        assertEquals("grandchild:1", rt.load("grandchild").invoke("run"));
        var error = assertThrows(RuntimeException.class, () -> base.invoke("run"));
        Throwable cause = error;
        while (cause.getCause() != null) cause = cause.getCause();
        assertInstanceOf(NoSuchMethodException.class, cause);
        assertTrue(cause.getMessage().contains("describe"), cause.toString());
        // Explicit arrow calls retain their existing optional behavior.
        assertEquals(0, rt.loadSource("optional.c",
                "mixed run() { return jvmud_current_lpc_object()->absent(); }").invoke("run"));
    }

    @Test void defaultAndFalseRemainStrictAndFlagDoesNotEnableUntypedMethods() {
        for (var rt : new LPCRuntime[] {new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(root).build()), runtime(false)}) {
            assertThrows(LPCRuntimeException.class, () -> rt.loadSource("strict.c", "mixed run() { return absent(); }"));
        }
        assertThrows(LPCRuntimeException.class, () -> runtime(true).loadSource("untyped.c", "run() { return 1; }"));
    }

    @Test void knownMethodsFunctionsAliasesAndQualifiedCallsKeepChecks() throws Exception {
        Files.writeString(root.resolve("helper_base.c"), "int helper() { return 8; }");
        Files.writeString(root.resolve("helpers.c"), "inherit \"helper_base\";");
        Files.writeString(root.resolve("profile.config"), """
                transpilation.implicit_self_calls = true
                mfun_object = helpers
                engine_function.jvmud_time = clock
                engine_function.unavailable_native = configured_alias
                """);
        var rt = runtime(true);
        rt.registerMudlibBoundary(MudlibBoundaryConfigReader.read(root, "profile.config"));
        assertEquals(8, rt.loadSource("known.c", "int run() { return helper(); }").invoke("run"));
        for (String source : new String[] {
                "int known() { return 1; } mixed run() { return known(1); }",
                "mixed run() { return helper(1); }",
                "mixed run() { return clock(1); }",
                "mixed run() { return configured_alias(); }",
                "mixed run() { return jvmud_time(1); }",
                "mixed run() { return efun::absent(); }",
                "mixed run() { return ::absent(); }"}) {
            assertThrows(LPCRuntimeException.class, () -> rt.loadSource("bad.c", source), source);
        }
    }

    @Test void hostedBootPreservesIndependentFlagBeforeAndAfterBridgeMerge() throws Exception {
        Files.writeString(root.resolve("base.c"), "mixed answer() { return hook(); }");
        Files.writeString(root.resolve("start.c"), "inherit \"base\"; int hook() { return 42; }");
        Files.writeString(root.resolve("bridge.c"), "inherit \"base\"; string hook() { return \"ready\"; }");
        Path config = root.resolve("test.config");
        String body = "mudlib_object = bridge\ninitial_place = start\n";
        for (String flag : new String[] {"", "transpilation.implicit_self_calls = false\n"}) {
            Files.writeString(config, flag + body);
            assertThrows(RuntimeException.class, () -> MudInstance.boot(root, "test.config"));
        }
        Files.writeString(config, "transpilation.implicit_self_calls = true\n" + body);
        var mud = MudInstance.boot(root, "test.config");
        assertTrue(mud.bootResult().mudlibBoundary().transpileImplicitSelfCalls());
        assertFalse(mud.bootResult().mudlibBoundary().transpileUntypedMethods());
        assertEquals(42, mud.<Object>administer(rt -> rt.invokeObject(rt.loadOrGetObject("start"), "answer")));
        assertEquals("ready", mud.<Object>administer(rt -> rt.invokeObject(rt.loadOrGetObject("bridge"), "answer")));
    }
}
