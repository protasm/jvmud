package io.github.protasm.jvmud.language.compiler.transpiler;

import io.github.protasm.jvmud.language.compiler.LPCCompilationEnvironment;
import io.github.protasm.jvmud.language.compiler.parser.ast.ASTArguments;
import io.github.protasm.jvmud.language.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.language.compiler.parser.ast.expr.ASTExprCallEfun;
import io.github.protasm.jvmud.language.compiler.parser.ast.expr.ASTExprDynamicInvoke;
import io.github.protasm.jvmud.language.compiler.parser.ast.expr.ASTExprUnresolvedCall;

/**
 * Translates an unknown bare call to an ordinary dynamic invocation on the current LPC object.
 * Unlike declaration normalization this transformation runs during name resolution, after inherited
 * methods, global helpers and engine functions are known. The caller must establish that the name
 * is undeclared and that the independent manifest switch is enabled. No prototype or implementation
 * is invented: results are mixed and missing methods fail at execution rather than silently returning zero.
 */
public final class ImplicitSelfCallTranspiler {
    /** Preserves the call's source line and already-resolved argument expressions. */
    public ASTExpression transpile(ASTExprUnresolvedCall call, ASTArguments arguments, LPCCompilationEnvironment context) {
        var self = context.resolveEngineFunctionSignature("jvmud_current_lpc_object", 0);
        if (self == null)
            throw new IllegalArgumentException("Implicit self calls require jvmud_current_lpc_object");
        return new ASTExprDynamicInvoke(call.line(),
                new ASTExprCallEfun(call.line(), self, new ASTArguments(call.line())), call.name(), arguments, true);
    }
}
