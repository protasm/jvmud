package io.github.protasm.jvmud.language.efun;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Language-only LPC functions, independent of a hosted mudlib's worlds and services.
 *
 * <p>The catalog retains its established {@code jvmud_} names. Text, collection and value
 * operations need only LPC values; callback forms use the neutral generated-code context for
 * invocation and the lexical receiver. Registering these functions creates no world, session,
 * clock, filesystem or database service.</p>
 */
public final class LanguageEfuns {
    private LanguageEfuns() {}

    /** Returns fresh function definitions grouped by their language responsibility. */
    public static List<Efun> functions() {
        List<Efun> functions = new ArrayList<>();
        functions.addAll(TextEfuns.functions());
        functions.addAll(RegularExpressionEfuns.functions());
        functions.addAll(CollectionEfuns.functions());
        functions.addAll(ValueEfuns.functions());
        return List.copyOf(functions);
    }

    /** Registers language declarations and implementations in the supplied host-neutral registry. */
    public static void register(EfunRegistry registry) {
        Objects.requireNonNull(registry, "registry");
        functions().forEach(registry::register);
    }
}
