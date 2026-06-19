package io.github.protasm.jvmud.compiler.parser.ast.stmt;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.ASTStatement;

public final class ASTStmtWhile extends ASTStatement {
    private final ASTExpression condition;
    private final ASTStatement body;

    public ASTStmtWhile(int line, ASTExpression condition, ASTStatement body) {
        super(line);
        this.condition = condition;
        this.body = body;
    }

    public ASTExpression condition() {
        return condition;
    }

    public ASTStatement body() {
        return body;
    }
}
