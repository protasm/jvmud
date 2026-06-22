package io.github.protasm.jvmud.compiler.exec;

import io.github.protasm.jvmud.compiler.preproc.IncludeResolver;
import io.github.protasm.jvmud.compiler.preproc.SearchPathIncludeResolver;
import io.github.protasm.jvmud.compiler.pipeline.CompilationObserver;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Configuration options for the host-facing LPC execution runtime.
 *
 * <p>The base include path is the mudlib root for host calls that use mudlib-style paths. Include
 * search paths and custom resolvers control preprocessor and inherited-source lookup. Class loading
 * options control the JVM parent visible to generated LPC classes.</p>
 */
public final class LPCRuntimeConfig {
    private final Path baseIncludePath;
    private final List<Path> includeSearchPaths;
    private final String parentInternalName;
    private final ClassLoader parentClassLoader;
    private final IncludeResolver includeResolver;
    private final CompilationObserver compilationObserver;
    private final LPCObjectLoadObserver objectLoadObserver;

    private LPCRuntimeConfig(Builder builder) {
        this.baseIncludePath = (builder.baseIncludePath != null)
                ? builder.baseIncludePath.toAbsolutePath().normalize()
                : null;
        this.includeSearchPaths = List.copyOf(builder.includeSearchPaths);
        this.parentInternalName = Objects.requireNonNull(builder.parentInternalName, "parentInternalName");
        this.parentClassLoader = Objects.requireNonNull(builder.parentClassLoader, "parentClassLoader");
        this.includeResolver = builder.includeResolver;
        this.compilationObserver = Objects.requireNonNull(builder.compilationObserver, "compilationObserver");
        this.objectLoadObserver = Objects.requireNonNull(builder.objectLoadObserver, "objectLoadObserver");
    }

    /** Starts a builder with local-development defaults. */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the normalized mudlib root used to resolve host-supplied LPC paths. */
    public Path baseIncludePath() {
        return baseIncludePath;
    }

    /** Returns additional include search paths used by the default include resolver. */
    public List<Path> includeSearchPaths() {
        return includeSearchPaths;
    }

    /** Returns the JVM internal superclass name used for generated LPC objects. */
    public String parentInternalName() {
        return parentInternalName;
    }

    /** Returns the parent class loader for generated LPC classes. */
    public ClassLoader parentClassLoader() {
        return parentClassLoader;
    }

    /** Returns the explicitly supplied include resolver, if any. */
    public IncludeResolver includeResolver() {
        return includeResolver;
    }

    /** Returns the observer used for compiler stage progress. */
    public CompilationObserver compilationObserver() {
        return compilationObserver;
    }

    /** Returns the observer used for host-side LPC object load diagnostics. */
    public LPCObjectLoadObserver objectLoadObserver() {
        return objectLoadObserver;
    }

    IncludeResolver resolveIncludeResolver() {
        if (includeResolver != null) {
            return includeResolver;
        }

        return new SearchPathIncludeResolver(baseIncludePath, includeSearchPaths);
    }

    public static final class Builder {
        private Path baseIncludePath = Path.of(".");
        private List<Path> includeSearchPaths = List.of();
        private String parentInternalName = "java/lang/Object";
        private ClassLoader parentClassLoader = LPCRuntimeConfig.class.getClassLoader();
        private IncludeResolver includeResolver;
        private CompilationObserver compilationObserver = CompilationObserver.NONE;
        private LPCObjectLoadObserver objectLoadObserver = LPCObjectLoadObserver.NONE;

        /** Sets the mudlib root used to resolve host-supplied source paths. */
        public Builder baseIncludePath(Path baseIncludePath) {
            this.baseIncludePath = baseIncludePath;
            return this;
        }

        /** Sets additional include search paths for the default include resolver. */
        public Builder includeSearchPaths(List<Path> includeSearchPaths) {
            this.includeSearchPaths = (includeSearchPaths != null) ? includeSearchPaths : List.of();
            return this;
        }

        /** Sets the JVM internal superclass name for generated LPC classes. */
        public Builder parentInternalName(String parentInternalName) {
            if (parentInternalName != null) {
                this.parentInternalName = parentInternalName;
            }
            return this;
        }

        /** Sets the parent class loader visible to generated LPC classes. */
        public Builder parentClassLoader(ClassLoader parentClassLoader) {
            if (parentClassLoader != null) {
                this.parentClassLoader = parentClassLoader;
            }
            return this;
        }

        /** Supplies a custom include resolver. */
        public Builder includeResolver(IncludeResolver includeResolver) {
            this.includeResolver = includeResolver;
            return this;
        }

        /** Supplies a compiler stage observer. */
        public Builder compilationObserver(CompilationObserver compilationObserver) {
            if (compilationObserver != null) {
                this.compilationObserver = compilationObserver;
            }
            return this;
        }

        /** Supplies an observer for host-side LPC object load attempts. */
        public Builder objectLoadObserver(LPCObjectLoadObserver objectLoadObserver) {
            if (objectLoadObserver != null) {
                this.objectLoadObserver = objectLoadObserver;
            }
            return this;
        }

        /** Builds an immutable runtime configuration. */
        public LPCRuntimeConfig build() {
            return new LPCRuntimeConfig(this);
        }
    }
}
