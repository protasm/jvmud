package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.Objects;

/** A named callable reference, parsed from source forms such as {@code #'moveHook}. */
public final class ASTExprFunctionReference extends ASTExpression {
    private final String name;

    public ASTExprFunctionReference(int line, String name) {
        super(line);
        this.name = Objects.requireNonNull(name, "name");
    }

    public String name() {
        return name;
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCMIXED;
    }
}
