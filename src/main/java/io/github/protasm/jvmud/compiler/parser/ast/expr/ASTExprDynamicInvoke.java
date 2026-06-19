package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTArguments;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.Objects;

public final class ASTExprDynamicInvoke extends ASTExpression {
    private final ASTExpression target;
    private final String methodName;
    private final ASTArguments arguments;

    public ASTExprDynamicInvoke(int line, ASTExpression target, String methodName, ASTArguments arguments) {
        super(line);
        this.target = Objects.requireNonNull(target, "target");
        this.methodName = Objects.requireNonNull(methodName, "methodName");
        this.arguments = Objects.requireNonNull(arguments, "arguments");
    }

    public ASTExpression target() {
        return target;
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
