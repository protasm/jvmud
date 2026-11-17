package io.github.protasm.jvmud.language.compiler.parser.ast.expr;

import io.github.protasm.jvmud.language.compiler.parser.ast.ASTArguments;
import io.github.protasm.jvmud.language.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.language.compiler.type.LPCType;
import io.github.protasm.jvmud.language.efun.EfunSignature;

import java.util.Objects;

/**
 * A resolved global call carrying a declaration, not an executable callback.
 * Generated code looks up the implementation in its active execution context.
 */
public final class ASTExprCallEfun extends ASTExpression {
    private final EfunSignature signature;
    private final ASTArguments arguments;
    private final boolean engineOnly;

    /** Creates an ordinary call that permits mudlib function shadowing. */
    public ASTExprCallEfun(int line, EfunSignature signature, ASTArguments arguments) {
        this(line, signature, arguments, false);
    }

    /** Preserves whether an explicit engine qualifier must bypass mudlib functions. */
    public ASTExprCallEfun(int line, EfunSignature signature, ASTArguments arguments, boolean engineOnly) {
        super(line);

        this.signature = Objects.requireNonNull(signature, "signature");
        this.arguments = arguments;
        this.engineOnly = engineOnly;
    }

    /** Returns whether runtime dispatch must go directly to the engine registry. */
    public boolean engineOnly() {
        return engineOnly;
    }

    public ASTArguments arguments() {
        return arguments;
    }

    public EfunSignature signature() {
        return signature;
    }

    @Override
    public LPCType lpcType() {
        return signature.returnType();
    }
}
