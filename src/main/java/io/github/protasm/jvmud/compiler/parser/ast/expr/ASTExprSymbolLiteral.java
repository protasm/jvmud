package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.Objects;

/** A quoted LPC symbol/name literal, parsed from source forms such as {@code 'item}. */
public final class ASTExprSymbolLiteral extends ASTExpression {
    private final String name;

    public ASTExprSymbolLiteral(int line, String name) {
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
