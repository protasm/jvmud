package io.github.protasm.jvmud.compiler.parser.ast.stmt;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.ASTLocal;
import io.github.protasm.jvmud.compiler.parser.ast.ASTStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ASTStmtFor extends ASTStatement {
    private final List<ASTLocal> initializerLocals;
    private final ASTExpression initializer;
    private final ASTExpression condition;
    private final ASTExpression update;
    private final ASTStatement body;

    public ASTStmtFor(
            int line, ASTExpression initializer, ASTExpression condition, ASTExpression update, ASTStatement body) {
        this(line, List.of(), initializer, condition, update, body);
    }

    public ASTStmtFor(
            int line,
            List<ASTLocal> initializerLocals,
            ASTExpression initializer,
            ASTExpression condition,
            ASTExpression update,
            ASTStatement body) {
        super(line);
        this.initializerLocals =
                initializerLocals != null ? new ArrayList<>(initializerLocals) : new ArrayList<>();
        this.initializer = initializer;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    public List<ASTLocal> initializerLocals() {
        return Collections.unmodifiableList(initializerLocals);
    }

    public ASTExpression initializer() {
        return initializer;
    }

    public ASTExpression condition() {
        return condition;
    }

    public ASTExpression update() {
        return update;
    }

    public ASTStatement body() {
        return body;
    }
}
