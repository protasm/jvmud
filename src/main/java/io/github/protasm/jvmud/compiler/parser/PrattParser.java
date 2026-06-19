package io.github.protasm.jvmud.compiler.parser;

import static io.github.protasm.jvmud.compiler.parser.PrattParser.Precedence.PREC_AND;
import static io.github.protasm.jvmud.compiler.parser.PrattParser.Precedence.PREC_BIT_AND;
import static io.github.protasm.jvmud.compiler.parser.PrattParser.Precedence.PREC_BIT_OR;
import static io.github.protasm.jvmud.compiler.parser.PrattParser.Precedence.PREC_BIT_XOR;
import static io.github.protasm.jvmud.compiler.parser.PrattParser.Precedence.PREC_COMPARISON;
import static io.github.protasm.jvmud.compiler.parser.PrattParser.Precedence.PREC_EQUALITY;
import static io.github.protasm.jvmud.compiler.parser.PrattParser.Precedence.PREC_FACTOR;
import static io.github.protasm.jvmud.compiler.parser.PrattParser.Precedence.PREC_NONE;
import static io.github.protasm.jvmud.compiler.parser.PrattParser.Precedence.PREC_OR;
import static io.github.protasm.jvmud.compiler.parser.PrattParser.Precedence.PREC_SHIFT;
import static io.github.protasm.jvmud.compiler.parser.PrattParser.Precedence.PREC_TERM;
import static io.github.protasm.jvmud.compiler.parser.PrattParser.Precedence.PREC_TERNARY;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_AMP;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_APOSTROPHE;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_BANG;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_BANG_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_CARET;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_DBL_AMP;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_DBL_PIPE;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_DOLLAR;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_EQUAL_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_FALSE;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_FLOAT_LITERAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_GREATER;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_GREATER_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_GREATER_GREATER;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_HASH;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_IDENTIFIER;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_INT_LITERAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_LEFT_BRACE;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_LEFT_BRACKET;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_LEFT_PAREN;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_LESS;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_LESS_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_LESS_LESS;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_MINUS;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_PERCENT;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_PLUS;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_PIPE;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_QUESTION;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_RIGHT_ARROW;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_SLASH;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_SUPER;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_STAR;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_STRING_LITERAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_TRUE;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_TILDE;

import java.util.HashMap;
import java.util.Map;

import io.github.protasm.jvmud.compiler.parser.parselet.InfixBinaryOp;
import io.github.protasm.jvmud.compiler.parser.parselet.InfixIndex;
import io.github.protasm.jvmud.compiler.parser.parselet.InfixInvoke;
import io.github.protasm.jvmud.compiler.parser.parselet.InfixQualifiedCall;
import io.github.protasm.jvmud.compiler.parser.parselet.InfixTernary;
import io.github.protasm.jvmud.compiler.parser.parselet.PrefixIdentifier;
import io.github.protasm.jvmud.compiler.parser.parselet.PrefixLParen;
import io.github.protasm.jvmud.compiler.parser.parselet.PrefixLiteral;
import io.github.protasm.jvmud.compiler.parser.parselet.PrefixArrayLiteral;
import io.github.protasm.jvmud.compiler.parser.parselet.PrefixClosureArgument;
import io.github.protasm.jvmud.compiler.parser.parselet.PrefixNumber;
import io.github.protasm.jvmud.compiler.parser.parselet.PrefixQuotedReference;
import io.github.protasm.jvmud.compiler.parser.parselet.PrefixString;
import io.github.protasm.jvmud.compiler.parser.parselet.PrefixSuperCall;
import io.github.protasm.jvmud.compiler.parser.parselet.PrefixUnaryOp;
import io.github.protasm.jvmud.compiler.token.Token;
import io.github.protasm.jvmud.compiler.token.TokenType;

public class PrattParser {
    public static final class Precedence {
        public static final int PREC_NONE = 0;
        public static final int PREC_ASSIGNMENT = 1; // =
        public static final int PREC_TERNARY = 2; // ?:
        public static final int PREC_OR = 3; // or
        public static final int PREC_AND = 4; // and
        public static final int PREC_BIT_OR = 5; // |
        public static final int PREC_BIT_XOR = 6; // ^
        public static final int PREC_BIT_AND = 7; // &
        public static final int PREC_EQUALITY = 8; // == !=
        public static final int PREC_COMPARISON = 9; // < > <= >=
        public static final int PREC_SHIFT = 10; // << >>
        public static final int PREC_TERM = 11; // + -
        public static final int PREC_FACTOR = 12; // * /
        public static final int PREC_UNARY = 13; // ! -
        public static final int PREC_CALL = 14; // ()
        public static final int PREC_PRIMARY = 15;

        // Precedence()
        private Precedence() {
        }
    }

    private static final Map<TokenType, ParseRule> tokenTypeToRule;

    static {
        tokenTypeToRule = new HashMap<>();

        tokenTypeToRule.put(T_LEFT_PAREN, new ParseRule(new PrefixLParen(), null, PREC_NONE));
        tokenTypeToRule.put(T_LEFT_BRACE, new ParseRule(new PrefixArrayLiteral(), null, PREC_NONE));
        tokenTypeToRule.put(T_LEFT_BRACKET, new ParseRule(null, new InfixIndex(), Precedence.PREC_CALL));
        tokenTypeToRule.put(T_RIGHT_ARROW, new ParseRule(null, new InfixInvoke(), Precedence.PREC_CALL));

        tokenTypeToRule.put(T_APOSTROPHE, new ParseRule(new PrefixQuotedReference(false), null, PREC_NONE));
        tokenTypeToRule.put(T_HASH, new ParseRule(new PrefixQuotedReference(true), null, PREC_NONE));
        tokenTypeToRule.put(T_DOLLAR, new ParseRule(new PrefixClosureArgument(), null, PREC_NONE));

        tokenTypeToRule.put(T_BANG, new ParseRule(new PrefixUnaryOp(), null, PREC_NONE));
        tokenTypeToRule.put(T_TILDE, new ParseRule(new PrefixUnaryOp(), null, PREC_NONE));

        tokenTypeToRule.put(T_MINUS, new ParseRule(new PrefixUnaryOp(), new InfixBinaryOp(), PREC_TERM));
        tokenTypeToRule.put(T_PLUS, new ParseRule(null, new InfixBinaryOp(), PREC_TERM));
        tokenTypeToRule.put(T_STAR, new ParseRule(null, new InfixBinaryOp(), PREC_FACTOR));
        tokenTypeToRule.put(T_SLASH, new ParseRule(null, new InfixBinaryOp(), PREC_FACTOR));
        tokenTypeToRule.put(T_PERCENT, new ParseRule(null, new InfixBinaryOp(), PREC_FACTOR));

        tokenTypeToRule.put(T_DBL_PIPE, new ParseRule(null, new InfixBinaryOp(), PREC_OR));
        tokenTypeToRule.put(T_DBL_AMP, new ParseRule(null, new InfixBinaryOp(), PREC_AND));
        tokenTypeToRule.put(T_PIPE, new ParseRule(null, new InfixBinaryOp(), PREC_BIT_OR));
        tokenTypeToRule.put(T_CARET, new ParseRule(null, new InfixBinaryOp(), PREC_BIT_XOR));
        tokenTypeToRule.put(T_AMP, new ParseRule(null, new InfixBinaryOp(), PREC_BIT_AND));

        tokenTypeToRule.put(T_GREATER, new ParseRule(null, new InfixBinaryOp(), PREC_COMPARISON));
        tokenTypeToRule.put(T_GREATER_EQUAL, new ParseRule(null, new InfixBinaryOp(), PREC_COMPARISON));
        tokenTypeToRule.put(T_LESS, new ParseRule(null, new InfixBinaryOp(), PREC_COMPARISON));
        tokenTypeToRule.put(T_LESS_EQUAL, new ParseRule(null, new InfixBinaryOp(), PREC_COMPARISON));
        tokenTypeToRule.put(T_LESS_LESS, new ParseRule(null, new InfixBinaryOp(), PREC_SHIFT));
        tokenTypeToRule.put(T_GREATER_GREATER, new ParseRule(null, new InfixBinaryOp(), PREC_SHIFT));

        tokenTypeToRule.put(T_BANG_EQUAL, new ParseRule(null, new InfixBinaryOp(), PREC_EQUALITY));
        tokenTypeToRule.put(T_EQUAL_EQUAL, new ParseRule(null, new InfixBinaryOp(), PREC_EQUALITY));

        tokenTypeToRule.put(T_QUESTION, new ParseRule(null, new InfixTernary(), PREC_TERNARY));

        tokenTypeToRule.put(T_FALSE, new ParseRule(new PrefixLiteral(), null, PREC_NONE));
        tokenTypeToRule.put(T_IDENTIFIER, new ParseRule(new PrefixIdentifier(), null, PREC_NONE));
        tokenTypeToRule.put(T_INT_LITERAL, new ParseRule(new PrefixNumber(), null, PREC_NONE));
        tokenTypeToRule.put(T_FLOAT_LITERAL, new ParseRule(new PrefixNumber(), null, PREC_NONE));
        tokenTypeToRule.put(T_STRING_LITERAL, new ParseRule(new PrefixString(), null, PREC_NONE));
        tokenTypeToRule.put(T_TRUE, new ParseRule(new PrefixLiteral(), null, PREC_NONE));
        tokenTypeToRule.put(T_SUPER, new ParseRule(new PrefixSuperCall(), new InfixQualifiedCall(), Precedence.PREC_CALL));

    }

    private PrattParser() {
    }

    public static ParseRule getRule(Token<?> token) {
        ParseRule rule = tokenTypeToRule.get(token.type());

        return (rule != null ? rule : new ParseRule(null, null, PREC_NONE));
    }
}
