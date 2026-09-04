package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.Objects;

public final class IRMappingSet implements IRExpression {
    private final int line;
    private final IRExpression mapping;
    private final IRExpression key;
    private final IRExpression valueIndex;
    private final IRExpression value;
    private final RuntimeType type;

    public IRMappingSet(int line, IRExpression mapping, IRExpression key, IRExpression value, RuntimeType type) {
        this(line, mapping, key, null, value, type);
    }

    /** Creates a mapping assignment, optionally targeting one multi-value slot. */
    public IRMappingSet(
            int line,
            IRExpression mapping,
            IRExpression key,
            IRExpression valueIndex,
            IRExpression value,
            RuntimeType type) {
        this.line = line;
        this.mapping = Objects.requireNonNull(mapping, "mapping");
        this.key = Objects.requireNonNull(key, "key");
        this.valueIndex = valueIndex;
        this.value = Objects.requireNonNull(value, "value");
        this.type = Objects.requireNonNull(type, "type");
    }

    @Override
    public int line() {
        return line;
    }

    public IRExpression mapping() {
        return mapping;
    }

    public IRExpression key() {
        return key;
    }

    public IRExpression valueIndex() {
        return valueIndex;
    }

    public IRExpression value() {
        return value;
    }

    @Override
    public RuntimeType type() {
        return type;
    }
}
