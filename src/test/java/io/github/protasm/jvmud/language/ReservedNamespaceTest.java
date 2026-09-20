package io.github.protasm.jvmud.language;

import static org.junit.jupiter.api.Assertions.*;

import io.github.protasm.jvmud.language.efun.builtin.CoreEfuns;
import io.github.protasm.jvmud.language.exec.LPCRuntime;
import io.github.protasm.jvmud.language.exec.LPCRuntimeConfig;
import io.github.protasm.jvmud.language.exec.LPCRuntimeException;
import io.github.protasm.jvmud.language.preproc.Preprocessor;
import io.github.protasm.jvmud.language.preproc.SearchPathIncludeResolver;
import io.github.protasm.jvmud.execution.model.mudlib.MudlibBoundary;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Checks that mudlib declarations cannot claim the engine's reserved namespace. */
final class ReservedNamespaceTest {
    @TempDir Path root;

    @Test
    void rejectsReservedDeclarationsAndMacroRewrites() {
        String[] sources = {
            "int jvmud_future() { return 1; }",
            "int jvmud_future;",
            "int ordinary, jvmud_future;",
            "int run(int jvmud_future) { return 1; }",
            "int run() { int jvmud_future; return 1; }",
            "int run() { int ordinary, jvmud_future; return 1; }",
            "int run() { for (int jvmud_future = 0; jvmud_future < 1; jvmud_future++) {} return 1; }",
            "int run() { foreach (int jvmud_future in ({1})) {} return 1; }",
            "function run() { return function int (int jvmud_future) { return 1; }; }",
            "#define jvmud_future 1\nint run() { return 1; }",
            "#define jvmud_future(x) x\nint run() { return 1; }",
            "#define WRAP(jvmud_future) jvmud_future\nint run() { return 1; }",
            "#undef jvmud_future\nint run() { return 1; }",
            "#define NAME jvmud_future\nint NAME() { return 1; }"
        };
        for (int i = 0; i < sources.length; i++) {
            LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(root).build());
            String source = sources[i];
            String path = "reserved" + i + ".c";
            var error = assertThrows(LPCRuntimeException.class, () -> runtime.loadSource(path, source), source);
            assertTrue(error.getMessage().contains("reserved 'jvmud_' prefix"), error.getMessage());
        }
    }

    @Test
    void allowsNativeCallsStringsCommentsAndUnreservedNames() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(root).build());
        CoreEfuns.registerCore(runtime);
        var object = runtime.loadSource("allowed.c", """
                // jvmud_future is permitted in prose.
                string label = "jvmud_future";
                int mercy_size() { return jvmud_size(({1, 2})); }
                int direct() { return efun::jvmud_size(({1, 2, 3})); }
                """);
        assertEquals(2, object.invoke("mercy_size"));
        assertEquals(3, object.invoke("direct"));
    }

    @Test
    void configurationCannotRepurposeEngineNames() {
        var resolver = new SearchPathIncludeResolver(root, List.of());
        assertThrows(IllegalArgumentException.class,
                () -> new Preprocessor(resolver, Map.of("jvmud_size", "7")));
        assertThrows(IllegalArgumentException.class,
                () -> new Preprocessor(resolver, Map.of(), Map.of("jvmud_size", Map.of("x", "7"))));
        assertThrows(IllegalArgumentException.class,
                () -> MudlibBoundary.builder().engineFunction("jvmud_random", "jvmud_size"));
        assertDoesNotThrow(() -> MudlibBoundary.builder().engineFunction("jvmud_size", "sizeof"));
        assertDoesNotThrow(() -> MudlibBoundary.builder().engineFunction("jvmud_size", "jvmud_size"));
    }

    @Test
    void rejectsReservedNamesFromIncludesAndInheritedObjects() throws Exception {
        Files.writeString(root.resolve("bad.h"), "int jvmud_future;\n");
        Files.writeString(root.resolve("base.c"), "int jvmud_future() { return 1; }\n");
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(root).build());
        var include = assertThrows(LPCRuntimeException.class,
                () -> runtime.loadSource("included.c", "#include \"bad.h\"\nint run() { return 1; }"));
        assertTrue(include.getMessage().contains("reserved 'jvmud_' prefix"), include.getMessage());
        var inherited = assertThrows(LPCRuntimeException.class,
                () -> runtime.loadSource("child.c", "inherit \"/base\"; int run() { return 1; }"));
        assertTrue(inherited.getMessage().contains("reserved 'jvmud_' prefix"), inherited.getMessage());
    }
}
