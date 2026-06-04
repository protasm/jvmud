package io.github.protasm.jvmud.compiler.parser.parselet;

import io.github.protasm.jvmud.compiler.parser.Parser;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;

public interface PrefixParselet {
    ASTExpression parse(Parser parser, boolean canAssign);
}
