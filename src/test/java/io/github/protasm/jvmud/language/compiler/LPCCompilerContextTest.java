package io.github.protasm.jvmud.language.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.language.compiler.parser.ast.Symbol;
import io.github.protasm.jvmud.language.compiler.pipeline.CompilationPipeline;
import io.github.protasm.jvmud.language.compiler.pipeline.CompilationResult;
import io.github.protasm.jvmud.language.compiler.preprocessor.SearchPathIncludeResolver;
import io.github.protasm.jvmud.language.compiler.type.LPCType;
import io.github.protasm.jvmud.language.efun.EfunSignature;

import java.lang.reflect.ClassFileFormatVersion;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassReader;

/** Examples of compiling with source policy and declarations, without creating a game instance. */
class LPCCompilerContextTest {
    @TempDir Path sourceRoot;

    @Test
    void standalonePipelineProducesAllCompilerRepresentations() {
        CompilationResult result = new CompilationPipeline("java/lang/Object")
                .run("int answer() { return 42; }");

        assertTrue(result.succeeded(), () -> result.getProblems().toString());
        assertNotNull(result.getTokens());
        assertNotNull(result.getAstObject());
        assertNotNull(result.getSemanticModel());
        assertNotNull(result.getTypedIr());
        assertTrue(result.getBytecode().length > 0);
        assertEquals(ClassFileFormatVersion.RELEASE_25,
                ClassFileFormatVersion.fromMajor(new ClassReader(result.getBytecode()).readUnsignedShort(6)));
    }

    @Test
    void callableDeclarationsDoNotRequireRuntimeImplementations() {
        LPCCompilerContext environment = new LPCCompilerContext();
        EfunSignature measure = new EfunSignature(
                new Symbol(LPCType.LPCINT, "jvmud_measure"), List.of(LPCType.LPCSTRING));
        environment.registerSignature(measure);

        CompilationResult result = new CompilationPipeline("java/lang/Object", environment)
                .run("int width() { return jvmud_measure(\"hello\"); }");

        assertTrue(result.succeeded(), () -> result.getProblems().toString());
        assertEquals(measure, environment.resolveFunctionSignature("jvmud_measure", 1));
        assertNull(environment.efunRegistry().lookup("jvmud_measure", 1));
    }

    @Test
    void globalDeclarationsAreReadWithoutExecutingObjectInitialization() throws Exception {
        Path globals = sourceRoot.resolve("globals.c");
        Files.writeString(globals, """
                int marker;
                void create() { marker = 17 / 0; }
                int measure(string value) { return 4; }
                """);
        LPCCompilerContext environment = new LPCCompilerContext(
                new SearchPathIncludeResolver(sourceRoot, List.of()));
        environment.setGlobalFunctions(new LPCGlobalFunctions(
                "globals", globals, null, null, Set.of(), Map.of()));

        CompilationResult result = new CompilationPipeline("java/lang/Object", environment)
                .run("int width() { return measure(\"hello\"); }");

        assertTrue(result.succeeded(), () -> result.getProblems().toString());
        LPCGlobalFunction declaration = environment.resolveGlobalFunction("measure", 1);
        assertEquals("globals", declaration.objectPath());
        // Declaration lookup deliberately stops before semantic type resolution. Preserve the
        // existing mixed fallback while proving that discovery never executes create().
        assertEquals(LPCType.LPCMIXED, declaration.signature().returnType());
        assertEquals(List.of(LPCType.LPCMIXED), declaration.signature().parameterTypes());
    }

    @Test
    void globalShadowingAndExplicitNativeLookupUseDifferentDeclarations() {
        LPCCompilerContext environment = new LPCCompilerContext();
        EfunSignature nativeMeasure = new EfunSignature(
                new Symbol(LPCType.LPCINT, "measure"), List.of(LPCType.LPCSTRING));
        environment.registerSignature(nativeMeasure);
        environment.registerInMemoryObjectSource("globals",
                "string measure(string value) { return value; }", "/globals.c");
        environment.setMfunObjectPath("globals");

        // The parsed global declaration shadows the fully typed native declaration. Its source
        // types remain unresolved here, so the established declaration-only signature is mixed.
        assertEquals("globals", environment.resolveGlobalFunction("measure", 1).objectPath());
        assertEquals(LPCType.LPCMIXED,
                environment.resolveFunctionSignature("measure", 1).returnType());
        assertEquals(nativeMeasure, environment.resolveEngineFunctionSignature("measure", 1));
        assertTrue(new CompilationPipeline("java/lang/Object", environment)
                .run("string width() { return measure(\"hello\"); }").succeeded());
    }

    @Test
    void compileOnlyContextKeepsUntypedDeclarationsAsErrors() {
        CompilationPipeline pipeline = new CompilationPipeline("java/lang/Object");

        assertFalse(pipeline.run("answer() { return 42; }").succeeded());
        assertFalse(pipeline.run("int answer(value) { return value; }").succeeded());
    }
}
