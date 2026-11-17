package io.github.protasm.jvmud.language.compiler;

import io.github.protasm.jvmud.language.compiler.transpiler.FieldTypeOverride;
import io.github.protasm.jvmud.language.compiler.transpiler.LocalTypeOverride;
import io.github.protasm.jvmud.language.compiler.transpiler.MethodVarargsOverride;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Language-owned compatibility settings selected by a compilation host.
 *
 * <p>The optional source root anchors per-file overrides. These settings describe source
 * interpretation; they contain no engine lifecycle, connection, credential or persistence policy.</p>
 */
public record LPCCompilationOptions(
        Path sourceRoot,
        boolean dynamicTypes,
        boolean transpileUntypedMethods,
        boolean transpileImplicitSelfCalls,
        List<FieldTypeOverride> fieldTypeOverrides,
        List<LocalTypeOverride> localTypeOverrides,
        List<MethodVarargsOverride> methodVarargsOverrides,
        Map<String, String> predefines,
        Map<String, Map<String, String>> functionPredefines) {
    public LPCCompilationOptions {
        fieldTypeOverrides = List.copyOf(fieldTypeOverrides);
        localTypeOverrides = List.copyOf(localTypeOverrides);
        methodVarargsOverrides = List.copyOf(methodVarargsOverrides);
        predefines = Map.copyOf(predefines);
        Map<String, Map<String, String>> copiedFunctions = new LinkedHashMap<>();
        functionPredefines.forEach((name, definitions) ->
                copiedFunctions.put(name, Map.copyOf(definitions)));
        functionPredefines = Map.copyOf(copiedFunctions);
    }

    /** Returns the strict, unmodified LPC source profile. */
    public static LPCCompilationOptions defaults() {
        return new LPCCompilationOptions(null, false, false, false,
                List.of(), List.of(), List.of(), Map.of(), Map.of());
    }
}
