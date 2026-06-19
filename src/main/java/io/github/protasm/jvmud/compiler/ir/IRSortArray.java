package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.List;
import java.util.Objects;

/**
 * IR for a targeted {@code sort_array(values, (: ... :))} lowering.
 *
 * <p>This node keeps the callback body inline and names the synthetic locals required by bytecode
 * generation: the copied item list, loop indexes, a swap slot, and the callback argument locals
 * that represent LPC closure arguments such as {@code $1} and {@code $2}. It deliberately models
 * the compatibility operation directly instead of pretending inline callables are already
 * first-class runtime values.</p>
 *
 * @param line source line for diagnostics
 * @param source value to copy and sort
 * @param comparatorBody lowered inline comparator body
 * @param itemsLocal synthetic local containing the mutable list being sorted
 * @param indexLocal synthetic outer loop index
 * @param innerIndexLocal synthetic inner loop index
 * @param swapLocal synthetic temporary used while swapping elements
 * @param extraArguments comparator context values bound to callback slots starting at {@code $3}
 * @param comparatorArgumentLocals synthetic locals bound to {@code $1}, {@code $2}, and any
 *     additional referenced callback slots
 * @param type runtime result type
 */
public record IRSortArray(
        int line,
        IRExpression source,
        IRExpression comparatorBody,
        IRLocal itemsLocal,
        IRLocal indexLocal,
        IRLocal innerIndexLocal,
        IRLocal swapLocal,
        List<IRExpression> extraArguments,
        List<IRLocal> comparatorArgumentLocals,
        RuntimeType type)
        implements IRExpression {
    public IRSortArray {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(comparatorBody, "comparatorBody");
        Objects.requireNonNull(itemsLocal, "itemsLocal");
        Objects.requireNonNull(indexLocal, "indexLocal");
        Objects.requireNonNull(innerIndexLocal, "innerIndexLocal");
        Objects.requireNonNull(swapLocal, "swapLocal");
        extraArguments = List.copyOf(extraArguments);
        comparatorArgumentLocals = List.copyOf(comparatorArgumentLocals);
        Objects.requireNonNull(type, "type");
        if (comparatorArgumentLocals.size() < 2)
            throw new IllegalArgumentException("sort comparator requires at least two argument locals");
    }
}
