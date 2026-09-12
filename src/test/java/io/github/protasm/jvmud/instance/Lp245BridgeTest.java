package io.github.protasm.jvmud.instance;

import static org.junit.jupiter.api.Assertions.*;
import io.github.protasm.jvmud.compiler.exec.*;
import io.github.protasm.jvmud.compiler.efun.builtin.CoreEfuns;
import io.github.protasm.jvmud.compiler.parser.ParserOptions;
import io.github.protasm.jvmud.engine.mudlib.MudlibBoundaryConfigReader;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Verifies upstream preservation and real LPC calls through the new immutable-source bridge. */
final class Lp245BridgeTest {
    @TempDir Path temp;
    private static final Path UPSTREAM = Path.of("mudlibs/lp245");

    private LPCRuntime runtime() throws Exception {
        Path bridge = Files.createDirectories(temp.resolve("jvmud"));
        for (String name : List.of("mfuns.c", "mudlib.c", "lp245.config")) {
            Files.copy(UPSTREAM.resolve("jvmud").resolve(name), bridge.resolve(name));
        }
        Path config = bridge.resolve("lp245.config");
        Files.writeString(config, Files.readString(config).replace("transpilation.overrides = transpilation.json", ""));
        var boundary = MudlibBoundaryConfigReader.read(temp, "jvmud/lp245.config");
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(temp).build());
        CoreEfuns.registerCore(runtime, boundary.engineCapabilities());
        runtime.registerMudlibBoundary(boundary);
        runtime.setParserOptions(ParserOptions.features(boundary.languageFeatures()));
        return runtime;
    }

    @Test
    void originalSourcesMatchArchiveBaseline() throws Exception {
        var hashes = new ObjectMapper().readTree(Path.of("src/test/resources/lp245-lysator/upstream-sha256.json").toFile());
        var entries = hashes.fields();
        int count = 0;
        while (entries.hasNext()) {
            var entry = entries.next();
            String name = entry.getKey();
            // Saves and logs may eventually change; original LPC and includes may not.
            if (!name.endsWith(".c") && !name.endsWith(".h")) continue;
            String actual = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(Files.readAllBytes(UPSTREAM.resolve(name))));
            assertEquals(entry.getValue().asText(), actual, name);
            count++;
        }
        assertTrue(count >= 286);
    }

    @Test
    void mappedStringOperationsAndSevenScanfCapturesPreserveValues() throws Exception {
        LPCRuntime runtime = runtime();
        var probe = runtime.loadSource("probe.c", """
                mixed run() {
                  string a, b, c, d;
                  int e, f, g, count;
                  count = sscanf("one two three four 5 6 7", "%s %s %s %s %d %d %d", a, b, c, d, e, f, g);
                  return ({count, a, b, c, d, e, f, g});
                }
                mixed split_empty() { return explode("", ""); }
                mixed split_fields() { return explode(" a b ", " "); }
                mixed split_chars() { return explode("abc", ""); }
                string join_values() { return implode(({"a", 2, "c", ""}), "b"); }
                string join_empty() { return implode(({}), ","); }
                """);
        assertEquals(List.of(7, "one", "two", "three", "four", 5, 6, 7), probe.invoke("run"));
        assertEquals(List.of(""), probe.invoke("split_empty"));
        assertEquals(List.of("", "a", "b", ""), probe.invoke("split_fields"));
        assertEquals(List.of("a", "b", "c"), probe.invoke("split_chars"));
        assertEquals("abcb", probe.invoke("join_values"));
        assertEquals("", probe.invoke("join_empty"));
    }

    @Test
    void mappedWriteFileAppendsAndReportsFailureInDisposableStorage() throws Exception {
        LPCRuntime runtime = runtime();
        var probe = runtime.loadSource("writer.c", """
                int append(string path, string text) { return write_file(path, text); }
                """);
        assertEquals(1, probe.invoke("append", "notes.txt", "first"));
        assertEquals(1, probe.invoke("append", "notes.txt", "second"));
        assertEquals("firstsecond", Files.readString(temp.resolve("notes.txt")));
        assertEquals(0, probe.invoke("append", "..", "outside"));
    }

    @Test
    void traversalMatchesAncestorAndBreadthFirstDepthContracts() throws Exception {
        LPCRuntime runtime = runtime();
        String source = """
                mixed parents() { return all_environment(); }
                int no_parent() { return !all_environment(); }
                mixed descendants() { return deep_inventory(); }
                mixed level(int depth) { return deep_inventory(this_object(), depth); }
                """;
        var room = runtime.loadSource("room.c", source);
        var box = runtime.loadSource("box.c", source);
        var bag = runtime.loadSource("bag.c", source);
        var coin = runtime.loadSource("coin.c", source);
        runtime.moveObject(box.instance(), room.instance());
        runtime.moveObject(bag.instance(), room.instance());
        runtime.moveObject(coin.instance(), box.instance());
        assertEquals(List.of(box.instance(), room.instance()), coin.invoke("parents"));
        assertNull(room.invoke("parents"));
        assertEquals(1, room.invoke("no_parent"));
        assertEquals(List.of(box.instance(), bag.instance(), coin.instance()), room.invoke("descendants"));
        assertEquals(List.of(box.instance(), bag.instance()), room.invoke("level", 1));
        assertEquals(List.of(coin.instance()), room.invoke("level", -2));
        assertEquals(List.of(), room.invoke("level", -3));
        assertEquals(List.of(), coin.invoke("descendants"));
    }
}
