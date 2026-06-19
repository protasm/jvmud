package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.ASTField;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.Objects;

/** Bound field increment/decrement expression with prefix or postfix result semantics. */
public final class ASTExprFieldMutation extends ASTExpression {
    private final ASTField field;
    private final int delta;
    private final boolean prefix;

    public ASTExprFieldMutation(int line, ASTField field, int delta, boolean prefix) {
        super(line);
        this.field = Objects.requireNonNull(field, "field");
        this.delta = delta;
        this.prefix = prefix;
    }

    public ASTField field() {
        return field;
    }

    public int delta() {
        return delta;
    }

    /** Returns true when the expression should evaluate to the updated field value. */
    public boolean isPrefix() {
        return prefix;
    }

    @Override
    public LPCType lpcType() {
        return field.symbol().lpcType();
    }
}
