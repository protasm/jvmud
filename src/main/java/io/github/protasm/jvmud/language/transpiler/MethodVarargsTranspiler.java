package io.github.protasm.jvmud.language.transpiler;

import io.github.protasm.jvmud.language.parser.ast.*;
import java.nio.file.Path;
import java.util.*;

/** Adds varargs only to explicitly selected definitions, without changing source or parameters. */
public final class MethodVarargsTranspiler {
    /** Validates every applicable rule before mutation; missing, ambiguous or already varargs targets fail. */
    public void transpile(Path source, Path root, ASTObject object, List<MethodVarargsOverride> rules) {
        if (source == null || root == null || rules.isEmpty()) return;
        Path absolute = source.toAbsolutePath().normalize();
        Path base = root.toAbsolutePath().normalize();
        if (!absolute.startsWith(base)) return;
        String file = base.relativize(absolute).toString().replace('\\', '/');
        Set<ASTMethod> selected = new LinkedHashSet<>();
        for (var rule : rules) {
            if (!file.equals(rule.file())) continue;
            String label = "Method varargs override " + file + ":" + rule.method();
            var methods = object.methods().getAll(rule.method());
            if (methods.size() != 1 || methods.get(0).body() == null)
                throw new TranspilationException(label + " requires exactly one defined method", null);
            var method = methods.get(0);
            int count = method.parameters() == null ? 0 : method.parameters().size();
            if (count != rule.expectedParameterCount() || method.modifiers().isVarargs())
                throw new TranspilationException(label + " expected a non-varargs method with "
                        + rule.expectedParameterCount() + " parameters", method.line());
            if (!selected.add(method))
                throw new TranspilationException("Duplicate " + label, method.line());
        }
        selected.forEach(ASTMethod::enableVarargs);
    }
}
