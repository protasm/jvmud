package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.ASTParameters;
import io.github.protasm.jvmud.compiler.parser.ast.Symbol;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.Objects;

/**
 * AST node for the LPC compatibility form {@code function type (params) { return expr; }}.
 *
 * <p>This is a typed anonymous callable value. JVMud initially supports the common expression-return
 * subset used by RealmsMUD callbacks; full statement-bodied callable literals can be added later
 * without changing this source-level representation.</p>
 */
public final class ASTExprTypedFunctionLiteral extends ASTExpression {
    private final Symbol returnSymbol;
    private final ASTParameters parameters;
    private final ASTExpression body;

    public ASTExprTypedFunctionLiteral(int line, Symbol returnSymbol, ASTParameters parameters, ASTExpression body) {
        super(line);
        this.returnSymbol = Objects.requireNonNull(returnSymbol, "returnSymbol");
        this.parameters = Objects.requireNonNull(parameters, "parameters");
        this.body = Objects.requireNonNull(body, "body");
    }

    public Symbol returnSymbol() {
        return returnSymbol;
    }

    public ASTParameters parameters() {
        return parameters;
    }

    public ASTExpression body() {
        return body;
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCFUNCTION;
    }
}
