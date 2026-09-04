package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.Objects;

/**
 * JVMud-neutral protected evaluation expression.
 *
 * <p>Compatibility source can spell this as {@code catch (...)}, but the AST keeps that
 * compatibility syntax at the parser boundary.</p>
 */
public final class ASTExprProtectedEval extends ASTExpression {
    private final ASTExpression body;
    private final boolean suppressLogging;

    public ASTExprProtectedEval(int line, ASTExpression body, boolean suppressLogging) {
        super(line);
        this.body = Objects.requireNonNull(body, "body");
        this.suppressLogging = suppressLogging;
    }

    public ASTExpression body() {
        return body;
    }

    public boolean suppressLogging() {
        return suppressLogging;
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCMIXED;
    }
}
