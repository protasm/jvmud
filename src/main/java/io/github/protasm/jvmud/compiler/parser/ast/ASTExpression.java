package io.github.protasm.jvmud.compiler.parser.ast;

import io.github.protasm.jvmud.compiler.parser.type.LPCType;

public abstract non-sealed class ASTExpression extends ASTNode {
    public ASTExpression(int line) {
        super(line);
    }

    public abstract LPCType lpcType();
}
