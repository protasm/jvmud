package io.github.protasm.jvmud.compiler.semantic;

import io.github.protasm.jvmud.compiler.parser.ast.ASTObject;
import io.github.protasm.jvmud.compiler.pipeline.CompilationUnit;
import java.util.Objects;

/** Captures semantic artifacts produced by {@link SemanticAnalyzer}. */
public final class SemanticModel {
    private final ASTObject astObject;
    private final SemanticScope objectScope;
    private final CompilationUnit compilationUnit;

    public SemanticModel(ASTObject astObject, SemanticScope objectScope) {
        this(astObject, objectScope, null);
    }

    public SemanticModel(ASTObject astObject, SemanticScope objectScope, CompilationUnit compilationUnit) {
        this.astObject = Objects.requireNonNull(astObject, "astObject");
        this.objectScope = Objects.requireNonNull(objectScope, "objectScope");
        this.compilationUnit = compilationUnit;
    }

    public ASTObject astObject() {
        return astObject;
    }

    public SemanticScope objectScope() {
        return objectScope;
    }

    /**
     * Returns the compilation unit that produced this model when analysis came from a pipeline run.
     *
     * <p>The unit gives later stages access to resolved parent units, which is needed for compiler
     * metadata such as transitive LPC inheritance introspection. Ad-hoc semantic models may leave
     * this unset.</p>
     */
    public CompilationUnit compilationUnit() {
        return compilationUnit;
    }
}
