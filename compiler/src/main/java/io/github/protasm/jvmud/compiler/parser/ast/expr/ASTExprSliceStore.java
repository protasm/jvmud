package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.Objects;

public final class ASTExprSliceStore extends ASTExpression {
    private final ASTExpression target;
    private final ASTExpression start;
    private final ASTExpression end;
    private final ASTExpression value;

    public ASTExprSliceStore(
            int line, ASTExpression target, ASTExpression start, ASTExpression end, ASTExpression value) {
        super(line);
        this.target = Objects.requireNonNull(target, "target");
        this.start = Objects.requireNonNull(start, "start");
        this.end = end;
        this.value = Objects.requireNonNull(value, "value");
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

    public ASTExpression value() {
        return value;
    }

    @Override
    public LPCType lpcType() {
        return value.lpcType();
    }
}
