package io.github.protasm.jvmud.language.compiler;

import io.github.protasm.jvmud.language.efun.EfunSignature;

import java.util.Objects;

/** A resolved global declaration; execution loads the named object only when the call occurs. */
public record LPCGlobalFunction(String objectPath, EfunSignature signature) {
    public LPCGlobalFunction {
        Objects.requireNonNull(objectPath, "objectPath");
        Objects.requireNonNull(signature, "signature");
    }
}
