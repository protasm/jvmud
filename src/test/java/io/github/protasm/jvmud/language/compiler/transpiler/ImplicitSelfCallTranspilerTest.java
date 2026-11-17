package io.github.protasm.jvmud.language.compiler.transpiler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.language.compiler.LPCCompilationOptions;
import io.github.protasm.jvmud.language.compiler.LPCCompilerContext;
import io.github.protasm.jvmud.language.compiler.parser.ast.Symbol;
import io.github.protasm.jvmud.language.compiler.pipeline.CompilationPipeline;
import io.github.protasm.jvmud.language.compiler.type.LPCType;
import io.github.protasm.jvmud.language.efun.EfunSignature;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Checks opt-in name resolution using declarations rather than live engine functions. */
class ImplicitSelfCallTranspilerTest {
    private CompilationPipeline compiler(boolean enabled) {
        var environment = new LPCCompilerContext();
        environment.setOptions(new LPCCompilationOptions(null, false, false, enabled,
                List.of(), List.of(), List.of(), Map.of(), Map.of()));
        environment.registerSignature(new EfunSignature(
                new Symbol(LPCType.LPCOBJECT, "jvmud_current_lpc_object"), List.of()));
        environment.registerSignature(new EfunSignature(
                new Symbol(LPCType.LPCINT, "jvmud_time"), List.of()));
        return new CompilationPipeline("java/lang/Object", environment);
    }

    @Test void onlyUnknownCallsAreRewrittenAndOrdinaryTypeCheckingRemainsStrict() {
        String lateBound = "mixed run() { return describe(7); }";
        assertTrue(compiler(true).run(lateBound).succeeded());
        assertFalse(compiler(false).run(lateBound).succeeded());
        assertFalse(compiler(true).run("mixed run() { return jvmud_time(1); }").succeeded());
        assertFalse(compiler(true).run("run() { return 1; }").succeeded());
        assertFalse(compiler(true).run("int run() { return \"text\"; }").succeeded());
    }
}
