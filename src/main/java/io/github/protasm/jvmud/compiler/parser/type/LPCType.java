package io.github.protasm.jvmud.compiler.parser.type;

import static io.github.protasm.jvmud.compiler.parser.type.JType.JBOOLEAN;
import static io.github.protasm.jvmud.compiler.parser.type.JType.JFLOAT;
import static io.github.protasm.jvmud.compiler.parser.type.JType.JINT;
import static io.github.protasm.jvmud.compiler.parser.type.JType.JOBJECT;
import static io.github.protasm.jvmud.compiler.parser.type.JType.JSTRING;
import static io.github.protasm.jvmud.compiler.parser.type.JType.JVOID;

public enum LPCType {
    LPCINT(JINT),
    LPCFLOAT(JFLOAT),
    LPCMAPPING(null),
    LPCMIXED(JOBJECT),
    /** Compiler recovery type used after semantic errors; not a source-language LPC type. */
    LPCERROR(JOBJECT),
    /** Callable value, including typed LPC function literals accepted for mudlib compatibility. */
    LPCFUNCTION(JOBJECT),
    LPCOBJECT(JOBJECT),
    LPCSTATUS(JBOOLEAN),
    LPCSTRING(JSTRING),
    LPCARRAY(JOBJECT),
    LPCVOID(JVOID);

    private final JType jType;

    LPCType(JType jType) {
        this.jType = jType;
    }

    public JType jType() {
        return jType;
    }

    public static LPCType fromJavaType(Class<?> returnType) {
        if (returnType == void.class)
            return LPCType.LPCVOID;
        if (returnType == int.class)
            return LPCType.LPCINT;
        if (returnType == float.class)
            return LPCType.LPCFLOAT;
        if (returnType == boolean.class)
            return LPCType.LPCSTATUS;
        if (returnType == String.class)
            return LPCType.LPCSTRING;
        if (returnType == Object.class)
            return LPCType.LPCMIXED;

        return LPCType.LPCMIXED; // Default for unknown types
    }
}
