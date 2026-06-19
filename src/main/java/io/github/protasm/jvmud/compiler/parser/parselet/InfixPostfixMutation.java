package io.github.protasm.jvmud.compiler.parser.parselet;

import static io.github.protasm.jvmud.compiler.token.TokenType.T_PLUS_PLUS;

import io.github.protasm.jvmud.compiler.parser.ParseException;
import io.github.protasm.jvmud.compiler.parser.Parser;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayAccess;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayMutation;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedIdentifier;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedMutation;

/** Parses postfix increment/decrement expressions after a mutable target. */
public class InfixPostfixMutation implements InfixParselet {
    @Override
    public ASTExpression parse(Parser parser, ASTExpression left, boolean canAssign) {
        int delta = parser.tokens().previous().type() == T_PLUS_PLUS ? 1 : -1;
        int line = left.line();

        if (left instanceof ASTExprUnresolvedIdentifier identifier)
            return new ASTExprUnresolvedMutation(line, identifier.name(), delta, false);
        if (left instanceof ASTExprArrayAccess access)
            return new ASTExprArrayMutation(line, access.target(), access.index(), delta, false);

        throw new ParseException("Invalid postfix increment target.", parser.tokens().previous());
    }
}
