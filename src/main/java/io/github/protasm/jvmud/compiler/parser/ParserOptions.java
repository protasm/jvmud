package io.github.protasm.jvmud.compiler.parser;

import io.github.protasm.jvmud.engine.mudlib.LanguageFeature;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/** Immutable, explicitly selectable optional LPC syntax features. */
public final class ParserOptions {
    private final Set<LanguageFeature> features;

    private ParserOptions(Set<LanguageFeature> features) {
        this.features = Set.copyOf(features);
    }

    /**
     * Returns the complete compiler-development profile.
     *
     * <p>Hosted mudlibs do not use this implicit profile; boot replaces it with the features
     * declared by the selected mudlib manifest.</p>
     */
    public static ParserOptions defaults() {
        return new ParserOptions(EnumSet.allOf(LanguageFeature.class));
    }

    /** Returns a profile containing only the supplied optional syntax features. */
    public static ParserOptions features(Set<LanguageFeature> features) {
        return new ParserOptions(Objects.requireNonNull(features, "features"));
    }

    /** Returns whether the profile enables an optional syntax family. */
    public boolean supports(LanguageFeature feature) {
        return features.contains(Objects.requireNonNull(feature, "feature"));
    }

    /** Returns the immutable enabled feature set. */
    public Set<LanguageFeature> features() {
        return features;
    }
}
