package io.github.protasm.jvmud.compiler.runtime;

/** Reads a bounded slice from an array inside a mudlib-owned JSON document. */
@FunctionalInterface
public interface MudlibJsonArrayReader {
    /**
     * Reads part of the JSON array selected by an RFC 6901 JSON Pointer.
     *
     * @param path mudlib-relative JSON file path
     * @param pointer JSON Pointer selecting an array; the empty pointer selects a root array
     * @param offset zero-based first array entry to return
     * @param count maximum number of array entries to return
     * @return an LPC-compatible array slice, or LPC false when the file is unavailable
     */
    Object read(String path, String pointer, int offset, int count);
}
