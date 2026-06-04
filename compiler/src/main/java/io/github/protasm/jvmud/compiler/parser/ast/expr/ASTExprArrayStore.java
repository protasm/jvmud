package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.Objects;

public final class ASTExprArrayStore extends ASTExpression {
    private final ASTExpression target;
    private final ASTExpression index;
    private final ASTExpression value;

    public ASTExprArrayStore(int line, ASTExpression target, ASTExpression index, ASTExpression value) {
        super(line);
        this.target = Objects.requireNonNull(target, "target");
        this.index = Objects.requireNonNull(index, "index");
        this.value = Objects.requireNonNull(value, "value");
    }

    public ASTExpression target() {
        return target;
    }

    public ASTExpression index() {
        return index;
    }

    public ASTExpression value() {
        return value;
    }

    @Override
    public LPCType lpcType() {
        return value.lpcType();
    }
}
