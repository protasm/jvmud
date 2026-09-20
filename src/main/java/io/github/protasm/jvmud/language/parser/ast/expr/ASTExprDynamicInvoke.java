package io.github.protasm.jvmud.language.parser.ast.expr;

import io.github.protasm.jvmud.language.parser.ast.ASTArguments;
import io.github.protasm.jvmud.language.parser.ast.ASTExpression;
import io.github.protasm.jvmud.language.parser.type.LPCType;
import java.util.Objects;

public final class ASTExprDynamicInvoke extends ASTExpression {
    private final ASTExpression target;
    private final boolean required;
    private final String methodName;
    private final ASTArguments arguments;

    public ASTExprDynamicInvoke(int line, ASTExpression target, String methodName, ASTArguments arguments) {
        this(line, target, methodName, arguments, false);
    }

    /** A required invocation reports a missing implementation instead of returning LPC zero. */
    public ASTExprDynamicInvoke(int line, ASTExpression target, String methodName, ASTArguments arguments,
            boolean required) {
        super(line);
        this.required = required;
        this.target = Objects.requireNonNull(target, "target");
        this.methodName = Objects.requireNonNull(methodName, "methodName");
        this.arguments = Objects.requireNonNull(arguments, "arguments");
    }

    /** Whether dispatch requires an implementation on the actual target. */
    public boolean required() { return required; }

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
