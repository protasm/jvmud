package io.github.protasm.jvmud.instance;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Exercises manifest transpilation through actual hosted startup and bridge declaration merging. */
class MudlibBootTranspilationTest {
    @TempDir Path root;

    private void fixture(String setting, boolean legacyBridge) throws Exception {
        Files.createDirectories(root.resolve("jvmud"));
        Files.writeString(root.resolve("jvmud/test.config"), setting + """
                mudlib_root = ..
                mudlib_object = jvmud/mudlib
                initial_place = start
                preload_objects = preload
                engine_function.jvmud_uppercase_text = uppercase
                """);
        Files.writeString(root.resolve("jvmud/mudlib.c"),
                (legacyBridge ? "" : "string ") + "player_prompt() { return uppercase(\"ready> \"); }");
        Files.writeString(root.resolve("start.c"), "answer(arg) { return arg + 1; }");
        Files.writeString(root.resolve("preload.c"), "value() { return 7; }");
    }

    @Test void enabledSettingAppliesBeforeBridgeLoadAndSurvivesDeclarationMerge() throws Exception {
        fixture("transpilation.untyped_methods = true\n", true);
        MudInstance mud = MudInstance.boot(root, "jvmud/test.config");
        assertTrue(mud.bootResult().mudlibBoundary().transpileUntypedMethods());
        assertEquals("READY> ", mud.bootResult().mudlibBoundary().playerPrompt().orElseThrow());
        assertTrue(mud.bootResult().skippedPreloads().isEmpty());
        assertEquals(42, mud.<Object>administer(runtime -> runtime.invokeObject(runtime.loadOrGetObject("start"), "answer", 41)));
        assertEquals(7, mud.<Object>administer(runtime -> runtime.invokeObject(runtime.loadOrGetObject("preload"), "value")));
    }

    @Test void typedBridgeDoesNotLoseOptInForLaterObjects() throws Exception {
        fixture("transpilation.untyped_methods = true\n", false);
        MudInstance mud = MudInstance.boot(root, "jvmud/test.config");
        assertTrue(mud.bootResult().mudlibBoundary().transpileUntypedMethods());
        assertTrue(mud.bootResult().skippedPreloads().isEmpty());
    }

    @Test void dynamicTypesApplyToBridgeAndSurviveHostedBoundaryMerge() throws Exception {
        fixture("compiler.dynamic_types = true\n", false);
        Files.writeString(root.resolve("jvmud/mudlib.c"), "int player_prompt() { return \"dynamic> \"; }");
        Files.writeString(root.resolve("start.c"), "int answer(string arg) { return arg; }");
        Files.writeString(root.resolve("preload.c"), "int value() { return \"preloaded\"; }");
        MudInstance mud = MudInstance.boot(root, "jvmud/test.config");
        assertTrue(mud.bootResult().mudlibBoundary().dynamicTypes());
        assertEquals("dynamic> ", mud.bootResult().mudlibBoundary().playerPrompt().orElseThrow());
        assertTrue(mud.bootResult().skippedPreloads().isEmpty());
        assertEquals("unchanged", mud.<Object>administer(runtime -> runtime.invokeObject(runtime.loadOrGetObject("start"), "answer", "unchanged")));
        assertEquals("preloaded", mud.<Object>administer(runtime -> runtime.invokeObject(runtime.loadOrGetObject("preload"), "value")));
    }

    @Test void absentAndDisabledSettingsRejectLegacyObjectsAfterTypedBridge() throws Exception {
        for (String setting : new String[] {"", "transpilation.untyped_methods = false\n"}) {
            fixture(setting, false);
            var error = assertThrows(IllegalStateException.class, () -> MudInstance.boot(root, "jvmud/test.config"));
            assertTrue(error.getMessage().contains("initial place"), error.toString());
            assertTrue(error.getCause().getMessage().contains("explicit return type"), error.toString());
        }
    }

    @Test void disabledSettingRejectsLegacyBridgeItself() throws Exception {
        fixture("transpilation.untyped_methods = false\n", true);
        assertThrows(IllegalStateException.class, () -> MudInstance.boot(root, "jvmud/test.config"));
    }
}
