package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;

/**
 * Compiler recovery expression inserted after semantic analysis has already reported an error.
 *
 * <p>This is not an LPC source-language value. It lets later analysis keep walking the tree without
 * inventing a JVMud-native {@code null} or {@code nil} concept.</p>
 */
public final class ASTExprError extends ASTExpression {
    public ASTExprError(int line) {
        super(line);
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCERROR;
    }
}
