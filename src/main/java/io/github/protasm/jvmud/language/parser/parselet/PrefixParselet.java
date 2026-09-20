package io.github.protasm.jvmud.language.parser.parselet;

import io.github.protasm.jvmud.language.parser.Parser;
import io.github.protasm.jvmud.language.parser.ast.ASTExpression;

public interface PrefixParselet {
    ASTExpression parse(Parser parser, boolean canAssign);
}
