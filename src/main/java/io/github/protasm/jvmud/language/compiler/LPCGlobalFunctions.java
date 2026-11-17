package io.github.protasm.jvmud.language.compiler;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * Sources and name mappings for declaration-backed global LPC functions.
 *
 * <p>A source file override is useful when the global object is outside the normal include roots.
 * The compiler reads its declarations without constructing or initializing that object. Explicit
 * compatibility overrides precede ordinary mudlib globals; native aliases remain the fallback.</p>
 */
public record LPCGlobalFunctions(
        String mudlibObjectPath,
        Path mudlibSourcePath,
        String compatibilityObjectPath,
        Path compatibilitySourcePath,
        Set<String> compatibilityOverrides,
        Map<String, String> engineFunctionAliases) {
    public LPCGlobalFunctions {
        compatibilityOverrides = Set.copyOf(compatibilityOverrides);
        engineFunctionAliases = Map.copyOf(engineFunctionAliases);
    }

    public static LPCGlobalFunctions empty() {
        return new LPCGlobalFunctions(null, null, null, null, Set.of(), Map.of());
    }
}
