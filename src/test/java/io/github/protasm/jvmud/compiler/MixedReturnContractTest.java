package io.github.protasm.jvmud.compiler;

import static org.junit.jupiter.api.Assertions.*;

import io.github.protasm.jvmud.compiler.exec.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Explicit mixed return contracts remain stable through analysis and JVM virtual dispatch. */
class MixedReturnContractTest {
    @TempDir Path root;

    private LPCRuntime runtime() {
        return new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(root).build());
    }

    @Test void statusAndIntegerImplementationsShareMixedSignature() throws Exception {
        Files.writeString(root.resolve("base.c"), """
                mixed can_put_and_get(mixed value) { return value != 0; }
                mixed check(mixed value) { return can_put_and_get(value); }
                """);
        Files.writeString(root.resolve("child.c"), """
                inherit "base";
                mixed can_put_and_get(mixed value) {
                    if (!value) return 0;
                    return 1;
                }
                """);
        var rt = runtime();
        var base = rt.load("base");
        var child = rt.load("child");
        assertEquals(Object.class, base.instance().getClass().getDeclaredMethod("can_put_and_get", Object.class).getReturnType());
        assertEquals(Object.class, child.instance().getClass().getDeclaredMethod("can_put_and_get", Object.class).getReturnType());
        assertEquals(0, child.invoke("check", 0));
        assertEquals(1, child.invoke("check", "bag"));
    }

    @Test void inheritedCallerDispatchesToDifferentReturnKindsAcrossThreeLevels() throws Exception {
        Files.writeString(root.resolve("base.c"), """
                mixed description() { return "base"; }
                mixed describe() { return description(); }
                """);
        Files.writeString(root.resolve("child.c"), """
                inherit "base";
                mixed description() { return 42; }
                """);
        Files.writeString(root.resolve("grandchild.c"), """
                inherit "child";
                mixed description() { return ({"grandchild", 7}); }
                """);
        var rt = runtime();
        assertEquals("base", rt.load("base").invoke("describe"));
        assertEquals(42, rt.load("child").invoke("describe"));
        assertEquals(java.util.List.of("grandchild", 7), rt.load("grandchild").invoke("describe"));
    }

    @Test void explicitNarrowContractsAndUntypedDeclarationsStillRejectInvalidCode() throws Exception {
        Files.writeString(root.resolve("base.c"), "int value() { return 1; }");
        Files.writeString(root.resolve("child.c"), "inherit \"base\"; string value() { return \"wrong\"; }");
        assertThrows(LPCRuntimeException.class, () -> runtime().load("child"));
        assertThrows(LPCRuntimeException.class, () -> runtime().loadSource("bad.c", "int value() { return ({1}); }"));
        assertThrows(LPCRuntimeException.class, () -> runtime().loadSource("untyped.c", "value() { return 1; }"));
    }
}
