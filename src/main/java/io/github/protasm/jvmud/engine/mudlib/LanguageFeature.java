package io.github.protasm.jvmud.engine.mudlib;

/** Optional LPC syntax families that a mudlib profile may request explicitly. */
public enum LanguageFeature {
    /** {@code catch(...)}-style protected evaluation expressions. */
    PROTECTED_EVALUATION,
    /** Typed statement-bodied function literals. */
    TYPED_FUNCTION_LITERALS,
    /** {@code (: ... :)} inline callable literals. */
    INLINE_CALLABLES,
    /** Multiple semicolon-separated values per mapping key and indexed value slots. */
    MULTI_VALUE_MAPPINGS,
    /** {@code varargs} method and parameter modifiers. */
    VARARGS
}
