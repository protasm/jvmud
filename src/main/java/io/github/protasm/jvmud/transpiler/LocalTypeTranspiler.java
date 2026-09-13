package io.github.protasm.jvmud.transpiler;

import io.github.protasm.jvmud.compiler.parser.ast.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Applies checked local overrides after parsing and before type resolution. Using parsed local
 * identities preserves grouped declarations, initializer order and nested scopes without rewriting
 * source text. Parameters and fields are excluded. Overloaded methods or repeated local names are
 * ambiguous and rejected; a rule must identify exactly one defined method and one local within it.
 */
public final class LocalTypeTranspiler {
    /** Validates all rules for this compilation unit before changing any declarations. */
    public void transpile(Path source, Path root, ASTObject object, List<LocalTypeOverride> rules) {
        if (source == null || root == null || rules.isEmpty()) return;
        Path absolute = source.toAbsolutePath().normalize();
        Path base = root.toAbsolutePath().normalize();
        if (!absolute.startsWith(base)) return;
        String file = base.relativize(absolute).toString().replace('\\', '/');
        Map<ASTLocal, String> replacements = new LinkedHashMap<>();
        for (var rule : rules) {
            if (!file.equals(rule.file())) continue;
            String label = "Local override " + file + ":" + rule.method() + ":" + rule.local();
            var methods = object.methods().getAll(rule.method()).stream().filter(m -> m.body() != null).toList();
            if (methods.size() != 1)
                throw new TranspilationException(label + " expected exactly one defined method; found " + methods.size(), null);
            var method = methods.get(0);
            Set<Symbol> parameters = Collections.newSetFromMap(new IdentityHashMap<>());
            if (method.parameters() != null)
                for (var parameter : method.parameters()) parameters.add(parameter.symbol());
            var locals = method.locals().stream()
                    .filter(l -> !parameters.contains(l.symbol()) && l.symbol().name().equals(rule.local())).toList();
            if (locals.size() != 1)
                throw new TranspilationException(label + " expected exactly one local declaration; found " + locals.size(), method.line());
            var local = locals.get(0);
            String actual = local.symbol().declaredTypeName();
            if (!rule.expectedType().equals(actual))
                throw new TranspilationException(label + " expected " + rule.expectedType() + " but found " + actual, local.line());
            if (replacements.put(local, rule.replacementType()) != null)
                throw new TranspilationException("Duplicate " + label, local.line());
        }
        replacements.forEach(ASTLocal::replaceDeclaredType);
    }
}
