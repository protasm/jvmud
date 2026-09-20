package io.github.protasm.jvmud.compiler;

import static org.junit.jupiter.api.Assertions.*;
import io.github.protasm.jvmud.compiler.exec.*;
import io.github.protasm.jvmud.engine.mudlib.*;
import java.nio.file.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DynamicTypesTest {
    @TempDir Path root;

    private LPCRuntime runtime(boolean dynamic) {
        var rt = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(root).build());
        io.github.protasm.jvmud.compiler.efun.builtin.CoreEfuns.registerCore(rt);
        rt.registerMudlibBoundary(MudlibBoundary.builder().dynamicTypes(dynamic).build());
        return rt;
    }

    @Test void declarationsPreserveValuesInsteadOfCoercingThem() throws Exception {
        var rt = runtime(true);
        var obj = rt.loadSource("values.c", """
                private string stored;
                int* echo(int value) { string local = value; stored = local; return stored; }
                void result() { return "returned"; }
                int* arrayLabel() { return 42; }
                """);
        Object marker = obj.instance();
        assertSame(marker, obj.invoke("echo", marker));
        assertEquals(List.of(1, "two"), obj.invoke("echo", List.of(1, "two")));
        assertEquals("returned", obj.invoke("result"));
        assertEquals(42, obj.invoke("arrayLabel"));
        assertEquals(Object.class, obj.instance().getClass().getDeclaredMethod("echo", Object.class).getReturnType());
    }

    @Test void inheritanceAndGlobalDeclarationsUseTheSamePolicy() throws Exception {
        Files.writeString(root.resolve("base.c"), "string stored; int value(string arg) { return arg; } int call(string arg) { return value(arg); } string read() { return stored; }");
        Files.writeString(root.resolve("child.c"), "inherit \"base\"; string value(int arg) { stored = 3; stored = arg; return read(); }");
        var rt = runtime(true);
        assertEquals(List.of(7), rt.load("child").invoke("call", List.of(7)));
        rt.registerMudlibBoundary(MudlibBoundary.builder().dynamicTypes(true).mudlibGlobalObjectPath("base").build());
        var caller = rt.loadSource("caller.c", "int test() { return value(({9})); }");
        assertEquals(List.of(9), caller.invoke("test"));
    }

    @Test void strictModeAndMissingDeclarationRulesRemainUnchanged() {
        assertFalse(MudlibBoundary.empty().dynamicTypes());
        assertThrows(IllegalArgumentException.class, () -> MudlibBoundary.builder().dynamicTypes(true)
                .fieldTypeOverride(new io.github.protasm.jvmud.compiler.transpiler.FieldTypeOverride("test.c", "value", "string", "int")).build());
        assertThrows(LPCRuntimeException.class, () -> runtime(false).loadSource("bad.c", "int value() { return ({1}); }"));
        assertThrows(LPCRuntimeException.class, () -> runtime(true).loadSource("untyped.c", "value() { return 1; }"));
        assertThrows(LPCRuntimeException.class, () -> runtime(true).loadSource("parameter.c", "int value(arg) { return arg; }"));
    }

    @Test void duplicateAritiesStillFailAndDifferentAritiesStillWork() throws Exception {
        var rt = runtime(true);
        var error = assertThrows(LPCRuntimeException.class, () -> rt.loadSource("duplicate.c",
                "int value(int a) { return a; } string value(string a) { return a; }"));
        assertTrue(error.getMessage().contains("Duplicate method"), error.getMessage());
        var obj = rt.loadSource("arity.c", "int value() { return 3; } string value(int a) { return a; }");
        assertEquals(3, obj.invoke("value"));
        assertEquals("text", obj.invoke("value", "text"));
    }

    @Test void manifestSelectsPolicyAndRejectsInvalidBoolean() throws Exception {
        Path config = root.resolve("world.config");
        Files.writeString(config, "compiler.dynamic_types = true\n");
        assertTrue(MudlibBoundaryConfigReader.read(root, "world.config").dynamicTypes());
        Files.writeString(config, "compiler.dynamic_types = nonsense\n");
        assertThrows(IllegalArgumentException.class, () -> MudlibBoundaryConfigReader.read(root, "world.config"));
    }

    @Test void literalsLoopsConversionsAndRuntimeErrorsKeepTheirSemantics() throws Exception {
        var rt = runtime(true);
        var obj = rt.loadSource("operations.c", """
                int calculate() {
                    string total = 0, values = ({2, 3});
                    foreach (string item in values) { total += item; }
                    for (string i = 0; i < 2; i++) { total += i; }
                    return total;
                }
                function callback() { return function int (string value) { return value; }; }
                string convert() { return jvmud_to_int("42"); }
                int invalid(int value) { return value - 1; }
                varargs int first(string value) { return value; }
                int useVarargs() { return first(7, 8); }
                """);
        assertEquals(6, obj.invoke("calculate"));
        var callback = (io.github.protasm.jvmud.compiler.runtime.RuntimeFunctionLiteral) obj.invoke("callback");
        assertEquals("int (string value)", callback.signature()); // Written declaration remains documentary.
        assertEquals(42, obj.invoke("convert"));
        assertEquals(7, obj.invoke("useVarargs"));
        assertThrows(RuntimeException.class, () -> obj.invoke("invalid", obj.instance()));
    }
}
