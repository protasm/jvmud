package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.Objects;

public final class ASTExprArrayAccess extends ASTExpression {
    private final ASTExpression target;
    private final ASTExpression index;
    private final ASTExpression valueIndex;

    public ASTExprArrayAccess(int line, ASTExpression target, ASTExpression index) {
        this(line, target, index, null);
    }

    public ASTExprArrayAccess(int line, ASTExpression target, ASTExpression index, ASTExpression valueIndex) {
        super(line);
        this.target = Objects.requireNonNull(target, "target");
        this.index = Objects.requireNonNull(index, "index");
        this.valueIndex = valueIndex;
    }

    public ASTExpression target() {
        return target;
    }

    public ASTExpression index() {
        return index;
    }

    public ASTExpression valueIndex() {
        return valueIndex;
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCMIXED;
    }
}
