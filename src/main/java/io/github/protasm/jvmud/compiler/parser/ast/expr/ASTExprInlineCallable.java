package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.Objects;

/** AST node for LPC inline callable syntax, {@code (: expression :)}. */
public final class ASTExprInlineCallable extends ASTExpression {
    private final ASTExpression body;

    public ASTExprInlineCallable(int line, ASTExpression body) {
        super(line);
        this.body = Objects.requireNonNull(body, "body");
    }

    public ASTExpression body() {
        return body;
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCFUNCTION;
    }
}
