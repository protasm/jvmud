package io.github.protasm.jvmud.language.compiler.parser.ast.expr;

import io.github.protasm.jvmud.language.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.language.compiler.type.LPCType;

public final class ASTExprUnresolvedIdentifier extends ASTExpression {
    private final String name;

    public ASTExprUnresolvedIdentifier(int line, String name) {
        super(line);

        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCMIXED;
    }
}
