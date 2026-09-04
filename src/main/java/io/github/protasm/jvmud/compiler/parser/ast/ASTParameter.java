package io.github.protasm.jvmud.compiler.parser.ast;

public final class ASTParameter extends ASTNode {
    private final Symbol symbol;
    private final boolean varargs;

    public ASTParameter(int line, Symbol symbol) {
        this(line, symbol, false);
    }

    /**
     * Creates a parsed LPC parameter.
     *
     * <p>The {@code varargs} flag records parameter-local optional/rest spelling such
     * as {@code varargs mixed *data}. JVMud currently preserves that source shape for compatibility
     * analysis; call dispatch still uses the declared parameter type and arity.</p>
     *
     * @param line source line
     * @param symbol declared parameter symbol
     * @param varargs whether the parameter was prefixed with {@code varargs}
     */
    public ASTParameter(int line, Symbol symbol, boolean varargs) {
        super(line);

        this.symbol = symbol;
        this.varargs = varargs;
    }

    public Symbol symbol() {
        return symbol;
    }

    public boolean isVarargs() {
        return varargs;
    }

    public String descriptor() {
        return symbol.descriptor();
    }
}
