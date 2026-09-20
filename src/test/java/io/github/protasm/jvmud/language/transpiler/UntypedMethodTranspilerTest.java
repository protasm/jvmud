package io.github.protasm.jvmud.language.transpiler;

import static org.junit.jupiter.api.Assertions.*;

import io.github.protasm.jvmud.language.exec.*;
import io.github.protasm.jvmud.language.pipeline.CompilationPipeline;
import io.github.protasm.jvmud.language.scanner.Scanner;
import io.github.protasm.jvmud.language.token.*;
import io.github.protasm.jvmud.execution.model.mudlib.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UntypedMethodTranspilerTest {
    @TempDir Path temp;

    private LPCRuntime runtime(boolean enabled) {
        var runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(temp).build());
        runtime.registerMudlibBoundary(MudlibBoundary.builder().transpileUntypedMethods(enabled).build());
        return runtime;
    }

    @Test void optInExecutesLegacyMethodsAndDefaultRemainsStrict() {
        String source = "identity(value) { return value; } mixed answer() { return identity(42); }";
        assertFalse(new CompilationPipeline("java/lang/Object").run(source).succeeded());
        assertThrows(LPCRuntimeException.class, () -> runtime(false).loadSource("strict.c", source));
        assertEquals(42, runtime(true).loadSource("legacy.c", source).invoke("answer"));
        assertFalse(new CompilationPipeline("java/lang/Object").run("mixed f(arg) { return arg; }").succeeded());
        assertFalse(new CompilationPipeline("java/lang/Object").run("f(int arg) { return arg; }").succeeded());
    }

    @Test void preservesTypedCodeBodiesAndOriginalLocations() {
        String source = """
                // fake(arg) { }
                string label = "fake(arg) { }";
                static identity(arg) { return arg; }
                int typed(int value) { return value; }
                """;
        TokenList original = new Scanner().scan(source);
        TokenList transformed = new UntypedMethodTranspiler().transpile(original);
        int added = 0, index = 0;
        for (int i = 0; i < transformed.size(); i++) {
            Token<?> token = transformed.get(i);
            if (token == original.get(index)) index++;
            else {
                assertEquals("mixed", token.lexeme());
                assertEquals(original.get(index).span(), token.span());
                assertEquals(3, token.line());
                added++;
            }
        }
        assertEquals(original.size(), index);
        assertEquals(2, added);
        assertEquals(transformed.toString(), new UntypedMethodTranspiler().transpile(transformed).toString());
    }

    @Test void transformsIncludedMacrosAndInheritedSources() throws Exception {
        Files.writeString(temp.resolve("methods.h"), "#define METHOD inherited\nMETHOD(arg) { return arg; }\n");
        Files.writeString(temp.resolve("parent.c"), "#include \"methods.h\"\n");
        Files.writeString(temp.resolve("child.c"), "inherit \"parent\"; mixed answer() { return inherited(42); }");
        assertEquals(42, runtime(true).load("child.c").invoke("answer"));
        assertThrows(LPCRuntimeException.class, () -> runtime(false).load("child.c"));
    }

    @Test void honorsManifestDefaultsAndRejectsInvalidSwitch() throws Exception {
        Path config = temp.resolve("bridge.config");
        Files.writeString(config, "transpilation.untyped_methods = true\n");
        assertTrue(MudlibBoundaryConfigReader.read(temp, "bridge.config").transpileUntypedMethods());
        Files.writeString(config, "transpilation.untyped_methods = false\n");
        assertFalse(MudlibBoundaryConfigReader.read(temp, "bridge.config").transpileUntypedMethods());
        Files.writeString(config, "");
        assertFalse(MudlibBoundaryConfigReader.read(temp, "bridge.config").transpileUntypedMethods());
        Files.writeString(config, "transpilation.untyped_methods = perhaps\n");
        assertThrows(IllegalArgumentException.class, () -> MudlibBoundaryConfigReader.read(temp, "bridge.config"));
    }

    @Test void transformsGlobalHelpersAndKeepsRuntimeProfilesIsolated() throws Exception {
        Files.writeString(temp.resolve("helpers.c"), "identity(arg) { return arg; }");
        var enabled = runtime(true);
        enabled.registerMudlibBoundary(MudlibBoundary.builder().transpileUntypedMethods(true)
                .mfunObjectPath("helpers").build());
        assertEquals(42, enabled.loadSource("user.c", "mixed answer() { return identity(42); }").invoke("answer"));
        assertThrows(LPCRuntimeException.class, () -> runtime(false).loadSource("other.c", "answer() { return 42; }"));
    }

    @Test void handlesPrototypesArraysAndMixedTypedSignatures() {
        var object = runtime(true).loadSource("prototypes.c", """
                forward(arg);
                mixed forward(arg) { return arg; }
                mixed *items(mixed *values) { return values; }
                mixed answer() { return forward(items(({42}))[0]); }
                """);
        assertEquals(42, object.invoke("answer"));
    }

    @Test void doesNotRewriteFieldInitializersOrInactiveDeclarations() {
        var object = runtime(true).loadSource("initializers.c", """
                mixed values = ({ "fake(arg)", 42 });
                #if 0
                invalid(unclosed
                #endif
                answer() { return values[1]; }
                """);
        assertEquals(42, object.invoke("answer"));
        TokenList tokens = new Scanner().scan("int malformed(int) { return 42; }");
        assertEquals(tokens.toString(), new UntypedMethodTranspiler().transpile(tokens).toString());
    }

    @Test void originalLysatorChestCompilesWithoutChangingAnyArchiveFile() throws Exception {
        Path root = Path.of("mudlibs/lp245").toAbsolutePath();
        var boundary = MudlibBoundaryConfigReader.read(root, "jvmud/lp245.config");
        assertTrue(boundary.transpileUntypedMethods());
        var runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(root).build());
        io.github.protasm.jvmud.language.efun.builtin.CoreEfuns.registerCore(runtime, boundary.engineCapabilities());
        runtime.registerMudlibBoundary(boundary);
        runtime.setParserOptions(io.github.protasm.jvmud.language.parser.ParserOptions.features(boundary.languageFeatures()));
        var result = runtime.compile(root.resolve("obj/chest.c"));
        assertTrue(result.succeeded(), () -> result.getProblems().toString());
        var hashes = new com.fasterxml.jackson.databind.ObjectMapper().readTree(
                Path.of("src/test/resources/lp245-lysator/upstream-sha256.json").toFile());
        assertEquals(582, hashes.size());
        var entries = hashes.fields();
        while (entries.hasNext()) {
            var entry = entries.next();
            byte[] bytes = Files.readAllBytes(root.resolve(entry.getKey()));
            assertEquals(entry.getValue().asText(), java.util.HexFormat.of().formatHex(
                    java.security.MessageDigest.getInstance("SHA-256").digest(bytes)), entry.getKey());
        }
    }

    @Test void preservesExplicitTypeErrorsEvenWhenEnabled() {
        assertThrows(LPCRuntimeException.class, () -> runtime(true).loadSource("bad.c", "int bad() { return \"text\"; }"));
    }
}
