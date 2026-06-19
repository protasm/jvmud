package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.List;
import java.util.Objects;

public record IRCollectionTransform(
        int line,
        Operation operation,
        IRExpression source,
        List<IRExpression> extraArguments,
        IRExpression callbackBody,
        IRLocal sourceLocal,
        IRLocal itemsLocal,
        IRLocal resultLocal,
        IRLocal indexLocal,
        List<IRLocal> callbackArgumentLocals,
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
        Objects.requireNonNull(callbackBody, "callbackBody");
        Objects.requireNonNull(sourceLocal, "sourceLocal");
        Objects.requireNonNull(itemsLocal, "itemsLocal");
        Objects.requireNonNull(resultLocal, "resultLocal");
        Objects.requireNonNull(indexLocal, "indexLocal");
        callbackArgumentLocals = List.copyOf(callbackArgumentLocals);
        Objects.requireNonNull(type, "type");
    }
}
