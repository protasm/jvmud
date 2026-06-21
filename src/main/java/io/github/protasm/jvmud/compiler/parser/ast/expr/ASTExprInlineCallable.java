package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.Objects;

/** AST node for LPC inline callable syntax, {@code (: expression :)} or {@code (: { ... } :)}. */
public final class ASTExprInlineCallable extends ASTExpression {
    private final ASTExpression body;
    private final ASTStmtBlock blockBody;

    public ASTExprInlineCallable(int line, ASTExpression body) {
        super(line);
        this.body = Objects.requireNonNull(body, "body");
        this.blockBody = null;
    }

    public ASTExprInlineCallable(int line, ASTStmtBlock blockBody) {
        super(line);
        this.body = null;
        this.blockBody = Objects.requireNonNull(blockBody, "blockBody");
    }

    public ASTExpression body() {
        return body;
    }

    public ASTStmtBlock blockBody() {
        return blockBody;
    }

    public boolean hasBlockBody() {
        return blockBody != null;
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCFUNCTION;
    }
}
