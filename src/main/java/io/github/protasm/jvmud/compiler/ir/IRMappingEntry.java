package io.github.protasm.jvmud.compiler.ir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class IRMappingEntry {
    private final IRExpression key;
    private final List<IRExpression> values;

    public IRMappingEntry(IRExpression key, IRExpression value) {
        this(key, List.of(value));
    }

    public IRMappingEntry(IRExpression key, List<IRExpression> values) {
        this.key = Objects.requireNonNull(key, "key");
        if (values == null || values.isEmpty())
            throw new IllegalArgumentException("mapping entry requires at least one value");
        this.values = new ArrayList<>(values);
        this.values.forEach(value -> Objects.requireNonNull(value, "value"));
    }

    public IRExpression key() {
        return key;
    }

    public IRExpression value() {
        return values.get(0);
    }

    public List<IRExpression> values() {
        return Collections.unmodifiableList(values);
    }
}
