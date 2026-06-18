package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;

public sealed interface IRExpression extends IRNode
        permits IRArrayConcat,
                IRArrayGet,
                IRArrayLiteral,
                IRArrayMutation,
                IRArraySet,
                IRBinaryOperation,
                IRCoerce,
                IRConditionalExpression,
                IRConstant,
                IRDynamicInvoke,
                IRDynamicInvokeExpression,
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
                IRProtectedEval,
                IRSequence,
                IRSlice,
                IRStringGet,
                IRUnaryOperation {
    RuntimeType type();
}
