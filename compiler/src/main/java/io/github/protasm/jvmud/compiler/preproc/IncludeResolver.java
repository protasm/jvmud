package io.github.protasm.jvmud.compiler.preproc;

import java.io.IOException;
import java.nio.file.Path;

public interface IncludeResolver {
  /**
   * Resolve an include path to source text.
   *
   * @param includingFile absolute or virtual path of the including fileName (may be null for roots)
   * @param includePath the raw string inside {@code #include "..."} or {@code #include <...>}
   * @param system true for {@code #include <...>}, false for {@code #include "..."}
   * @return the loaded source text
   * @throws IOException if not found or unreadable
   */
  IncludeResolution resolve(Path includingFile, String includePath, boolean system) throws IOException;
}
