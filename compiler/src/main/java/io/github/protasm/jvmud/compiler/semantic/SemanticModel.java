package io.github.protasm.jvmud.compiler.semantic;

import io.github.protasm.jvmud.compiler.parser.ast.ASTObject;
import java.util.Objects;

/** Captures semantic artifacts produced by {@link SemanticAnalyzer}. */
public final class SemanticModel {
    private final ASTObject astObject;
    private final SemanticScope objectScope;

    public SemanticModel(ASTObject astObject, SemanticScope objectScope) {
        this.astObject = Objects.requireNonNull(astObject, "astObject");
        this.objectScope = Objects.requireNonNull(objectScope, "objectScope");
    }

    public ASTObject astObject() {
        return astObject;
    }

    public SemanticScope objectScope() {
        return objectScope;
    }
}
