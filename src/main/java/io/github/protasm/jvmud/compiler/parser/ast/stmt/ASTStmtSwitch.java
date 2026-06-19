package io.github.protasm.jvmud.compiler.parser.ast.stmt;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.ASTStatement;
import java.util.List;
import java.util.Objects;

/**
 * AST node for an LPC {@code switch} statement.
 *
 * <p>Case labels may be either exact values, such as {@code case 3:}, or inclusive ranges,
 * such as {@code case 1..2:}. The range form is part of common LPC switch syntax and is kept
 * explicit here so lowering can preserve fallthrough and default handling without rewriting the
 * source-level shape.</p>
 */
public final class ASTStmtSwitch extends ASTStatement {
    private final ASTExpression expression;
    private final List<SwitchCase> cases;

    public ASTStmtSwitch(int line, ASTExpression expression, List<SwitchCase> cases) {
        super(line);
        this.expression = Objects.requireNonNull(expression, "expression");
        this.cases = List.copyOf(cases);
    }

    public ASTExpression expression() {
        return expression;
    }

    public List<SwitchCase> cases() {
        return cases;
    }

    /**
     * One {@code case} or {@code default} label and the statements owned by that label.
     *
     * @param expression the exact case value or inclusive range start; {@code null} for default
     * @param rangeEndExpression the inclusive range end for {@code case start..end:}, otherwise
     *        {@code null}
     */
    public record SwitchCase(
            int line,
            ASTExpression expression,
            ASTExpression rangeEndExpression,
            boolean isDefault,
            List<ASTStatement> statements) {
        public SwitchCase {
            statements = List.copyOf(statements);
            if (!isDefault) {
                Objects.requireNonNull(expression, "expression");
            } else if (rangeEndExpression != null) {
                throw new IllegalArgumentException("default case cannot have a range end expression");
            }
        }

        public SwitchCase(int line, ASTExpression expression, boolean isDefault, List<ASTStatement> statements) {
            this(line, expression, null, isDefault, statements);
        }

        public boolean isRange() {
            return rangeEndExpression != null;
        }
    }
}
