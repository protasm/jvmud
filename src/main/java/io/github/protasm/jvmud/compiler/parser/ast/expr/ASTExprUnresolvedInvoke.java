package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTArguments;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;

public final class ASTExprUnresolvedInvoke extends ASTExpression {
    private final String targetName;
    private final String methodName;
    private final ASTArguments arguments;

    public ASTExprUnresolvedInvoke(int line, String targetName, String methodName, ASTArguments arguments) {
        super(line);

        this.targetName = targetName;
        this.methodName = methodName;
        this.arguments = arguments;
    }

    public String targetName() {
        return targetName;
    }

    public String methodName() {
        return methodName;
    }

    public ASTArguments arguments() {
        return arguments;
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCMIXED;
    }
}
