package io.github.protasm.jvmud.language.parser.ast;

public final class ASTLocal extends ASTNode {
    private Symbol symbol;
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

    /**
     * Replaces this local's declaration before semantic analysis for checked source translation.
     * References retain the same local identity, scope and source location.
     */
    public void replaceDeclaredType(String typeName) {
        if (symbol.declaredType() != null)
            throw new IllegalStateException("Cannot translate a local after type resolution");
        symbol = new Symbol(typeName, symbol.name());
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
