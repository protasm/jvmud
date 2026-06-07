package io.github.protasm.jvmud.compiler.exec;

import java.util.List;

/** Snapshot of host-visible state for a loaded LPC object. */
public record LPCObjectInspection(
        String objectId,
        String className,
        String environmentId,
        List<String> inventoryIds,
        List<FieldValue> fields,
        List<MethodSignature> methods) {

    public LPCObjectInspection {
        inventoryIds = List.copyOf(inventoryIds);
        fields = List.copyOf(fields);
        methods = List.copyOf(methods);
    }

    public record FieldValue(String ownerClass, String ownerName, String type, String name, String value) {}

    public record MethodSignature(String ownerClass, String ownerName, String returnType, String name, List<String> parameterTypes) {
        public MethodSignature {
            parameterTypes = List.copyOf(parameterTypes);
        }
    }
}
