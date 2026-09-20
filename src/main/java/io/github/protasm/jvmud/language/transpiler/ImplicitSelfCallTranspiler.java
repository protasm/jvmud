package io.github.protasm.jvmud.language.transpiler;

import io.github.protasm.jvmud.language.parser.ast.ASTArguments;
import io.github.protasm.jvmud.language.parser.ast.ASTExpression;
import io.github.protasm.jvmud.language.parser.ast.expr.*;
import io.github.protasm.jvmud.language.runtime.RuntimeContext;

/**
 * Translates an unknown bare call to an ordinary dynamic invocation on the current LPC object.
 * Unlike declaration normalization this transformation runs during name resolution, after inherited
 * methods, global helpers and engine functions are known. The caller must establish that the name
 * is undeclared and that the independent manifest switch is enabled. No prototype or implementation
 * is invented: results are mixed and missing methods fail at execution rather than silently returning zero.
 */
public final class ImplicitSelfCallTranspiler {
    /** Preserves the call's source line and already-resolved argument expressions. */
    public ASTExpression transpile(ASTExprUnresolvedCall call, ASTArguments arguments, RuntimeContext context) {
        var self = context.resolveEngineEfun("jvmud_current_lpc_object", 0);
        if (self == null)
            throw new IllegalArgumentException("Implicit self calls require jvmud_current_lpc_object");
        return new ASTExprDynamicInvoke(call.line(),
                new ASTExprCallEfun(call.line(), self, new ASTArguments(call.line())), call.name(), arguments, true);
    }
}
