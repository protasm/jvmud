package io.github.protasm.jvmud.compiler.parser.ast.stmt;

import io.github.protasm.jvmud.compiler.parser.ast.ASTStatement;

/**
 * Represents a bare semicolon statement.
 *
 * <p>LPC mudlibs commonly use this as an empty loop body, for example {@code while (poll());}.
 * Keeping it explicit lets the parser preserve the source shape while the lowerer can treat it as
 * a no-op.</p>
 */
public final class ASTStmtEmpty extends ASTStatement {
    /**
     * Creates an empty statement.
     *
     * @param line source line for diagnostics and source mapping
     */
    public ASTStmtEmpty(int line) {
        super(line);
    }
}
