package io.github.protasm.jvmud.language.compiler.parser.parselet;

import io.github.protasm.jvmud.language.compiler.parser.Parser;
import io.github.protasm.jvmud.language.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.language.compiler.parser.ast.expr.ASTExprLiteralFalse;
import io.github.protasm.jvmud.language.compiler.parser.ast.expr.ASTExprLiteralTrue;

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
