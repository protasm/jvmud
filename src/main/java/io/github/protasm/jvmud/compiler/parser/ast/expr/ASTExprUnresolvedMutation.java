package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.Objects;

/** Unresolved local-or-field increment/decrement expression before semantic binding. */
public final class ASTExprUnresolvedMutation extends ASTExpression {
    private final String name;
    private final int delta;
    private final boolean prefix;

    public ASTExprUnresolvedMutation(int line, String name, int delta, boolean prefix) {
        super(line);
        this.name = Objects.requireNonNull(name, "name");
        this.delta = delta;
        this.prefix = prefix;
    }

    public String name() {
        return name;
    }

    public int delta() {
        return delta;
    }

    /** Returns true when the expression should evaluate to the updated value. */
    public boolean isPrefix() {
        return prefix;
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCMIXED;
    }
}
