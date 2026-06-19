package io.github.protasm.jvmud.compiler.parser.parselet;

import io.github.protasm.jvmud.compiler.parser.Parser;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLiteralFalse;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLiteralTrue;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;

public class PrefixLiteral implements PrefixParselet {
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
        switch (parser.tokens().previous().type()) {
        case T_TRUE:
            return new ASTExprLiteralTrue(parser.currLine());
        case T_FALSE:
            return new ASTExprLiteralFalse(parser.currLine());
        default:
            return null; // TODO
        }
    }
}
