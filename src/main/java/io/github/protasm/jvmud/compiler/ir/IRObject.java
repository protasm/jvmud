package io.github.protasm.jvmud.compiler.ir;

import java.util.List;
import java.util.Objects;

public record IRObject(
        int line,
        String name,
        String parentInternalName,
        List<String> directInheritPaths,
        List<IRField> fields,
        List<IRMethod> methods) {
    public IRObject {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(parentInternalName, "parentInternalName");
        directInheritPaths = List.copyOf(directInheritPaths);
        fields = List.copyOf(fields);
        methods = List.copyOf(methods);
    }
}
