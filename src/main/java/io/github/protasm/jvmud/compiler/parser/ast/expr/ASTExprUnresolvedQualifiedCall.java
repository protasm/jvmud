package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTArguments;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.Objects;

public final class ASTExprUnresolvedQualifiedCall extends ASTExpression {
    private final String qualifier;
    private final String name;
    private final ASTArguments arguments;

    public ASTExprUnresolvedQualifiedCall(int line, String qualifier, String name, ASTArguments arguments) {
        super(line);
        this.qualifier = Objects.requireNonNull(qualifier, "qualifier");
        this.name = Objects.requireNonNull(name, "name");
        this.arguments = Objects.requireNonNull(arguments, "arguments");
    }

    public String qualifier() {
        return qualifier;
    }

    public String name() {
        return name;
    }

    public ASTArguments arguments() {
        return arguments;
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCMIXED;
    }
}
