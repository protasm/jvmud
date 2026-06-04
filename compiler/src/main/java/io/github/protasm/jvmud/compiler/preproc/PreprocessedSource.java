package io.github.protasm.jvmud.compiler.preproc;

import io.github.protasm.jvmud.compiler.sourcepos.SourceMapper;
import java.util.Objects;

/** Result of preprocessing: expanded source text with a mapping back to originals. */
public record PreprocessedSource(String source, SourceMapper mapper) {
    public PreprocessedSource {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(mapper, "mapper");
    }
}
