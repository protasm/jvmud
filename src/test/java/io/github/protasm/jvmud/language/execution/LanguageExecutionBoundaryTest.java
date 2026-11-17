package io.github.protasm.jvmud.language.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.language.compiler.LPCCompilerContext;
import io.github.protasm.jvmud.language.compiler.parser.ParserOptions;
import io.github.protasm.jvmud.language.compiler.parser.ast.Symbol;
import io.github.protasm.jvmud.language.compiler.pipeline.CompilationPipeline;
import io.github.protasm.jvmud.language.compiler.type.LPCType;
import io.github.protasm.jvmud.language.efun.Efun;
import io.github.protasm.jvmud.language.efun.EfunRegistry;
import io.github.protasm.jvmud.language.efun.EfunSignature;
import io.github.protasm.jvmud.language.runtime.RuntimeContext;
import io.github.protasm.jvmud.language.runtime.RuntimeContextHolder;

import java.util.List;

import org.junit.jupiter.api.Test;

/** Proves compiled invocation needs the language contract, not an engine or a world. */
class LanguageExecutionBoundaryTest {
    @Test
    void compiledFunctionAndObjectCallsUseTheNeutralExecutionContract() throws Exception {
        EfunRegistry functions = new EfunRegistry();
        functions.register(new Efun() {
            public EfunSignature signature() {
                return new EfunSignature(new Symbol(LPCType.LPCINT, "supplied"), List.of());
            }

            public Object call(RuntimeContext context, Object[] arguments) {
                return 40;
            }
        });
        LPCCompilerContext compiler = new LPCCompilerContext(null, functions);
        var result = new CompilationPipeline("java/lang/Object", compiler).run(
                null,
                "int answer(object target) { return supplied() + target->value(); }",
                "boundary/example",
                "/boundary/example.c",
                ParserOptions.defaults());
        assertTrue(result.succeeded(), () -> result.getProblems().toString());
        Class<?> program = new LPCRuntimeClassLoader(getClass().getClassLoader())
                .defineClass(result.getAstObject().name(), result.getBytecode());
        Object instance = program.getConstructor().newInstance();
        Object target = new Object();
        RuntimeContext context = new RuntimeContext() {
            public Object invokeEfun(String name, int arity, Object[] arguments) {
                return functions.lookup(name, arity).invoke(this, arguments);
            }

            public Object invokeEngineEfun(String name, int arity, Object[] arguments) {
                return invokeEfun(name, arity, arguments);
            }

            public Object invokeObject(Object receiver, String methodName, Object... arguments) {
                assertEquals(target, receiver);
                assertEquals("value", methodName);
                return 2;
            }

            public Object invokeOptionalObject(Object receiver, String methodName, Object... arguments) {
                return invokeObject(receiver, methodName, arguments);
            }

            public boolean hasObjectMethod(Object receiver, String methodName, int arity) {
                return true;
            }

            public boolean isDestroyedObject(Object value) {
                return false;
            }
        };
        RuntimeContext previous = RuntimeContextHolder.current();
        try {
            RuntimeContextHolder.setCurrent(context);
            assertEquals(42, program.getMethod("answer", Object.class).invoke(instance, target));
        } finally {
            RuntimeContextHolder.setCurrent(previous);
        }
    }
}
