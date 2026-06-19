package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.Objects;

/** Represents an LPC from-end index bound such as {@code <1}. */
public final class ASTExprFromEndIndex extends ASTExpression {
    private final ASTExpression distance;

    public ASTExprFromEndIndex(int line, ASTExpression distance) {
        super(line);
        this.distance = Objects.requireNonNull(distance, "distance");
    }

    public ASTExpression distance() {
        return distance;
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCINT;
    }
}
