package io.github.protasm.jvmud.compiler.parser.parselet;

import static io.github.protasm.jvmud.compiler.token.TokenType.T_COMMA;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_RIGHT_BRACE;

import io.github.protasm.jvmud.compiler.parser.Parser;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayLiteral;
import java.util.ArrayList;
import java.util.List;

public class PrefixArrayLiteral implements PrefixParselet {
    /** Parses an LPC array literal, including an optional trailing comma. */
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
        List<ASTExpression> elements = new ArrayList<>();
        int line = parser.currLine();

        if (!parser.tokens().check(T_RIGHT_BRACE)) {
            while (true) {
                elements.add(parser.expression());
                if (!parser.tokens().match(T_COMMA)) {
                    break;
                }
                if (parser.tokens().check(T_RIGHT_BRACE)) {
                    break;
                }
            }
        }

        parser.tokens().consume(T_RIGHT_BRACE, "Expect '}' after array literal.");

        return new ASTExprArrayLiteral(line, elements);
    }
}
