package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ASTExprMappingEntry {
    private final ASTExpression key;
    private final List<ASTExpression> values;

    public ASTExprMappingEntry(ASTExpression key, ASTExpression value) {
        this(key, List.of(value));
    }

    public ASTExprMappingEntry(ASTExpression key, List<ASTExpression> values) {
        this.key = Objects.requireNonNull(key, "key");
        if (values == null || values.isEmpty())
            throw new IllegalArgumentException("mapping entry requires at least one value");
        this.values = new ArrayList<>(values);
        this.values.forEach(value -> Objects.requireNonNull(value, "value"));
    }

    public ASTExpression key() {
        return key;
    }

    public ASTExpression value() {
        return values.get(0);
    }

    public List<ASTExpression> values() {
        return Collections.unmodifiableList(values);
    }
}
