package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.Objects;

public final class IRMappingGet implements IRExpression {
    private final int line;
    private final IRExpression mapping;
    private final IRExpression key;
    private final IRExpression valueIndex;
    private final RuntimeType type;

    public IRMappingGet(int line, IRExpression mapping, IRExpression key, RuntimeType type) {
        this(line, mapping, key, null, type);
    }

    public IRMappingGet(int line, IRExpression mapping, IRExpression key, IRExpression valueIndex, RuntimeType type) {
        this.line = line;
        this.mapping = Objects.requireNonNull(mapping, "mapping");
        this.key = Objects.requireNonNull(key, "key");
        this.valueIndex = valueIndex;
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

    @Override
    public RuntimeType type() {
        return type;
    }
}
