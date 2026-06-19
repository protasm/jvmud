package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.List;
import java.util.Objects;

/**
 * AST node for the JVMud compatibility form {@code sort_array(values, (: ... :))}.
 *
 * <p>The parser first sees this as an ordinary function call. Semantic resolution rewrites the
 * call to this node when the second argument is an inline callable, allowing the lowerer to bind
 * {@code $1}, {@code $2}, and any additional referenced callback arguments without introducing a
 * general first-class closure object model.</p>
 */
public final class ASTExprSortArray extends ASTExpression {
    private final ASTExpression source;
    private final ASTExprInlineCallable comparator;
    private final List<ASTExpression> extraArguments;

    /**
     * Creates a sort-array compatibility expression.
     *
     * @param line source line for diagnostics
     * @param source expression that produces the array or iterable value to sort
     * @param comparator inline callable whose body decides whether the two current values should
     *     be swapped
     */
    public ASTExprSortArray(int line, ASTExpression source, ASTExprInlineCallable comparator) {
        this(line, source, comparator, List.of());
    }

    /**
     * Creates a sort-array compatibility expression with additional comparator context.
     *
     * @param line source line for diagnostics
     * @param source expression that produces the array or iterable value to sort
     * @param comparator inline callable whose body decides whether the two current values should
     *     be swapped
     * @param extraArguments values bound to callback slots starting at {@code $3}
     */
    public ASTExprSortArray(
            int line, ASTExpression source, ASTExprInlineCallable comparator, List<ASTExpression> extraArguments) {
        super(line);
        this.source = Objects.requireNonNull(source, "source");
        this.comparator = Objects.requireNonNull(comparator, "comparator");
        this.extraArguments = List.copyOf(extraArguments);
    }

    /** Returns the expression that produces the values to sort. */
    public ASTExpression source() {
        return source;
    }

    /** Returns the inline comparator callback supplied as the second argument. */
    public ASTExprInlineCallable comparator() {
        return comparator;
    }

    /** Returns comparator context arguments bound to closure slots starting at {@code $3}. */
    public List<ASTExpression> extraArguments() {
        return extraArguments;
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCARRAY;
    }
}
