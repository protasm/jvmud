package io.github.protasm.jvmud.compiler.parser.ast.stmt;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.ASTStatement;

public final class ASTStmtDoWhile extends ASTStatement {
    private final ASTStatement body;
    private final ASTExpression condition;

    public ASTStmtDoWhile(int line, ASTStatement body, ASTExpression condition) {
        super(line);
        this.body = body;
        this.condition = condition;
    }

    public ASTStatement body() {
        return body;
    }

    public ASTExpression condition() {
        return condition;
    }
}
