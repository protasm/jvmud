package io.github.protasm.jvmud.language.parser.ast.expr;

import io.github.protasm.jvmud.language.parser.ast.ASTExpression;
import io.github.protasm.jvmud.language.efun.Efun;
import io.github.protasm.jvmud.language.parser.ast.ASTArguments;
import io.github.protasm.jvmud.language.parser.type.LPCType;

public final class ASTExprCallEfun extends ASTExpression {
    private final Efun efun;
    private final ASTArguments arguments;
    private final boolean engineOnly;

    /** Creates an ordinary call that permits mudlib function shadowing. */
    public ASTExprCallEfun(int line, Efun efun, ASTArguments arguments) {
        this(line, efun, arguments, false);
    }

    /** Preserves whether an explicit engine qualifier must bypass mudlib functions. */
    public ASTExprCallEfun(int line, Efun efun, ASTArguments arguments, boolean engineOnly) {
        super(line);

        this.efun = efun;
        this.arguments = arguments;
        this.engineOnly = engineOnly;
    }

    /** Returns whether runtime dispatch must go directly to the engine registry. */
    public boolean engineOnly() {
        return engineOnly;
    }

    public Efun efun() {
        return efun;
    }

    public ASTArguments arguments() {
        return arguments;
    }

    public io.github.protasm.jvmud.language.efun.EfunSignature signature() {
        return efun.signature();
    }

    @Override
    public LPCType lpcType() {
        return efun.signature().returnType();
    }
}
