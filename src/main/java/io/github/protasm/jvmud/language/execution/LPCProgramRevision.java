package io.github.protasm.jvmud.language.execution;

import java.util.Objects;

/**
 * One linked version of an LPC source program, independent of any live object executing it.
 *
 * <p>The generated class and its defining class loader identify the executable revision, including
 * the inherited classes against which it links. Two revisions can have the same source object id
 * and JVM binary class name while remaining distinct classes in separate loaders. Shared objects
 * and clones choose revisions independently; compiling a revision does not itself replace an
 * existing object or change its world bindings.</p>
 *
 * @param sourceObjectId canonical mudlib source id, without a clone suffix
 * @param objectClass linked generated class supplying this revision's behavior
 */
public record LPCProgramRevision(String sourceObjectId, Class<?> objectClass) {
    /** Creates a revision after compilation and linking have produced its generated class. */
    public LPCProgramRevision {
        Objects.requireNonNull(sourceObjectId, "sourceObjectId");
        Objects.requireNonNull(objectClass, "objectClass");
        if (sourceObjectId.isBlank()) {
            throw new IllegalArgumentException("An LPC program revision requires a source object id.");
        }
    }
}
