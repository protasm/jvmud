package io.github.protasm.jvmud.compiler.parser.ast;

import io.github.protasm.jvmud.compiler.parser.ast.visitor.ASTVisitor;

public abstract sealed class ASTNode
        permits ASTArgument,
                ASTExpression,
                ASTField,
                ASTInherit,
                ASTListNode,
                ASTLocal,
                ASTMapNode,
                ASTMethod,
                ASTObject,
                ASTParameter,
                ASTStatement {
    protected final int line;
    private int sourceOrder = -1;

    public ASTNode(int line) {
        this.line = line;
    }

    public int line() {
        return line;
    }

    public int sourceOrder() {
        return sourceOrder;
    }

    public void setSourceOrder(int sourceOrder) {
        this.sourceOrder = sourceOrder;
    }

    public String className() {
        return getClass().getSimpleName();
    }

    public final void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
