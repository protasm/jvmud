package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;

public sealed interface IRExpression extends IRNode
        permits IRArrayConcat,
                IRArrayGet,
                IRArrayLiteral,
                IRArraySet,
                IRBinaryOperation,
                IRCoerce,
                IRConditionalExpression,
                IRConstant,
                IRDynamicInvoke,
                IRDynamicInvokeField,
                IREfunCall,
                IRFieldLoad,
                IRFieldStore,
                IRInstanceCall,
                IRLocalLoad,
                IRLocalStore,
                IRMappingGet,
                IRMappingLiteral,
                IRMappingMerge,
                IRMappingSet,
                IRUnaryOperation {
    RuntimeType type();
}
