package io.github.protasm.jvmud.compiler.preproc;

import java.nio.file.Path;
import java.util.Objects;

/** Result of resolving an include directive. */
public record IncludeResolution(String source, Path resolvedPath, String displayPath) {
  public IncludeResolution {
    Objects.requireNonNull(source, "source");
  }
}
