package io.github.protasm.jvmud.compiler.parser.ast.stmt;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.ASTLocal;
import io.github.protasm.jvmud.compiler.parser.ast.ASTStatement;
import java.util.Objects;

public final class ASTStmtForeach extends ASTStatement {
    private final ASTLocal keyLocal;
    private final ASTLocal valueLocal;
    private final ASTExpression iterable;
    private final ASTStatement body;

    public ASTStmtForeach(
            int line, ASTLocal keyLocal, ASTLocal valueLocal, ASTExpression iterable, ASTStatement body) {
        super(line);
        this.keyLocal = Objects.requireNonNull(keyLocal, "keyLocal");
        this.valueLocal = valueLocal;
        this.iterable = Objects.requireNonNull(iterable, "iterable");
        this.body = Objects.requireNonNull(body, "body");
    }

    public ASTLocal keyLocal() {
        return keyLocal;
    }

    public ASTLocal valueLocal() {
        return valueLocal;
    }

    public boolean hasValueLocal() {
        return valueLocal != null;
    }

    public ASTExpression iterable() {
        return iterable;
    }

    public ASTStatement body() {
        return body;
    }
}
