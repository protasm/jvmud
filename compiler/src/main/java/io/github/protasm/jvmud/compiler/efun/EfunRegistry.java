package io.github.protasm.jvmud.compiler.efun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Instance-scoped registry for LPC-facing engine functions.
 *
 * <p>The registry is owned by a runtime context rather than by global process state, so each hosted
 * mudlib/runtime can choose which efuns it exposes. Built-in JVMud efuns are registered from
 * {@code CoreEfuns}; embedders and tests may register additional implementations before compiled
 * LPC code executes.</p>
 *
 * <p>Lookup is by function name and exact arity. Multiple arities for the same name are valid and
 * are how overloaded efuns are represented. Registering two implementations with the same name and
 * arity is allowed at insertion time but treated as a configuration error when that overload is
 * looked up, which prevents the runtime from silently invoking the wrong function.</p>
 */
public final class EfunRegistry {
    private final Map<String, List<Efun>> registry = new HashMap<>();

    /**
     * Registers an engine function implementation.
     *
     * @param efun implementation to add to this registry
     * @throws NullPointerException if {@code efun} is {@code null}
     */
    public void register(Efun efun) {
        Objects.requireNonNull(efun, "efun");
        registry.computeIfAbsent(efun.signature().name(), k -> new ArrayList<>()).add(efun);
    }

    /**
     * Returns the matching function for name and arity.
     *
     * @param name LPC-facing function name
     * @param arity exact number of LPC arguments in the call
     * @return matching efun, or {@code null} when none is registered
     * @throws IllegalStateException if more than one implementation matches the same name and arity
     */
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

    /**
     * Returns all registered signatures for a function name.
     *
     * <p>This is useful for diagnostics and compiler error messages that need to show the overloads
     * available for an unresolved or incorrectly-called efun.</p>
     *
     * @param name LPC-facing function name
     * @return immutable list of signatures currently registered for {@code name}
     */
    public List<EfunSignature> signatures(String name) {
        return registry.getOrDefault(name, List.of()).stream().map(Efun::signature).toList();
    }
}
