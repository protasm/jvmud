package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.List;
import java.util.Objects;

public final class ASTExprCollectionTransform extends ASTExpression {
    public enum Operation {
        FILTER,
        MAP
    }

    private final Operation operation;
    private final ASTExpression source;
    private final ASTExprInlineCallable callback;
    private final List<ASTExpression> extraArguments;

    public ASTExprCollectionTransform(
            int line,
            Operation operation,
            ASTExpression source,
            ASTExprInlineCallable callback,
            List<ASTExpression> extraArguments) {
        super(line);
        this.operation = Objects.requireNonNull(operation, "operation");
        this.source = Objects.requireNonNull(source, "source");
        this.callback = Objects.requireNonNull(callback, "callback");
        this.extraArguments = List.copyOf(extraArguments);
    }

    public Operation operation() {
        return operation;
    }

    public ASTExpression source() {
        return source;
    }

    public ASTExprInlineCallable callback() {
        return callback;
    }

    public List<ASTExpression> extraArguments() {
        return extraArguments;
    }

    @Override
    public LPCType lpcType() {
        return source.lpcType();
    }
}
