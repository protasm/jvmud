package io.github.protasm.jvmud.language.compiler.parser.parselet;

import io.github.protasm.jvmud.language.compiler.parser.Parser;
import io.github.protasm.jvmud.language.compiler.parser.ast.ASTExpression;

public interface PrefixParselet {
    ASTExpression parse(Parser parser, boolean canAssign);
}
