package io.github.protasm.jvmud.language.ir;

import java.util.Objects;

public record TypedIR(IRObject object) {
    public TypedIR {
        Objects.requireNonNull(object, "object");
    }
}
