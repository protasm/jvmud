package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.AssignOpType;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.Objects;

public final class ASTExprArrayStore extends ASTExpression {
    private final ASTExpression target;
    private final ASTExpression index;
    private final ASTExpression valueIndex;
    private final AssignOpType operator;
    private final ASTExpression value;

    public ASTExprArrayStore(int line, ASTExpression target, ASTExpression index, ASTExpression value) {
        this(line, target, index, null, AssignOpType.SET, value);
    }

    public ASTExprArrayStore(
            int line, ASTExpression target, ASTExpression index, AssignOpType operator, ASTExpression value) {
        this(line, target, index, null, operator, value);
    }

    /** Creates an indexed assignment, optionally targeting one multi-value mapping slot. */
    public ASTExprArrayStore(
            int line,
            ASTExpression target,
            ASTExpression index,
            ASTExpression valueIndex,
            AssignOpType operator,
            ASTExpression value) {
        super(line);
        this.target = Objects.requireNonNull(target, "target");
        this.index = Objects.requireNonNull(index, "index");
        this.valueIndex = valueIndex;
        this.operator = Objects.requireNonNull(operator, "operator");
        this.value = Objects.requireNonNull(value, "value");
    }

    public ASTExpression target() {
        return target;
    }

    public ASTExpression index() {
        return index;
    }

    /** Returns the optional value slot for a multi-value mapping assignment. */
    public ASTExpression valueIndex() {
        return valueIndex;
    }

    /** Returns the assignment operator applied to this indexed target. */
    public AssignOpType operator() {
        return operator;
    }

    public ASTExpression value() {
        return value;
    }

    @Override
    public LPCType lpcType() {
        return value.lpcType();
    }
}
