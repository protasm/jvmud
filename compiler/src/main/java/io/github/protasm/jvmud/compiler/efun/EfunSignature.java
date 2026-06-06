package io.github.protasm.jvmud.compiler.efun;

import io.github.protasm.jvmud.compiler.parser.ast.Symbol;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.List;
import java.util.Objects;

/** Describes an engine function: its name, return type, and parameter types. */
public record EfunSignature(Symbol symbol, List<LPCType> parameterTypes) {
    public EfunSignature {
        Objects.requireNonNull(symbol, "symbol");
        parameterTypes = List.copyOf(parameterTypes != null ? parameterTypes : List.of());
    }

    public String name() {
        return symbol.name();
    }

    public LPCType returnType() {
        return symbol.lpcType();
    }

    public int arity() {
        return parameterTypes.size();
    }
}
