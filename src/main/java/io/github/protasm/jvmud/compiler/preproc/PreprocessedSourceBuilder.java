package io.github.protasm.jvmud.compiler.preproc;

import io.github.protasm.jvmud.compiler.sourcepos.LineMap;
import io.github.protasm.jvmud.compiler.sourcepos.SegmentedSourceMapper;
import io.github.protasm.jvmud.compiler.sourcepos.SourceMapper;
import java.util.Objects;

/** Accumulates expanded text while building a {@link SourceMapper}. */
final class PreprocessedSourceBuilder {
    private final StringBuilder text = new StringBuilder();
    private final SegmentedSourceMapper.Builder mapperBuilder = new SegmentedSourceMapper.Builder();

    void append(String lexeme, LineMap map, int originalStartOffset, int originalEndOffset) {
        Objects.requireNonNull(lexeme, "lexeme");
        Objects.requireNonNull(map, "map");

        text.append(lexeme);
        mapperBuilder.append(lexeme, map, originalStartOffset, originalEndOffset);
    }

    int length() {
        return text.length();
    }

    PreprocessedSource build() {
        SourceMapper mapper = mapperBuilder.build();

        return new PreprocessedSource(text.toString(), mapper);
    }
}
