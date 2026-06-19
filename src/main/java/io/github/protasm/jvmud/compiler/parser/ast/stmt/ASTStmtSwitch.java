package io.github.protasm.jvmud.compiler.parser.ast.stmt;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.ASTStatement;
import java.util.List;
import java.util.Objects;

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

    public record SwitchCase(int line, ASTExpression expression, boolean isDefault, List<ASTStatement> statements) {
        public SwitchCase {
            statements = List.copyOf(statements);
            if (!isDefault)
                Objects.requireNonNull(expression, "expression");
        }
    }
}
