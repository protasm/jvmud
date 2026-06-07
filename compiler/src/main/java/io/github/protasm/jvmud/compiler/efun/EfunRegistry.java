package io.github.protasm.jvmud.compiler.efun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Instance-scoped engine function registry supporting signature-aware lookups.
 *
 * <p>The registry allows overloads by arity. It deliberately rejects ambiguous same-name,
 * same-arity matches at lookup time so callers fail before invoking the wrong engine function.</p>
 */
public final class EfunRegistry {
    private final Map<String, List<Efun>> registry = new HashMap<>();

    /** Registers an engine function implementation. */
    public void register(Efun efun) {
        Objects.requireNonNull(efun, "efun");
        registry.computeIfAbsent(efun.signature().name(), k -> new ArrayList<>()).add(efun);
    }

    /** Returns the matching function for name and arity, or {@code null} when none is registered. */
    public Efun lookup(String name, int arity) {
        List<Efun> efuns = registry.get(name);

        if (efuns == null)
            return null;

        Efun match = null;
        for (Efun efun : efuns) {
            if (efun.arity() != arity)
                continue;

            if (match != null)
                throw new IllegalStateException(
                        "Ambiguous engine function overload for '" + name + "' with arity " + arity);

            match = efun;
        }

        return match;
    }

    /** Returns all registered signatures for a name. */
    public List<EfunSignature> signatures(String name) {
        return registry.getOrDefault(name, List.of()).stream().map(Efun::signature).toList();
    }
}
