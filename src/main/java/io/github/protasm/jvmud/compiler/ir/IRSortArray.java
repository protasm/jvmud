package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.List;
import java.util.Objects;

/**
 * IR for a targeted {@code sort_array(values, (: ... :))} lowering.
 *
 * <p>This node keeps the sort loop explicit while delegating comparator execution to JVMud's
 * first-class callable runtime path.</p>
 *
 * @param line source line for diagnostics
 * @param source value to copy and sort
 * @param comparator callable value used to compare candidate elements
 * @param itemsLocal synthetic local containing the mutable list being sorted
 * @param indexLocal synthetic outer loop index
 * @param innerIndexLocal synthetic inner loop index
 * @param swapLocal synthetic temporary used while swapping elements
 * @param extraArguments comparator context values bound to callback slots starting at {@code $3}
 * @param type runtime result type
 */
public record IRSortArray(
        int line,
        IRExpression source,
        IRExpression comparator,
        IRLocal itemsLocal,
        IRLocal indexLocal,
        IRLocal innerIndexLocal,
        IRLocal swapLocal,
        List<IRExpression> extraArguments,
        RuntimeType type)
        implements IRExpression {
    public IRSortArray {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(comparator, "comparator");
        Objects.requireNonNull(itemsLocal, "itemsLocal");
        Objects.requireNonNull(indexLocal, "indexLocal");
        Objects.requireNonNull(innerIndexLocal, "innerIndexLocal");
        Objects.requireNonNull(swapLocal, "swapLocal");
        extraArguments = List.copyOf(extraArguments);
        Objects.requireNonNull(type, "type");
    }
}
