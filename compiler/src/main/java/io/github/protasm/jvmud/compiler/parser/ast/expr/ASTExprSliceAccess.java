package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.Objects;

public final class ASTExprSliceAccess extends ASTExpression {
    private final ASTExpression target;
    private final ASTExpression start;
    private final ASTExpression end;

    public ASTExprSliceAccess(int line, ASTExpression target, ASTExpression start, ASTExpression end) {
        super(line);
        this.target = Objects.requireNonNull(target, "target");
        this.start = Objects.requireNonNull(start, "start");
        this.end = end;
    }

    public ASTExpression target() {
        return target;
    }

    public ASTExpression start() {
        return start;
    }

    public ASTExpression end() {
        return end;
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCMIXED;
    }
}
