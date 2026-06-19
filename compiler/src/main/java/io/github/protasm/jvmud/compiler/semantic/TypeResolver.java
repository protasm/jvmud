package io.github.protasm.jvmud.compiler.semantic;

import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.Map;

/** Resolves LPC type keyword strings to {@link LPCType} values. */
public final class TypeResolver {
    private static final Map<String, LPCType> TYPE_KEYWORDS =
            Map.of(
                    "int", LPCType.LPCINT,
                    "float", LPCType.LPCFLOAT,
                    "mapping", LPCType.LPCMAPPING,
                    "mixed", LPCType.LPCMIXED,
                    "object", LPCType.LPCOBJECT,
                    "status", LPCType.LPCSTATUS,
                    "string", LPCType.LPCSTRING,
                    "void", LPCType.LPCVOID);

    public LPCType resolve(String typeName) {
        if (typeName == null)
            return null;

        String normalized = typeName.replaceAll("\\s+", "").toLowerCase();
        int baseEnd = normalized.length();
        while (baseEnd > 0 && normalized.charAt(baseEnd - 1) == '*')
            baseEnd--;

        if (baseEnd < normalized.length()) {
            String base = normalized.substring(0, baseEnd);
            if (TYPE_KEYWORDS.containsKey(base))
                return LPCType.LPCARRAY;
        }

        return TYPE_KEYWORDS.get(normalized);
    }
}
