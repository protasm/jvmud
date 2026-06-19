package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;

public sealed interface IRExpression extends IRNode
        permits IRArrayConcat,
                IRArrayDifference,
                IRArrayGet,
                IRArrayLiteral,
                IRArrayMutation,
                IRArraySet,
                IRBinaryOperation,
                IRCoerce,
                IRConditionalExpression,
                IRConstant,
                IRCollectionTransform,
                IRDynamicInvoke,
                IRDynamicInvokeExpression,
                IRDynamicInvokeField,
                IREfunCall,
                IRFieldLoad,
                IRFieldStore,
                IRForeachItems,
                IRForeachSize,
                IRForeachValue,
                IRInstanceCall,
                IRLocalLoad,
                IRLocalStore,
                IRMappingGet,
                IRMappingLiteral,
                IRMappingMerge,
                IRMappingSet,
                IRProtectedEval,
                IRSequence,
                IRStringDifference,
                IRSlice,
                IRStringGet,
                IRUnaryOperation {
    RuntimeType type();
}
