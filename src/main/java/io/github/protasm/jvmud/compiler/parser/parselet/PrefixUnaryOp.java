package io.github.protasm.jvmud.compiler.parser.parselet;

import static io.github.protasm.jvmud.compiler.parser.type.UnaryOpType.UOP_BIT_NOT;
import static io.github.protasm.jvmud.compiler.parser.type.UnaryOpType.UOP_NEGATE;
import static io.github.protasm.jvmud.compiler.parser.type.UnaryOpType.UOP_NOT;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_MINUS_MINUS;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_PLUS_PLUS;

import io.github.protasm.jvmud.compiler.parser.Parser;
import io.github.protasm.jvmud.compiler.parser.ParseException;
import io.github.protasm.jvmud.compiler.parser.PrattParser;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayAccess;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayMutation;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprOpUnary;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedIdentifier;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedMutation;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.token.TokenType;

/** Parses prefix unary operators, including mutating increment and decrement targets. */
public class PrefixUnaryOp implements PrefixParselet {
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
        int line = parser.currLine();
        TokenType opType = parser.tokens().previous().type();
        ASTExpression expr = parser.parsePrecedence(PrattParser.Precedence.PREC_UNARY);

        switch (opType) {
        case T_PLUS_PLUS:
            return prefixMutation(parser, line, expr, 1);
        case T_MINUS_MINUS:
            return prefixMutation(parser, line, expr, -1);
        case T_BANG:
            return new ASTExprOpUnary(line, expr, UOP_NOT);
        case T_MINUS:
            return new ASTExprOpUnary(line, expr, UOP_NEGATE);
        case T_TILDE:
            return new ASTExprOpUnary(line, expr, UOP_BIT_NOT);
        default:
            return null; // TODO throw exception
        }
    }

    /** Builds a prefix mutation expression for local and indexed assignment targets. */
    private ASTExpression prefixMutation(Parser parser, int line, ASTExpression target, int delta) {
        if (target instanceof ASTExprUnresolvedIdentifier identifier)
            return new ASTExprUnresolvedMutation(line, identifier.name(), delta, true);
        if (target instanceof ASTExprArrayAccess access)
            return new ASTExprArrayMutation(line, access.target(), access.index(), delta, true);
        throw new ParseException("Invalid prefix increment target.", parser.tokens().previous());
    }
}
