package io.github.protasm.jvmud.compiler.parser.ast.stmt;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.ASTStatement;

public final class ASTStmtExpression extends ASTStatement {
    private final ASTExpression expression;

    public ASTStmtExpression(int line, ASTExpression expression) {
        super(line);
        this.expression = expression;
    }

    public ASTExpression expression() {
        return expression;
    }
}
