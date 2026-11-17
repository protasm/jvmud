package io.github.protasm.jvmud.engine.instance.lpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.engine.configuration.MudlibConfiguration;
import io.github.protasm.jvmud.engine.configuration.MudlibConfigurationReader;
import io.github.protasm.jvmud.engine.instance.efun.EngineEfuns;
import io.github.protasm.jvmud.language.compiler.parser.ParserOptions;
import io.github.protasm.jvmud.language.compiler.pipeline.CompilationPipeline;
import io.github.protasm.jvmud.language.compiler.scanner.Scanner;
import io.github.protasm.jvmud.language.compiler.token.TokenList;
import io.github.protasm.jvmud.language.compiler.transpiler.UntypedMethodTranspiler;
import io.github.protasm.jvmud.language.execution.LPCExecutionOptions;
import io.github.protasm.jvmud.language.execution.LPCRuntimeException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Exercises opt-in legacy declaration translation through loading and compatibility profiles. */
class UntypedMethodTranspilationIntegrationTest {
    @TempDir Path temp;

    private MudlibRuntime runtime(boolean enabled) {
        var runtime = new MudlibRuntime(LPCExecutionOptions.builder().baseIncludePath(temp).build());
        runtime.configureMudlib(MudlibConfiguration.builder().transpileUntypedMethods(enabled).build());
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

    @Test void transformsIncludedMacrosAndInheritedSources() throws Exception {
        Files.writeString(temp.resolve("methods.h"), "#define METHOD inherited\nMETHOD(arg) { return arg; }\n");
        Files.writeString(temp.resolve("parent.c"), "#include \"methods.h\"\n");
        Files.writeString(temp.resolve("child.c"), "inherit \"parent\"; mixed answer() { return inherited(42); }");
        assertEquals(42, runtime(true).load("child.c").invoke("answer"));
        assertThrows(LPCRuntimeException.class, () -> runtime(false).load("child.c"));
    }

    @Test void transformsGlobalHelpersAndKeepsRuntimeProfilesIsolated() throws Exception {
        Files.writeString(temp.resolve("helpers.c"), "identity(arg) { return arg; }");
        var enabled = runtime(true);
        enabled.configureMudlib(MudlibConfiguration.builder().transpileUntypedMethods(true)
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
        var boundary = MudlibConfigurationReader.read(root, "jvmud/lp245.config");
        assertTrue(boundary.transpileUntypedMethods());
        var runtime = new MudlibRuntime(LPCExecutionOptions.builder().baseIncludePath(root).build());
        EngineEfuns.registerCore(runtime, boundary.engineCapabilities());
        runtime.configureMudlib(boundary);
        runtime.setParserOptions(ParserOptions.features(boundary.languageFeatures()));
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
