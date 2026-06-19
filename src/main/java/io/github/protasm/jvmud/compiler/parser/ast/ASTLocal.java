package io.github.protasm.jvmud.compiler.parser.ast;

public final class ASTLocal extends ASTNode {
    private final Symbol symbol;
    private int slot;
    private int scopeDepth;
    private int scopeId;

    public ASTLocal(int line, Symbol symbol) {
        super(line);

        this.symbol = symbol;

        slot = -1;
        scopeDepth = -1;
        scopeId = -1;
    }

    public Symbol symbol() {
        return symbol;
    }

    public int slot() {
        return slot;
    }

    public int scopeDepth() {
        return scopeDepth;
    }

    /** Returns the parser scope identity that owns this local declaration. */
    public int scopeId() {
        return scopeId;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public void setScopeDepth(int scopeDepth) {
        this.scopeDepth = scopeDepth;
    }

    /** Sets the parser scope identity that owns this local declaration. */
    public void setScopeId(int scopeId) {
        this.scopeId = scopeId;
    }

    public String descriptor() {
        return symbol.descriptor();
    }
}
