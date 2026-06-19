package io.github.protasm.jvmud.compiler.runtime;

import io.github.protasm.jvmud.compiler.parser.type.JType;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.Objects;

/** Centralizes canonical runtime types and conversions from LPC surface types. */
public final class RuntimeTypes {
    public static final RuntimeType INT =
            new RuntimeType(RuntimeValueKind.INT, JType.JINT, null, RuntimeTruthiness.NUMERIC_ZERO_FALSE, null);

    public static final RuntimeType FLOAT =
            new RuntimeType(RuntimeValueKind.FLOAT, JType.JFLOAT, null, RuntimeTruthiness.NUMERIC_ZERO_FALSE, null);

    public static final RuntimeType STATUS =
            new RuntimeType(RuntimeValueKind.STATUS, JType.JBOOLEAN, null, RuntimeTruthiness.NUMERIC_ZERO_FALSE, null);

    public static final RuntimeType STRING =
            new RuntimeType(RuntimeValueKind.STRING, JType.JSTRING, "java/lang/String", RuntimeTruthiness.REFERENCE_NULL_FALSE, null);

    public static final RuntimeType OBJECT =
            new RuntimeType(RuntimeValueKind.OBJECT, JType.JOBJECT, "java/lang/Object", RuntimeTruthiness.REFERENCE_NULL_FALSE, null);

    /**
     * Runtime representation for first-class LPC callable values.
     */
    public static final RuntimeType CALLABLE =
            new RuntimeType(
                    RuntimeValueKind.CALLABLE,
                    JType.JOBJECT,
                    "io/github/protasm/jvmud/compiler/runtime/RuntimeCallable",
                    RuntimeTruthiness.REFERENCE_NULL_FALSE,
                    null);

    public static final RuntimeType MIXED =
            new RuntimeType(RuntimeValueKind.MIXED, JType.JOBJECT, "java/lang/Object", RuntimeTruthiness.REFERENCE_NULL_FALSE, null);

    /**
     * Internal Java {@code null} marker used by generated helper calls, not an LPC source type.
     */
    public static final RuntimeType INTERNAL_NULL =
            new RuntimeType(RuntimeValueKind.INTERNAL_NULL, JType.JINTERNAL_NULL, null, RuntimeTruthiness.ALWAYS_FALSE, null);

    /**
     * Compiler recovery type for expressions that survived only to suppress duplicate diagnostics.
     */
    public static final RuntimeType ERROR =
            new RuntimeType(RuntimeValueKind.ERROR, JType.JOBJECT, "java/lang/Object", RuntimeTruthiness.ALWAYS_FALSE, null);

    public static final RuntimeType VOID =
            new RuntimeType(RuntimeValueKind.VOID, JType.JVOID, null, RuntimeTruthiness.NONE, null);

    /**
     * Runtime representation for LPC mappings. JVMud treats mapping keys as general LPC values,
     * matching the Map-backed runtime model instead of narrowing keys to strings.
     */
    public static final RuntimeType MAPPING =
            new RuntimeType(RuntimeValueKind.MAPPING, JType.JOBJECT, "java/util/Map", RuntimeTruthiness.REFERENCE_NULL_FALSE, null);

    private static final RuntimeType EFUN =
            new RuntimeType(RuntimeValueKind.EFUN, JType.JOBJECT, "io/github/protasm/jvmud/compiler/efun/Efun", RuntimeTruthiness.REFERENCE_NULL_FALSE, null);

    private RuntimeTypes() {}

    public static RuntimeType arrayOf(RuntimeType elementType) {
        Objects.requireNonNull(elementType, "elementType");
        return new RuntimeType(
                RuntimeValueKind.ARRAY, JType.JOBJECT, "java/util/List", RuntimeTruthiness.REFERENCE_NULL_FALSE, elementType);
    }

    public static RuntimeType efunHandle() {
        return EFUN;
    }

    public static RuntimeType fromLpcType(LPCType type) {
        if (type == null)
            return MIXED;

        return switch (type) {
        case LPCINT -> INT;
        case LPCFLOAT -> FLOAT;
        case LPCMAPPING -> MAPPING;
        case LPCMIXED -> MIXED;
        case LPCERROR -> ERROR;
        case LPCFUNCTION -> CALLABLE;
        case LPCOBJECT -> OBJECT;
        case LPCSTATUS -> STATUS;
        case LPCSTRING -> STRING;
        case LPCARRAY -> arrayOf(MIXED);
        case LPCVOID -> VOID;
        };
    }
}
