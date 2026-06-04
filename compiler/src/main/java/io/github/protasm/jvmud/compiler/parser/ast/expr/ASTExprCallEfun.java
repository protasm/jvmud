package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.efun.Efun;
import io.github.protasm.jvmud.compiler.parser.ast.ASTArguments;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;

public final class ASTExprCallEfun extends ASTExpression {
    private final Efun efun;
    private final ASTArguments arguments;

    public ASTExprCallEfun(int line, Efun efun, ASTArguments arguments) {
        super(line);

        this.efun = efun;
        this.arguments = arguments;
    }

    public Efun efun() {
        return efun;
    }

    public ASTArguments arguments() {
        return arguments;
    }

    public io.github.protasm.jvmud.compiler.efun.EfunSignature signature() {
        return efun.signature();
    }

    @Override
    public LPCType lpcType() {
        return efun.signature().returnType();
    }
}
