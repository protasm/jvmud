package io.github.protasm.jvmud.compiler.ir;

import java.util.Objects;

public record TypedIR(IRObject object) {
    public TypedIR {
        Objects.requireNonNull(object, "object");
    }
}
