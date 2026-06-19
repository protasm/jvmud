package io.github.protasm.jvmud.compiler.parser.ast;

public final class ASTField extends ASTNode {
    private final String ownerName;
    private final Symbol symbol;
    private ASTExpression initializer;
    private final boolean declared;
    private final DeclarationModifiers modifiers;
    private boolean defined;

    public ASTField(int line, String ownerName, Symbol symbol) {
        this(line, ownerName, symbol, true);
    }

    public ASTField(int line, String ownerName, Symbol symbol, boolean declared) {
        this(line, ownerName, symbol, declared, false);
    }

    public ASTField(int line, String ownerName, Symbol symbol, boolean declared, boolean staticModifier) {
        this(
                line,
                ownerName,
                symbol,
                declared,
                new DeclarationModifiers(
                        DeclarationModifiers.Visibility.DEFAULT,
                        staticModifier,
                        false,
                        false,
                        false,
                        false));
    }

    public ASTField(
            int line,
            String ownerName,
            Symbol symbol,
            boolean declared,
            DeclarationModifiers modifiers) {
        super(line);

        this.ownerName = ownerName;
        this.symbol = symbol;
        this.declared = declared;
        this.modifiers = modifiers != null ? modifiers : DeclarationModifiers.NONE;

        initializer = null;
        defined = false;
    }

    public String ownerName() {
        return ownerName;
    }

    public Symbol symbol() {
        return symbol;
    }

    public ASTExpression initializer() {
        return initializer;
    }

    public void setInitializer(ASTExpression expr) {
        this.initializer = expr;
    }

    public String descriptor() {
        return symbol.descriptor();
    }

    public boolean isDeclared() {
        return declared;
    }

    public boolean isStatic() {
        return modifiers.isStatic();
    }

    public DeclarationModifiers modifiers() {
        return modifiers;
    }

    public boolean isDefined() {
        return defined;
    }

    public void markDefined() {
        this.defined = true;
    }
}
