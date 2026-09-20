package io.github.protasm.jvmud.language.parser.parselet;

import io.github.protasm.jvmud.language.parser.Parser;
import io.github.protasm.jvmud.language.parser.ast.ASTExpression;

public interface InfixParselet {
    ASTExpression parse(Parser parser, ASTExpression left, boolean canAssign);
}
