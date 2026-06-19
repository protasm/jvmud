package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.List;
import java.util.Objects;

public record IRCollectionTransform(
        int line,
        Operation operation,
        IRExpression source,
        List<IRExpression> extraArguments,
        IRExpression callback,
        IRLocal sourceLocal,
        IRLocal itemsLocal,
        IRLocal resultLocal,
        IRLocal indexLocal,
        RuntimeType type)
        implements IRExpression {
    public enum Operation {
        FILTER,
        MAP
    }

    public IRCollectionTransform {
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(source, "source");
        extraArguments = List.copyOf(extraArguments);
        Objects.requireNonNull(callback, "callback");
        Objects.requireNonNull(sourceLocal, "sourceLocal");
        Objects.requireNonNull(itemsLocal, "itemsLocal");
        Objects.requireNonNull(resultLocal, "resultLocal");
        Objects.requireNonNull(indexLocal, "indexLocal");
        Objects.requireNonNull(type, "type");
    }
}
